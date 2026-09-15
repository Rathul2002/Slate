package slate.core;


/**
 * Resolves a component name to its registered ComponentDefinition.
 *
 * The resolver does not guess the XML resource path.
 * The ComponentDefinition already contains the actual resource path
 * discovered during component scanning.
 */

public class ComponentResolver {

    private final ComponentRegistry componentRegistry;

    public ComponentResolver(ComponentRegistry componentRegistry) {

        if (componentRegistry == null) {
            throw new IllegalArgumentException("Component registry cannot be null");
        }

        this.componentRegistry = componentRegistry;
    }



    /**
     * Resolves a component name to its complete definition.
     *
     * Example:
     *
     * Home
     *   ↓
     * ComponentRegistry
     *   ↓
     * ComponentDefinition
     *   ↓
     * pages/Home/Home.xml
     */
    public ComponentDefinition resolve(String componentName) {

        if (componentName == null || componentName.isBlank()) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }

        ComponentDefinition definition = componentRegistry.find(componentName);

        if (definition == null) {
            throw new IllegalStateException("Unknown component: " + componentName);
        }

        return definition;
    }
}