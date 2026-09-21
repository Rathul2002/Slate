package slate.core;

import java.util.List;
import java.util.Map;

/**
 * A node in Slate's resolved virtual component tree.
 *
 * Unlike SlateNode, which represents the raw XML structure,
 * ComponentTreeNode understands component boundaries.
 */
public final class ComponentTreeNode {

    public enum Kind {
        ELEMENT,
        COMPONENT,
        TEXT
    }

    private final Kind kind;
    private final String type;
    private final String text;
    private final Map<String, Object> props;
    private final List<ComponentTreeNode> children;
    private final ComponentDefinition componentDefinition;
    private final ComponentInstance componentInstance;
    private final ComponentInputs componentInputs;
    private final ComponentTreeNode internalRoot;

    /*
     * The component instance that owns the XML declaration of this node.
     *
     * This is deliberately different from the component whose native
     * subtree happens to contain the node after Content projection.
     */
    private final ComponentInstance declarationOwner;

    private ComponentTreeNode(
            Kind kind,
            String type,
            String text,
            Map<String, Object> props,
            List<ComponentTreeNode> children,
            ComponentInputs componentInputs,
            ComponentTreeNode internalRoot,
            ComponentDefinition componentDefinition,
            ComponentInstance componentInstance,
            ComponentInstance declarationOwner
    ) {
        if (kind == null) {
            throw new IllegalArgumentException("Tree node kind cannot be null");
        }

        if (props == null) {
            throw new IllegalArgumentException("Tree node props cannot be null");
        }

        if (children == null) {
            throw new IllegalArgumentException("Tree node children cannot be null");
        }

        if (kind == Kind.TEXT && text == null) {
            throw new IllegalArgumentException("Text tree node must contain text");
        }

        if (kind == Kind.COMPONENT && componentDefinition == null) {
            throw new IllegalArgumentException("Component tree node must contain a component definition");
        }

        if (kind == Kind.COMPONENT && componentInstance == null) {
            throw new IllegalArgumentException(
                    "Component tree node must contain a component instance"
            );
        }

        if (kind == Kind.COMPONENT && componentInputs == null) {
            throw new IllegalArgumentException("Component tree node must contain component inputs");
        }

        if (kind == Kind.COMPONENT && internalRoot == null) {
            throw new IllegalArgumentException("Component tree node must contain one resolved internal root");
        }

        if (kind != Kind.COMPONENT && (componentDefinition != null || componentInstance != null
                || componentInputs != null || internalRoot != null)) {
            throw new IllegalArgumentException("Only component tree nodes may contain component runtime data");
        }

        if (kind == Kind.COMPONENT && !componentInstance.getDefinition().equals(componentDefinition)) {

            throw new IllegalArgumentException("Component instance must belong to the supplied component definition");
        }

        if (kind == Kind.COMPONENT && !componentInstance.getProps().equals(componentInputs.getProps())) {

            throw new IllegalArgumentException("Component instance props must match component input props");
        }

        if (kind == Kind.COMPONENT && !props.equals(componentInputs.getProps())) {

            throw new IllegalArgumentException("Component tree props must match component input props");
        }

        /*
         * A component node must not use its normal children list for the
         * component's input children.
         *
         * The internal root is stored separately.
         */
        if (kind == Kind.COMPONENT && !children.isEmpty()) {
            throw new IllegalArgumentException("Component tree nodes cannot mix internal children "
                            + "with component input children");
        }

        this.kind = kind;
        this.type = type;
        this.text = text;
        this.props = Map.copyOf(props);
        this.children = List.copyOf(children);
        this.componentInputs = componentInputs;
        this.internalRoot = internalRoot;
        this.componentDefinition = componentDefinition;
        this.componentInstance = componentInstance;
        this.declarationOwner = declarationOwner;
    }

    /**
     * Creates a normal Slate element node.
     *
     * Examples:
     *
     * View
     * Window
     * Button
     * Text
     */
    public static ComponentTreeNode element(
            String type,
            Map<String, Object> props,
            List<ComponentTreeNode> children
    ) {
        return element(
                type,
                props,
                children,
                null
        );
    }

    /**
     * Creates a normal Slate element node.
     */
    public static ComponentTreeNode element(
            String type,
            Map<String, Object> props,
            List<ComponentTreeNode> children,
            ComponentInstance declarationOwner
    ) {
        return new ComponentTreeNode(
                Kind.ELEMENT,
                type,
                null,
                props,
                children,
                null,
                null,
                null,
                null,
                declarationOwner
        );
    }


    /**
     * Creates a component boundary node.
     *
     * A component contains three separate concepts:
     *
     * 1. component inputs supplied by the usage site
     * 2. one resolved internal root from the component definition
     * 3. one ComponentInstance representing this runtime usage
     */
    public static ComponentTreeNode component(
            String type,
            ComponentInputs inputs,
            ComponentTreeNode internalRoot,
            ComponentDefinition definition
    ) {
        return component(
                type,
                inputs,
                internalRoot,
                definition,
                ComponentInstance.create(
                        definition,
                        inputs.getProps()
                ),
                null
        );
    }

    /**
     * Creates a component node with its explicit runtime instance.
     */
    public static ComponentTreeNode component(
            String type,
            ComponentInputs inputs,
            ComponentTreeNode internalRoot,
            ComponentDefinition definition,
            ComponentInstance instance
    ) {
        return component(
                type,
                inputs,
                internalRoot,
                definition,
                instance,
                null
        );
    }

    /**
     * Creates a component node with its explicit runtime instance
     * and its declaration owner.
     *
     * <p>The declaration owner is the component whose XML declared
     * this component usage.</p>
     */
    public static ComponentTreeNode component(
            String type,
            ComponentInputs inputs,
            ComponentTreeNode internalRoot,
            ComponentDefinition definition,
            ComponentInstance instance,
            ComponentInstance declarationOwner
    ) {
        if (inputs == null) {
            throw new IllegalArgumentException(
                    "Component inputs cannot be null"
            );
        }

        if (internalRoot == null) {
            throw new IllegalArgumentException(
                    "Component internal root cannot be null"
            );
        }

        if (definition == null) {
            throw new IllegalArgumentException(
                    "Component definition cannot be null"
            );
        }

        if (instance == null) {
            throw new IllegalArgumentException(
                    "Component instance cannot be null"
            );
        }

        return new ComponentTreeNode(
                Kind.COMPONENT,
                type,
                null,
                inputs.getProps(),
                List.of(),
                inputs,
                internalRoot,
                definition,
                instance,
                declarationOwner
        );
    }

    public static ComponentTreeNode text(String text) {
        return text(text, null);
    }

    /**
     * Creates a text node with declaration ownership.
     */
    public static ComponentTreeNode text(
            String text,
            ComponentInstance declarationOwner
    ) {
        return new ComponentTreeNode(
                Kind.TEXT,
                null,
                text,
                Map.of(),
                List.of(),
                null,
                null,
                null,
                null,
                declarationOwner
        );
    }


    public Kind getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Map<String, Object> getProps() {
        return props;
    }

    public List<ComponentTreeNode> getChildren() {
        if (isComponent()) {
            return List.of(internalRoot);
        }

        return children;
    }

    public ComponentInputs getComponentInputs() {
        return componentInputs;
    }

    public ComponentTreeNode getInternalRoot() {
        return internalRoot;
    }

    public ComponentDefinition getComponentDefinition() {
        return componentDefinition;
    }

    public ComponentInstance getComponentInstance() {
        return componentInstance;
    }

    public ComponentInstance getDeclarationOwner() {
        return declarationOwner;
    }

    public boolean isComponent() {
        return kind == Kind.COMPONENT;
    }

    public boolean isElement() {
        return kind == Kind.ELEMENT;
    }

    public boolean isText() {
        return kind == Kind.TEXT;
    }
}