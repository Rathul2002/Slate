package slate.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        this(
                componentResolver,
                new BuiltInElementRegistry()
        );
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

        return buildNode(node, new ArrayList<>());
    }

    private ComponentTreeNode buildNode(SlateNode node, List<String> componentStack) {


        if (node.isText()) {
            return ComponentTreeNode.text(node.getText());
        }

        String type = node.getType();

        /*
         * If the registry knows this name, this XML node
         * represents a Slate component.
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
             * Resolve the component's own XML root.
             * Example:
             * <Home />
             * becomes:
             * Home [COMPONENT]
             *     └── View
             *         ├── Hero
             *         └── Information
             */
            ComponentTreeNode componentRoot = buildNode(definition.getRoot(), nextStack);

            /*
             * IMPORTANT:
             *
             * The definition is shared metadata.
             * The instance belongs to this particular <Home /> usage.
             */
            ComponentInstance componentInstance = ComponentInstance.create(definition);

            return ComponentTreeNode.component(
                    type,
                    node.getProps(),
                    List.of(componentRoot),
                    definition,
                    componentInstance
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

        List<ComponentTreeNode> children = new ArrayList<>();

        for (SlateNode child : node.getChildren()) {
            children.add(buildNode(child, componentStack));
        }

        return ComponentTreeNode.element(elementType, node.getProps(), children);
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