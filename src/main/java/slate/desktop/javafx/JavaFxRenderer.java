package slate.desktop.javafx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import slate.core.ComponentTreeNode;
import slate.core.renderer.Renderer;
import slate.core.renderer.RendererProvider;

/**
 * Coordinates rendering of a resolved Slate component tree using JavaFX.
 *
 * <p>The renderer owns the application Window for the current lifecycle,
 * while JavaFxRuntime owns the JavaFX toolkit.</p>
 *
 * <p>Individual element rendering is delegated to JavaFxRenderContext and
 * JavaFxElementRegistry.</p>
 */
public final class JavaFxRenderer implements Renderer {

    private static final String DEFAULT_TITLE = "Slate Application";
    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;
    private static final boolean DEFAULT_RESIZABLE = true;

    private final JavaFxElementRegistry elementRegistry;
    private final JavaFxComponentMountManager componentMountManager;
    private final JavaFxRenderContext renderContext;

    /*
     * The current Slate lifecycle has one application window.
     *
     * The reference is intentionally kept inside the JavaFX backend so the
     * renderer can manage the window lifecycle without exposing JavaFX types
     * to slate.core.
     */
    private Stage applicationStage;
    private boolean mounted;

    public JavaFxRenderer() {
        this(new JavaFxElementRegistry());
    }

    public JavaFxRenderer(JavaFxElementRegistry elementRegistry) {
        if (elementRegistry == null) {
            throw new IllegalArgumentException("Element registry cannot be null");
        }

        this.elementRegistry = elementRegistry;

        this.componentMountManager = new JavaFxComponentMountManager();

        this.renderContext = new JavaFxRenderContext(elementRegistry, componentMountManager);
    }

    /**
     * Renders the resolved Slate application tree.
     *
     * <p>The current lifecycle intentionally supports one initial mount for
     * one application runtime. Re-rendering the whole application through
     * Renderer.render(...) is not yet part of Slate's reconciliation model.</p>
     */
    @Override
    public void render(ComponentTreeNode root) {

        if (root == null) {
            throw new IllegalArgumentException("Root render node cannot be null");
        }

        JavaFxRuntime.run(() -> mountWindow(root));
    }

    /**
     * Creates and displays Slate's native application window.
     *
     * <p>The Window node is special because it represents the native
     * application boundary. Other elements are delegated through the normal
     * element rendering pipeline.</p>
     */
    private void mountWindow(ComponentTreeNode root) {
        validateWindowRoot(root);

        if (mounted) {
            throw new IllegalStateException("Slate application window has already been mounted");
        }

        /*
         * Read the Window configuration from the resolved Slate tree before
         * creating the native Stage. This keeps the XML property model as the
         * source of truth rather than exposing JavaFX Stage configuration to
         * application code.
         */
        String title = JavaFxPropertySupport.getString(root, "title", DEFAULT_TITLE);

        double width = JavaFxPropertySupport.getDouble(root, "width", DEFAULT_WIDTH);

        double height = JavaFxPropertySupport.getDouble(root, "height", DEFAULT_HEIGHT);

        boolean resizable = JavaFxPropertySupport.getBoolean(root, "resizable", DEFAULT_RESIZABLE);

        validateWindowDimensions(width, height);

        Stage stage = new Stage();

        /*
         * The baseline View/Window layout currently uses VBox. This remains
         * an implementation detail of the desktop backend and can later be
         * replaced by proper Slate layout semantics.
         */
        VBox rootContainer = new VBox();

        for (ComponentTreeNode child : root.getChildren()) {

            Node nativeNode = renderContext.render(child);

            if (nativeNode != null) {
                rootContainer.getChildren().add(nativeNode);
            }
        }

        Scene scene = new Scene(rootContainer, width, height);

        stage.setTitle(title);
        stage.setResizable(resizable);
        stage.setScene(scene);

        /*
         * Slate controls the application lifecycle. The close request is
         * consumed so JavaFX does not independently decide the toolkit
         * lifecycle before Slate has performed its shutdown operation.
         */
        stage.setOnCloseRequest(event -> {
            event.consume();
            JavaFxRuntime.shutdown();
        });

        applicationStage = stage;
        mounted = true;

        stage.show();
    }

    /**
     * Validates the special Window node before creating native UI.
     */
    private void validateWindowRoot(ComponentTreeNode root) {
        if (!root.isElement()) {
            throw new IllegalStateException("Application root must be an element");
        }

        if (!"window".equals(root.getType())) {
            throw new IllegalStateException("Application root must be <Window> but found <"
                        + root.getType()
                            + ">"
            );
        }
    }

    /**
     * Prevents invalid window dimensions from reaching the native backend.
     */
    private void validateWindowDimensions(double width, double height) {

        if (!Double.isFinite(width) || width <= 0) {
            throw new IllegalArgumentException("Window width must be a positive finite number: " + width);
        }

        if (!Double.isFinite(height) || height <= 0) {
            throw new IllegalArgumentException("Window height must be a positive finite number: " + height);
        }
    }


    public JavaFxElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    /**
     * Returns the backend-owned component mount registry.
     */
    public JavaFxComponentMountManager getComponentMountManager() {
        return componentMountManager;
    }

    /**
     * Returns the backend-owned application stage.
     *
     * <p>This method is intentionally backend-only. It must not be exposed
     * through slate.core.</p>
     */
    public Stage getApplicationStage() {
        return applicationStage;
    }

    /**
     * ServiceLoader provider for the JavaFX renderer backend.
     */
    public static final class Provider implements RendererProvider {

        @Override
        public Renderer create() {
            return new JavaFxRenderer();
        }
    }
}