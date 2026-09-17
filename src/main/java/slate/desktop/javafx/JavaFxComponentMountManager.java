package slate.desktop.javafx;

import javafx.scene.Node;
import slate.core.ComponentInstance;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Tracks JavaFX native mounts belonging to Slate component instances.
 *
 * <p>Component identity is runtime-instance identity, not component name.
 * Therefore, two usages of the same ComponentDefinition receive independent
 * native mounts.</p>
 *
 * <p>The manager owns the backend association between a ComponentInstance and
 * its native subtree. Actual scene-graph attachment state is observed by
 * JavaFxComponentMount itself.</p>
 */
public final class JavaFxComponentMountManager {

    private final Map<ComponentInstance, JavaFxComponentMount> mounts;

    public JavaFxComponentMountManager() {
        this.mounts = new IdentityHashMap<>();
    }

    /**
     * Creates and registers a native mount for a component instance.
     *
     * <p>Creating a mount does not itself mean that the native subtree is
     * attached. JavaFxComponentMount observes the JavaFX parent relationship
     * and updates its mounted state when the native tree changes.</p>
     */
    public JavaFxComponentMount createMount(ComponentInstance componentInstance, Node rootNode) {
        if (componentInstance == null) {
            throw new IllegalArgumentException("Component instance cannot be null");
        }

        if (rootNode == null) {
            throw new IllegalArgumentException("Component native root cannot be null");
        }

        if (mounts.containsKey(componentInstance)) {
            throw new IllegalStateException("A native mount already exists for component instance: "
                    + componentInstance.getName());
        }

        JavaFxComponentMount mount = new JavaFxComponentMount(componentInstance, rootNode);

        mounts.put(componentInstance, mount);

        return mount;
    }

    /**
     * Finds the mount belonging to a runtime component instance.
     */
    public JavaFxComponentMount find(ComponentInstance componentInstance) {
        if (componentInstance == null) {
            return null;
        }

        return mounts.get(componentInstance);
    }

    /**
     * Finds the native root belonging to a component instance.
     */
    public Node findRoot(ComponentInstance componentInstance) {
        JavaFxComponentMount mount = find(componentInstance);

        return mount == null ? null : mount.getRootNode();
    }

    /**
     * Returns the number of currently tracked component instances.
     */
    public int size() {

        return mounts.size();
    }

    /**
     * Returns a read-only snapshot of the current ownership records.
     */
    public Map<ComponentInstance, JavaFxComponentMount> snapshot() {

        return Collections.unmodifiableMap(new IdentityHashMap<>(mounts));
    }

    /**
     * Releases every backend ownership record.
     *
     * <p>This is used when the current application lifecycle ends or when a
     * partially-created render must be discarded.</p>
     */
    public void clear() {
        for (JavaFxComponentMount mount : mounts.values()) {
            mount.dispose();
        }

        mounts.clear();
    }
}