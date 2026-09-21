package slate.core;

import java.util.List;
import java.util.Map;

/**
 * Represents the inputs supplied to one component usage.
 *
 * <p>Props and children are deliberately kept separate from the component's
 * resolved internal UI root. Props are named component inputs, while children
 * are ordered raw Slate nodes supplied by the component usage site.</p>
 *
 * <p>The children list is immutable and preserves source XML order. Content
 * projection resolves these raw child inputs into the component's internal
 * runtime tree.</p>
 */
public final class ComponentInputs {

    private final Map<String, Object> props;
    private final List<SlateNode> children;

    public ComponentInputs(
            Map<String, Object> props,
            List<SlateNode> children
    ) {
        if (props == null) {
            throw new IllegalArgumentException("Component input props cannot be null");
        }

        if (children == null) {
            throw new IllegalArgumentException("Component input children cannot be null");
        }

        this.props = Map.copyOf(props);
        this.children = List.copyOf(children);
    }

    public Map<String, Object> getProps() {
        return props;
    }

    public List<SlateNode> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}