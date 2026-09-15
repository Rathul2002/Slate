package slate.core;

/**
 * Describes a reusable Slate component.
 *
 * A component is identified by its XML filename, while the XML root element
 * describes the component's internal UI structure.
 */
public class ComponentDefinition {

    private final String name;
    private final String resourcePath;
    private final SlateNode root;
    private final Class<?> behaviorClass;

    public ComponentDefinition(
            String name,
            String resourcePath,
            SlateNode root,
            Class<?> behaviorClass
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }

        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Component resource path cannot be null or empty");
        }

        if (root == null) {
            throw new IllegalArgumentException("Component root cannot be null");
        }

        this.name = name;
        this.resourcePath = resourcePath;
        this.root = root;
        this.behaviorClass = behaviorClass;
    }

    public String getName() {
        return name;
    }

    public SlateNode getRoot() {
        return root;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Returns the optional Java behavior class for this component.
     *
     * A null value is valid because visual-only components do not require
     * a Java class.
     */
    public Class<?> getBehaviorClass() {
        return behaviorClass;
    }


}
