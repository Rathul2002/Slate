package slate.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts raw Slate XML nodes into the resolved runtime component tree.
 *
 * <p>This is the point where component names are resolved, component
 * boundaries are created, recursive component references are detected and
 * runtime component instances are attached to component usages.</p>
 */
public class ComponentTreeBuilder {

    private final ComponentResolver componentResolver;
    private final BuiltInElementRegistry builtInElementRegistry;

    public ComponentTreeBuilder(ComponentResolver componentResolver) {
        this(componentResolver, new BuiltInElementRegistry());
    }

    public ComponentTreeBuilder(ComponentResolver componentResolver,
                                BuiltInElementRegistry builtInElementRegistry) {
        if (componentResolver == null) {
            throw new IllegalArgumentException("Component resolver cannot be null");
        }
        if (builtInElementRegistry == null) {
            throw new IllegalArgumentException("Built-in element registry cannot be null");
        }


        this.componentResolver = componentResolver;
        this.builtInElementRegistry = builtInElementRegistry;
    }

    /**
     * Builds a component tree from a raw SlateNode.
     */
    public ComponentTreeNode build(SlateNode node) {

        if (node == null) {
            throw new IllegalArgumentException("Slate node cannot be null");
        }

        return buildNode(
                node,
                new ArrayList<>(),
                null,
                null,
                null,
                false,
                null
        );
    }

    private ComponentTreeNode buildNode(
            SlateNode node,
            List<String> componentStack,
            ComponentInputs currentComponentInputs,
            ComponentInstance currentComponentInstance,
            ComponentInstance currentComponentInputOwner,
            boolean contentProjectionAllowed,
            ComponentInstance declarationOwner
    ) {


        if (node.isText()) {
            return ComponentTreeNode.text(node.getText(), declarationOwner);
        }

        String type = node.getType();

        /*
         * Content is a reserved Slate composition directive.
         * It is never resolved as a component or normal element here.
         */
        if (ContentProjection.isContentNode(node)) {
            throw new IllegalStateException("Content can only be used inside a component's internal XML");
        }

        /*
         * Content is a reserved Slate composition directive.
         * It is never resolved as a component or normal element here.
         */
        ComponentDefinition definition = tryResolveComponent(type);

        if (definition != null) {
            if (componentStack.contains(type)) {
                throw new IllegalStateException("Circular component reference detected: "
                        + buildComponentPath(componentStack, type)
                );
            }

            List<String> nextStack = new ArrayList<>(componentStack);

            nextStack.add(type);

            /*
             * These are the inputs supplied to THIS component usage.
             */
            ComponentInputs componentInputs = new ComponentInputs(node.getProps(), node.getChildren());

            /*
             * Create the runtime component BEFORE building its
             * internal XML.
             *
             * The instance will own nodes declared inside this
             * component's XML.
             */
            ComponentInstance componentInstance = ComponentInstance.create(definition, componentInputs.getProps());

            if (ContentProjection.isContentNode(definition.getRoot())) {
                throw new IllegalStateException("A component root cannot be <Content>: " + type);
            }

            /*
             * Build the component's own XML while giving the builder
             * access to this component usage's inputs.
             *
             * Content directives inside the component definition
             * therefore project THIS component's children.
             */
            ComponentTreeNode componentRoot = buildNode(
                    definition.getRoot(),
                    nextStack,
                    componentInputs,
                    componentInstance,
                    declarationOwner,
                    true,
                    componentInstance
            );
            /*
             * The definition is shared metadata.
             *
             * The props belong to this particular component usage.
             *
             * ComponentTreeNode.component(...) now creates the
             * ComponentInstance with those exact props.
             */
            return ComponentTreeNode.component(
                    type,
                    componentInputs,
                    componentRoot,
                    definition,
                    componentInstance,
                    declarationOwner
            );
        }

        /*
         * The node is not a component.
         *
         * It must be a built-in Slate element.
         *
         * Unknown tags are no longer silently accepted as arbitrary
         * elements.
         */
        String elementType = builtInElementRegistry.resolve(type);

        List<ComponentTreeNode> children = buildChildren(
                node.getChildren(),
                componentStack,
                currentComponentInputs,
                currentComponentInstance,
                currentComponentInputOwner,
                contentProjectionAllowed,
                declarationOwner
                );

        return ComponentTreeNode.element(elementType, node.getProps(), children, declarationOwner);
    }

    /**
     * Builds ordinary element children and expands Content directives.
     */
    private List<ComponentTreeNode> buildChildren(
            List<SlateNode> rawChildren,
            List<String> componentStack,
            ComponentInputs currentComponentInputs,
            ComponentInstance currentComponentInstance,
            ComponentInstance currentComponentInputOwner,
            boolean contentProjectionAllowed,
            ComponentInstance declarationOwner
    ) {

        List<ComponentTreeNode> resolvedChildren = new ArrayList<>();

        for (SlateNode child : rawChildren) {

            if (ContentProjection.isContentNode(child)) {

                if (!contentProjectionAllowed || currentComponentInputs == null) {

                    throw new IllegalStateException("Content can only be used inside a " + "component's internal XML");
                }

                ContentProjection projection = ContentProjection.from(child);

                /*
                 * Projection is intentionally non-consuming.
                 *
                 * Every matching input child is resolved and inserted
                 * in its original order.
                 */
                for (SlateNode inputChild : currentComponentInputs.getChildren()) {

                    if (!projection.matches(inputChild)) {
                        continue;
                    }

                    /*
                     * The projected child belongs to the component's
                     * runtime tree, but it is not itself part of the
                     * component-definition projection context.
                     *
                     * If the projected child is another component,
                     * that component creates its own ComponentInputs
                     * and therefore its own Content projection scope.
                     */
                    resolvedChildren.add(buildNode(
                            inputChild,
                            componentStack,
                            null,
                            null,
                            null,
                            false,
                            currentComponentInputOwner)
                    );
                }

                continue;
            }

            /*
             * This is normal component-definition UI.
             *
             * Therefore, nested Content directives must continue to
             * see the current component's inputs.
             */
            resolvedChildren.add(buildNode(
                    child,
                    componentStack,
                    currentComponentInputs,
                    currentComponentInstance,
                    currentComponentInputOwner,
                    contentProjectionAllowed,
                    declarationOwner)
            );
        }

        return resolvedChildren;
    }

    private ComponentDefinition tryResolveComponent(String type) {

        /*
         * The registry-backed resolver returns a definition
         * when this node is a known component.
         *
         * A normal native Slate element is not registered
         * as a component oe text, so it remains an ELEMENT node.
         */
        try {
            return componentResolver.resolve(type);
        } catch (IllegalStateException e) {

            /*
             * The resolver currently exposes "Unknown component" through
             * IllegalStateException. Preserve that existing contract for now.
             */
            if (e.getMessage() != null && e.getMessage().startsWith("Unknown component:")) {
                return null;
            }

            throw e;
        }
    }

    private String buildComponentPath(List<String> stack, String current) {

        if (stack.isEmpty()) {
            return current;
        }

        return String.join(" -> ", stack) + " -> " + current;
    }
}