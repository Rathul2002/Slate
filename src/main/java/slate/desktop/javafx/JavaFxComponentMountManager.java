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
 */
public final class JavaFxComponentMountManager {

    private final Map<ComponentInstance, JavaFxComponentMount> mounts;

    public JavaFxComponentMountManager() {
        this.mounts = new IdentityHashMap<>();
    }

    /**
     * Creates and registers a mount for a component instance.
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
     * Marks a registered mount as attached to the native tree.
     */
    public void mount(ComponentInstance componentInstance) {
        JavaFxComponentMount mount = requireMount(componentInstance);

        mount.markMounted();
    }

    /**
     * Removes the ownership record for a component instance.
     *
     * <p>Native parent removal is intentionally not implemented yet. That
     * belongs to the later full mounting/reconciliation model.</p>
     */
    public JavaFxComponentMount unmount(ComponentInstance componentInstance) {
        if (componentInstance == null) {
            throw new IllegalArgumentException("Component instance cannot be null");
        }

        JavaFxComponentMount mount = mounts.remove(componentInstance);

        if (mount == null) {
            throw new IllegalStateException("No native mount exists for component instance: "
                    + componentInstance.getName());
        }

        mount.markUnmounted();

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
     * Clears all backend ownership records.
     *
     * <p>Native tree detachment will be handled by the later full mount
     * lifecycle implementation.</p>
     */
    public void clear() {

        for (JavaFxComponentMount mount : mounts.values()) {
            mount.markUnmounted();
        }

        mounts.clear();
    }

    private JavaFxComponentMount requireMount(ComponentInstance componentInstance) {
        if (componentInstance == null) {
            throw new IllegalArgumentException("Component instance cannot be null");
        }

        JavaFxComponentMount mount = mounts.get(componentInstance);

        if (mount == null) {
            throw new IllegalStateException("No native mount exists for component instance: "
                                                + componentInstance.getName());
        }

        return mount;
    }
}