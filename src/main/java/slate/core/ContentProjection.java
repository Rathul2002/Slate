package slate.core;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents Slate's internal component-content projection directive.
 *
 * <p>{@code <Content/>} and {@code <Content class="actions"/>} are not
 * renderer elements. They are consumed while the component tree is being
 * built and therefore never reach a native renderer.</p>
 */
final class ContentProjection {

    static final String TYPE = "content";

    private static final String CLASS_PROP = "class";

    private final Set<String> requiredClasses;

    private ContentProjection(Set<String> requiredClasses) {
        this.requiredClasses = Set.copyOf(requiredClasses);
    }

    static boolean isContentNode(SlateNode node) {
        return node != null
                && node.isElement()
                && TYPE.equalsIgnoreCase(node.getType());
    }

    static ContentProjection from(SlateNode node) {

        if (!isContentNode(node)) {
            throw new IllegalArgumentException("Node is not a Content projection: "
                            + (node == null ? "null" : node.getType()));
        }

        if (!node.getChildren().isEmpty()) {
            throw new IllegalStateException("Content cannot contain child nodes");
        }

        Map<String, Object> props = node.getProps();

        for (String propName : props.keySet()) {

            if (!CLASS_PROP.equals(propName)) {
                throw new IllegalStateException("Content supports only the 'class' attribute: " + propName);
            }
        }

        return new ContentProjection(parseClasses(props.get(CLASS_PROP)));
    }

    boolean matches(SlateNode child) {

        if (requiredClasses.isEmpty()) {
            return true;
        }

        return parseClasses(child.getProps().get(CLASS_PROP)).containsAll(requiredClasses);
    }

    private static Set<String> parseClasses(Object value) {

        if (value == null) {
            return Set.of();
        }

        if (!(value instanceof String classValue)) {
            throw new IllegalStateException("The 'class' attribute must be a String");
        }

        String normalized = classValue.trim();

        if (normalized.isEmpty()) {
            return Set.of();
        }

        String[] tokens = normalized.split("\\s+");

        LinkedHashSet<String> classes = new LinkedHashSet<>();

        for (String token : tokens) {

            if (!token.isBlank()) {
                classes.add(token);
            }
        }

        return Set.copyOf(classes);
    }
}