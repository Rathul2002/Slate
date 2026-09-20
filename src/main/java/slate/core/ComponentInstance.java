package slate.core;

import java.util.Map;

/**
 * Represents one runtime instance of a Slate component usage.
 *
 * <p>{@link ComponentDefinition} is shared component metadata. A
 * {@code ComponentInstance} belongs to one particular occurrence of a
 * component in the runtime tree.</p>
 *
 * <p>The instance owns runtime data associated with that particular usage,
 * including its current props and behavior instance.</p>
 *
 * <p>This is the foundation for future state, events, lifecycle, dependency
 * tracking, reconciliation, and native subtree ownership.</p>
 */
public final class ComponentInstance {

    private final ComponentDefinition definition;
    private final Object behaviorInstance;

    /*
     * Props belong to this runtime component usage, not to the shared
     * ComponentDefinition.
     */
    private Map<String, Object> props;

    private ComponentInstance(
            ComponentDefinition definition,
            Map<String, Object> props,
            Object behaviorInstance
    ) {
        this.definition = definition;
        this.behaviorInstance = behaviorInstance;
        this.props = copyProps(props);

        /*
         * Initial props are bound immediately after the behavior object is
         * created. The runtime prop map remains the source of truth.
         */
        if (behaviorInstance != null) {
            ComponentPropBinder.bind(behaviorInstance, this.props);
        }
    }

    /**
     * Creates one runtime instance from a component definition.
     *
     * <p>Visual-only components simply receive a null behavior instance.
     * Components with a Java behavior class currently require a public or
     * otherwise accessible no-argument constructor.</p>
     */
    public static ComponentInstance create(ComponentDefinition definition) {
        return create(definition, Map.of());
    }

    /**
     * Creates one runtime instance from a component definition and its
     * component-usage props.
     */
    public static ComponentInstance create(ComponentDefinition definition, Map<String, Object> props) {
        if (definition == null) {
            throw new IllegalArgumentException("Component definition cannot be null");
        }

        if (props == null) {
            throw new IllegalArgumentException("Component props cannot be null");
        }

        Class<?> behaviorClass = definition.getBehaviorClass();

        if (behaviorClass == null) {
            return new ComponentInstance(definition, props, null);
        }

        try {
            Object behaviorInstance = behaviorClass.getDeclaredConstructor().newInstance();

            return new ComponentInstance(definition, props, behaviorInstance);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create behavior instance for component: " + definition.getName()
                            + " (" + behaviorClass.getName() + ")", e);
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

    public Map<String, Object> getProps() {
        return props;
    }

    /**
     * Returns one current prop value.
     *
     * @return the prop value, or {@code null} when it is not present
     */
    public Object getProp(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Prop name cannot be null or blank");
        }

        return props.get(name);
    }

    /**
     * Updates the runtime prop snapshot.
     *
     * <p>This method is intentionally package-private. Application code should
     * not mutate component props directly. Future reconciliation/prop-binding
     * infrastructure inside {@code slate.core} will own prop updates.</p>
     */
    void updateProps(Map<String, Object> newProps) {
        Map<String, Object> nextProps = copyProps(newProps);

        if (behaviorInstance != null) {
            ComponentPropBinder.bind(behaviorInstance, nextProps);
        }

        this.props = nextProps;
    }

    public Object getBehaviorInstance() {
        return behaviorInstance;
    }

    public boolean hasBehavior() {
        return behaviorInstance != null;
    }

    /**
     * Creates the immutable runtime representation used for a prop snapshot.
     */
    private static Map<String, Object> copyProps(
            Map<String, Object> props
    ) {
        if (props == null) {
            throw new IllegalArgumentException("Component props cannot be null");
        }

        return Map.copyOf(props);
    }
}