package slate.core;

/**
 * Represents one runtime instance of a Slate component usage.
 *
 * <p>{@link ComponentDefinition} is shared component metadata. A
 * {@code ComponentInstance} belongs to one particular occurrence of a
 * component in the runtime tree.</p>
 *
 * <p>This is the foundation for future props, state, events, lifecycle,
 * dependency tracking, and native subtree ownership.</p>
 */
public final class ComponentInstance {

    private final ComponentDefinition definition;
    private final Object behaviorInstance;

    private ComponentInstance(
            ComponentDefinition definition,
            Object behaviorInstance
    ) {
        this.definition = definition;
        this.behaviorInstance = behaviorInstance;
    }

    /**
     * Creates one runtime instance from a component definition.
     *
     * <p>Visual-only components simply receive a null behavior instance.
     * Components with a Java behavior class currently require a public or
     * otherwise accessible no-argument constructor.</p>
     */
    public static ComponentInstance create(ComponentDefinition definition) {

        if (definition == null) {
            throw new IllegalArgumentException("Component definition cannot be null");
        }

        Class<?> behaviorClass = definition.getBehaviorClass();

        if (behaviorClass == null) {
            return new ComponentInstance(definition, null);
        }

        try {
            Object behaviorInstance = behaviorClass.getDeclaredConstructor().newInstance();

            return new ComponentInstance(definition, behaviorInstance);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create behavior instance for component: "
                            + definition.getName()
                            + " ("
                            + behaviorClass.getName()
                            + ")",
                    e
            );
        }
    }

    public ComponentDefinition getDefinition() {
        return definition;
    }

    public String getName() {
        return definition.getName();
    }

    public Class<?> getBehaviorClass() {
        return definition.getBehaviorClass();
    }

    public Object getBehaviorInstance() {
        return behaviorInstance;
    }

    public boolean hasBehavior() {
        return behaviorInstance != null;
    }
}