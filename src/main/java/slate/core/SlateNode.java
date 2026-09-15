package slate.core;

import java.util.List;
import java.util.Map;

public class SlateNode {

    public enum Kind {
        ELEMENT,
        TEXT
    }

    private final Kind kind;
    private final String type;
    private final String text;
    private final Map<String, Object> props;
    private final List<SlateNode> children;

    public SlateNode(
            String type,
            Map<String, Object> props,
            List<SlateNode> children
    ) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Node type cannot be null or empty");
        }

        if (props == null) {
            throw new IllegalArgumentException("Node props cannot be null");
        }

        if (children == null) {
            throw new IllegalArgumentException("Node children cannot be null");
        }

        this.kind = Kind.ELEMENT;
        this.type = type;
        this.text = null;
        this.props = Map.copyOf(props);
        this.children = List.copyOf(children);
    }

    protected SlateNode(
            Kind kind,
            String type,
            String text,
            Map<String, Object> props,
            List<SlateNode> children
    ) {
        if (kind == null) {
            throw new IllegalArgumentException("Node kind cannot be null");
        }

        this.kind = kind;
        this.type = type;
        this.text = text;
        this.props = Map.copyOf(props);
        this.children = List.copyOf(children);
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

    public List<SlateNode> getChildren() {
        return children;
    }

    public boolean isText() {
        return kind == Kind.TEXT;
    }

    public boolean isElement() {
        return kind == Kind.ELEMENT;
    }
}