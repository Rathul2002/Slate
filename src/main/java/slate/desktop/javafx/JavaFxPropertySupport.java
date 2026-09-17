package slate.desktop.javafx;

import slate.core.ComponentTreeNode;

import java.util.Map;

/**
 * Utility methods for reading static Slate properties from a runtime node.
 *
 * <p>The Slate runtime stores properties as {@code Map<String, Object>}.
 * XML attributes currently arrive as strings, but keeping the runtime value
 * type as Object allows the property model to evolve without changing the
 * core tree structure.</p>
 *
 * <p>This class only supports the current static-property phase. It is not
 * the future reactive Props system.</p>
 */
public final class JavaFxPropertySupport {

    /**
     * Returns the immutable property map belonging to a runtime node.
     *
     * <p>The JavaFX backend reads the runtime properties but does not mutate
     * the map owned by the runtime tree.</p>
     */
    public static Map<String, Object> props(ComponentTreeNode node) {
        validateNode(node);
        return node.getProps();
    }

    /**
     * Returns a raw property value.
     *
     * @return the property value, or null when the property is absent
     */
    public static Object get(ComponentTreeNode node, String name) {
        validatePropertyName(name);
        return props(node).get(name);
    }

    /**
     * Returns a property as text.
     *
     * <p>XML attributes currently arrive as strings, while future runtime
     * property values may already be typed. This method supports both cases.</p>
     */
    public static String getString(ComponentTreeNode node, String name) {
        Object value = get(node, name);

        if (value == null) {
            return null;
        }

        return value instanceof String ? (String) value : String.valueOf(value);
    }

    /**
     * Returns a textual property or the supplied default value when absent.
     */
    public static String getString(ComponentTreeNode node, String name, String defaultValue) {
        String value = getString(node, name);
        return value == null ? defaultValue : value;
    }

    /**
     * Reads a static integer property.
     *
     * <p>Both numeric runtime values and textual XML values are supported.</p>
     */
    public static int getInt(ComponentTreeNode node, String name, int defaultValue) {
        Object value = get(node, name);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw invalidProperty(node, name, value, "integer", e);
        }
    }

    /**
     * Reads a static floating-point property.
     *
     * <p>Both numeric runtime values and textual XML values are supported.</p>
     */
    public static double getDouble(ComponentTreeNode node, String name, double defaultValue) {
        Object value = get(node, name);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw invalidProperty(node, name, value, "number", e);
        }
    }

    /**
     * Reads a finite floating-point property.
     *
     * <p>NaN and positive/negative infinity are rejected because they do not
     * represent a meaningful static native UI value.</p>
     */
    public static double getFiniteDouble(ComponentTreeNode node, String name, double defaultValue) {
        double value = getDouble(node, name, defaultValue);

        if (!Double.isFinite(value)) {
            throw invalidProperty(node, name, value, "finite number", null);
        }

        return value;
    }

    /**
     * Reads a finite positive floating-point property.
     *
     * <p>The returned value must be greater than zero.</p>
     */
    public static double getPositiveDouble(ComponentTreeNode node, String name, double defaultValue) {
        double value = getFiniteDouble(node, name, defaultValue);

        if (value <= 0) {
            throw invalidProperty(
                    node,
                    name,
                    value,
                    "positive finite number",
                    null
            );
        }

        return value;
    }

    /**
     * Reads a finite non-negative floating-point property.
     *
     * <p>Zero is accepted. Negative values are rejected.</p>
     */
    public static double getNonNegativeDouble(ComponentTreeNode node, String name, double defaultValue) {
        double value = getFiniteDouble(node, name, defaultValue);

        if (value < 0) {
            throw invalidProperty(node, name, value, "non-negative finite number", null);
        }

        return value;
    }

    /**
     * Reads a static boolean property.
     *
     * <p>Only true and false are accepted for textual values. Invalid values
     * produce an explicit error instead of silently becoming false.</p>
     */
    public static boolean getBoolean(ComponentTreeNode node, String name, boolean defaultValue) {
        Object value = get(node, name);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return defaultValue;
        }

        if ("true".equalsIgnoreCase(text)) {
            return true;
        }

        if ("false".equalsIgnoreCase(text)) {
            return false;
        }

        throw invalidProperty(node, name, value, "boolean (true or false)", null);
    }

    private static void validateNode(ComponentTreeNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Property node cannot be null");
        }
    }

    private static void validatePropertyName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Property name cannot be blank");
        }
    }

    private static IllegalArgumentException invalidProperty(
            ComponentTreeNode node,
            String name,
            Object value,
            String expectedType,
            Exception cause
    ) {
        String message = "Invalid property '" + name +
                        "' on <" + node.getType() +
                        ">: '" + value +
                        "'; expected " + expectedType;

        if (cause == null) {
            return new IllegalArgumentException(message);
        }

        return new IllegalArgumentException(message, cause);
    }
}