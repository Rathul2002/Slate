package slate.desktop.javafx;

import javafx.scene.Node;
import slate.core.ComponentTreeNode;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry of JavaFX implementations for Slate elements.
 *
 * <p>The registry is populated through Java's ServiceLoader mechanism.
 * Individual element implementations therefore remain independent of the
 * renderer coordinator.</p>
 */
public final class JavaFxElementRegistry {

    private final Map<String, JavaFxElementRenderer> renderers = new LinkedHashMap<>();

    public JavaFxElementRegistry() {
        discoverRenderers();
    }

    /**
     * Discovers every JavaFxElementRenderer visible to the backend class
     * loader.
     *
     * <p>The standard JavaFX backend JAR provides its element implementations
     * through META-INF/services. Extension JARs can contribute additional
     * implementations using the same mechanism.</p>
     */
    private void discoverRenderers() {

        ServiceLoader<JavaFxElementRenderer> loader = ServiceLoader.load(JavaFxElementRenderer.class);

        boolean foundRenderer = false;

        for (JavaFxElementRenderer renderer : loader) {

            foundRenderer = true;
            register(renderer);
        }

        if (!foundRenderer) {
            throw new IllegalStateException("No JavaFX Slate element renderers were discovered");
        }
    }


    /**
     * Registers one discovered renderer.
     *
     * <p>Renderer names are normalized so registration remains
     * case-insensitive in the same way built-in element lookup currently is.</p>
     */
    public void register(JavaFxElementRenderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException("Element renderer cannot be null");
        }

        String type = normalize(renderer.getElementType());

        if (type.isBlank()) {
            throw new IllegalArgumentException("Element renderer type cannot be blank");
        }

        JavaFxElementRenderer previous = renderers.putIfAbsent(type, renderer);

        if (previous != null) {
            throw new IllegalStateException("Duplicate JavaFX element renderer: " + type);
        }
    }

    /**
     * Resolves and renders an element using its registered implementation.
     */
    public Node render(ComponentTreeNode node, JavaFxRenderContext context) {
        if (node == null) {
            throw new IllegalArgumentException("Render node cannot be null");
        }

        if (!node.isElement()) {
            throw new IllegalArgumentException("JavaFX element registry can only render ELEMENT nodes");
        }

        String type = normalize(node.getType());

        JavaFxElementRenderer renderer = renderers.get(type);

        if (renderer == null) {
            throw new IllegalStateException("Unsupported Slate element: " + node.getType());
        }

        return renderer.render(node, context);
    }


    /**
     * Returns true when a JavaFX renderer exists for the supplied element.
     */
    public boolean contains(String type) {
        return type != null && renderers.containsKey(normalize(type));
    }

    /**
     * Returns the implementation for a type without rendering it.
     *
     * <p>This is useful for later validation and diagnostics.</p>
     */
    public JavaFxElementRenderer find(String type) {
        if (type == null) {
            return null;
        }

        return renderers.get(normalize(type));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}