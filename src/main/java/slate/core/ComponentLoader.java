package slate.core;

/**
 * Loads a registered Slate component.
 * ComponentResolver is responsible for finding the ComponentDefinition.
 * ComponentLoader is responsible for obtaining the component's XML
 * representation from that definition.
 */
public class ComponentLoader {

    private final ComponentResolver componentResolver;

    public ComponentLoader(ComponentResolver componentResolver) {

        if (componentResolver == null) {
            throw new IllegalArgumentException("Component resolver cannot be null");
        }

        this.componentResolver = componentResolver;
    }

    /**
     * Loads a component and returns its root SlateNode.
     * Example:
     * Home
     *   ↓
     * ComponentResolver
     *   ↓
     * ComponentDefinition
     *   ↓
     * root SlateNode
     */
    public SlateNode load(String componentName) {

        if (componentName == null || componentName.isBlank()) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }

        ComponentDefinition definition = componentResolver.resolve(componentName);

        return definition.getRoot();
    }
}
