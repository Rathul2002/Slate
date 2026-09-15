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

    private ComponentTreeNode(
            Kind kind,
            String type,
            String text,
            Map<String, Object> props,
            List<ComponentTreeNode> children,
            ComponentDefinition componentDefinition,
            ComponentInstance componentInstance
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

        if (kind != Kind.COMPONENT && (componentDefinition != null || componentInstance != null)) {
            throw new IllegalArgumentException("Only component tree nodes may contain component runtime data");
        }

        if (kind == Kind.COMPONENT && componentInstance.getDefinition() != componentDefinition) {
            throw new IllegalArgumentException("Component instance must belong to the supplied component definition");
        }


        this.kind = kind;
        this.type = type;
        this.text = text;
        this.props = Map.copyOf(props);
        this.children = List.copyOf(children);
        this.componentDefinition = componentDefinition;
        this.componentInstance = componentInstance;
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
        return new ComponentTreeNode(
                Kind.ELEMENT,
                type,
                null,
                props,
                children,
                null,
                null
        );
    }


    /**
     * Creates a component boundary node.
     *
     * The component definition identifies the reusable component,
     * while the props belong to this particular component usage.
     */
    public static ComponentTreeNode component(
            String type,
            Map<String, Object> props,
            List<ComponentTreeNode> children,
            ComponentDefinition definition
    ) {
        return new ComponentTreeNode(
                Kind.COMPONENT,
                type,
                null,
                props,
                children,
                definition,
                ComponentInstance.create(definition)
        );
    }

    /**
     * Creates a component node with its explicit runtime instance.
     */
    public static ComponentTreeNode component(
            String type,
            Map<String, Object> props,
            List<ComponentTreeNode> children,
            ComponentDefinition definition,
            ComponentInstance instance
    ) {
        return new ComponentTreeNode(
                Kind.COMPONENT,
                type,
                null,
                props,
                children,
                definition,
                instance
        );
    }

    public static ComponentTreeNode text(String text) {
        return new ComponentTreeNode(
                Kind.TEXT,
                null,
                text,
                Map.of(),
                List.of(),
                null,
                null
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
        return children;
    }

    public ComponentDefinition getComponentDefinition() {
        return componentDefinition;
    }

    public ComponentInstance getComponentInstance() {
        return componentInstance;
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