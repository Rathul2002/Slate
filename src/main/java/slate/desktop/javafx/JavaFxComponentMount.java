package slate.desktop.javafx;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import slate.core.ComponentInstance;

/**
 * Represents the JavaFX-side native ownership of one Slate component instance.
 *
 * <p>A component instance belongs to slate.core and therefore cannot contain
 * JavaFX objects. This backend object associates that runtime instance with
 * the native root of its rendered subtree.</p>
 *
 * <p>The mounted state reflects the native scene graph rather than merely
 * successful node creation. The state is therefore driven by the native
 * root's parent relationship.</p>
 */
public final class JavaFxComponentMount {

    private final ComponentInstance componentInstance;
    private final Node rootNode;

    private final ChangeListener<Parent> parentListener;

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

        /*
         * The JavaFX scene graph is the authoritative source for whether this
         * native subtree is currently attached.
         */
        this.parentListener = (observable, oldParent, newParent) ->
                mounted = newParent != null;

        rootNode.parentProperty().addListener(parentListener);

        /*
         * A mount may theoretically be created around an already-attached
         * native node, so initialize the state from the current scene graph.
         */
        this.mounted = rootNode.getParent() != null;
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

    /**
     * Releases the scene-graph listener owned by this mount.
     *
     * <p>The mount manager calls this when the runtime no longer tracks the
     * component instance. Removing the listener prevents the native Node from
     * retaining this backend mount after ownership has been released.</p>
     */
    void dispose() {
        rootNode.parentProperty().removeListener(parentListener);
        mounted = false;
    }
}