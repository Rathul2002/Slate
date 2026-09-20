package slate.core;

import slate.core.annotation.Prop;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Binds Slate runtime props to fields marked with {@link Prop}.
 *
 * <p>The XML values already exist inside the ComponentInstance prop map.
 * This class connects those values to the Java behavior object.</p>
 */
final class ComponentPropBinder {

    private ComponentPropBinder() {
    }

    /**
     * Applies the supplied props to all matching {@code @Prop} fields.
     *
     * <p>Missing props are intentionally ignored so a Java field may define
     * its own default value.</p>
     */
    static void bind(Object behaviorInstance, Map<String, Object> props) {
        if (behaviorInstance == null) {
            throw new IllegalArgumentException("Behavior instance cannot be null");
        }

        if (props == null) {
            throw new IllegalArgumentException("Props cannot be null");
        }

        List<FieldBinding> bindings = collectBindings(behaviorInstance.getClass(), props);

        /*
         * Validate and convert everything before changing the behavior object.
         * This prevents a conversion error from occurring halfway through
         * the binding process.
         */
        List<ResolvedBinding> resolvedBindings = new ArrayList<>();

        for (FieldBinding binding : bindings) {
            Object rawValue = props.get(binding.propName());

            Object convertedValue = convertValue(rawValue,
                    binding.field().getType(), binding.propName());

            resolvedBindings.add(new ResolvedBinding(binding.field(),
                            convertedValue, binding.propName()));
        }

        /*
         * All values have been validated and converted.
         * Now write them into the actual Java fields.
         */
        for (ResolvedBinding binding : resolvedBindings) {
            try {
                if (!binding.field().trySetAccessible()) {
                    throw new IllegalStateException("Cannot access @Prop field '"
                                    + binding.field().getName() + "'");
                }

                /*
                 * THIS is the operation that actually changes the variable.
                 *
                 * Example:
                 *   field      -> private String title;
                 *   value      -> "Hello"
                 *
                 * becomes:
                 *   title = "Hello";
                 */
                binding.field().set(behaviorInstance, binding.value());

            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new IllegalStateException("Failed to bind prop '" + binding.propName()
                                + "' to field '" + binding.field().getName() + "'", e);
            }
        }

    }

    /**
     * Finds all @Prop fields in the behavior class hierarchy.
     */
    private static List<FieldBinding> collectBindings(Class<?> behaviorClass, Map<String, Object> props) {
        List<FieldBinding> bindings = new ArrayList<>();
        Set<String> propNames = new HashSet<>();

        Class<?> currentClass = behaviorClass;

        while (currentClass != null && currentClass != Object.class) {

            for (Field field : currentClass.getDeclaredFields()) {

                if (!field.isAnnotationPresent(Prop.class)) {
                    continue;
                }

                int modifiers = field.getModifiers();

                if (Modifier.isStatic(modifiers)) {
                    throw new IllegalStateException("@Prop field cannot be static: "
                                    + currentClass.getName() + "." + field.getName());
                }

                if (Modifier.isFinal(modifiers)) {
                    throw new IllegalStateException("@Prop field cannot be final: " + currentClass.getName()
                                    + "." + field.getName());
                }

                Prop annotation = field.getAnnotation(Prop.class);

                String propName = annotation.value().isBlank() ? field.getName() : annotation.value();

                if (!propNames.add(propName)) {
                    throw new IllegalStateException("Duplicate @Prop name '" + propName
                                    + "' in component behavior: " + behaviorClass.getName());
                }

                /*
                 * A field without a matching XML/runtime prop is left
                 * untouched. This allows Java-side default values.
                 */
                if (!props.containsKey(propName)) {
                    continue;
                }

                bindings.add(new FieldBinding(field, propName));
            }

            currentClass = currentClass.getSuperclass();
        }

        return bindings;
    }

    /**
     * Converts the runtime prop value to the Java field type.
     */
    private static Object convertValue(Object value, Class<?> targetType, String propName) {
        if (value == null) {

            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Prop '" + propName + "' cannot be null for primitive type "
                                + targetType.getName());
            }

            return null;
        }

        /*
         * If the runtime value is already the required type,
         * no conversion is necessary.
         */
        if (isAssignable(targetType, value.getClass())) {
            return value;
        }

        String text = String.valueOf(value).trim();

        if (targetType == String.class) {
            return text;
        }

        if (targetType == int.class || targetType == Integer.class) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw invalidConversion(propName, text, "integer", e);
            }
        }

        if (targetType == long.class || targetType == Long.class) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException e) {
                throw invalidConversion(propName, text, "long integer", e);
            }
        }

        if (targetType == double.class || targetType == Double.class) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw invalidConversion(propName, text, "double", e);
            }
        }

        if (targetType == float.class || targetType == Float.class) {
            try {
                return Float.parseFloat(text);
            } catch (NumberFormatException e) {
                throw invalidConversion(propName, text, "float", e);
            }
        }

        if (targetType == short.class || targetType == Short.class) {
            try {
                return Short.parseShort(text);
            } catch (NumberFormatException e) {
                throw invalidConversion(propName, text, "short integer", e);
            }
        }

        if (targetType == byte.class || targetType == Byte.class) {
            try {
                return Byte.parseByte(text);
            } catch (NumberFormatException e) {
                throw invalidConversion(propName, text, "byte", e);
            }
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }

            if ("false".equalsIgnoreCase(text)) {
                return false;
            }

            throw invalidConversion(propName, text, "boolean (true or false)", null);
        }

        if (targetType == char.class || targetType == Character.class) {
            if (text.length() != 1) {
                throw invalidConversion(propName, text, "single character", null);
            }

            return text.charAt(0);
        }

        throw new IllegalArgumentException("Unsupported @Prop type '" + targetType.getName()
                        + "' for prop '" + propName + "'");
    }

    private static boolean isAssignable(Class<?> targetType, Class<?> valueType) {
        if (targetType.isAssignableFrom(valueType)) {
            return true;
        }

        if (targetType == int.class && valueType == Integer.class) {
            return true;
        }

        if (targetType == long.class && valueType == Long.class) {
            return true;
        }

        if (targetType == double.class && valueType == Double.class) {
            return true;
        }

        if (targetType == float.class && valueType == Float.class) {
            return true;
        }

        if (targetType == short.class && valueType == Short.class) {
            return true;
        }

        if (targetType == byte.class && valueType == Byte.class) {
            return true;
        }

        if (targetType == boolean.class && valueType == Boolean.class) {
            return true;
        }

        if (targetType == char.class && valueType == Character.class) {
            return true;
        }

        return false;
    }

    private static IllegalArgumentException invalidConversion(
            String propName,
            Object value,
            String expectedType,
            Exception cause
    ) {
        String message = "Invalid value for prop '" + propName + "': '"
                        + value + "'; expected " + expectedType;

        if (cause == null) {
            return new IllegalArgumentException(message);
        }

        return new IllegalArgumentException(message, cause);
    }

    private record FieldBinding(Field field, String propName) {
    }

    private record ResolvedBinding(Field field, Object value, String propName) {
    }
}
