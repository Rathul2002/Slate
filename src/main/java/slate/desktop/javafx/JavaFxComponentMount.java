package slate.desktop.javafx;

import javafx.scene.Node;
import slate.core.ComponentInstance;

/**
 * Represents the JavaFX-side native ownership of one Slate component
 * instance.
 *
 * <p>ComponentInstance belongs to {@code slate.core} and therefore cannot contain
 * JavaFX objects. This backend object associates that runtime instance with
 * the native root of the subtree created for it.</p>
 */
public final class JavaFxComponentMount {

    private final ComponentInstance componentInstance;
    private final Node rootNode;

    private boolean mounted;

    /**
     * Creates a native mount for one component instance.
     *
     * @param componentInstance runtime component instance
     * @param rootNode native root representing the component subtree
     */
    public JavaFxComponentMount(ComponentInstance componentInstance, Node rootNode) {
        if (componentInstance == null) {
            throw new IllegalArgumentException("Component instance cannot be null");
        }

        if (rootNode == null) {
            throw new IllegalArgumentException("Component native root cannot be null");
        }

        this.componentInstance = componentInstance;
        this.rootNode = rootNode;
    }

    /**
     * Returns the runtime component instance represented by this mount.
     */
    public ComponentInstance getComponentInstance() {
        return componentInstance;
    }

    /**
     * Returns the native root node owned by the component mount.
     */
    public Node getRootNode() {
        return rootNode;
    }

    /**
     * Returns whether this mount is currently attached.
     */
    public boolean isMounted() {
        return mounted;
    }

    void markMounted() {
        if (mounted) {
            throw new IllegalStateException("Component mount is already mounted: " + componentInstance.getName());
        }

        mounted = true;
    }

    void markUnmounted() {
        mounted = false;
    }
}