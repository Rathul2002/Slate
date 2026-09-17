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
 * <p>The renderer owns the native application window for the current Slate
 * application lifecycle. {@link JavaFxRuntime} owns the JavaFX toolkit
 * lifecycle.</p>
 *
 * <p>Individual Slate elements are delegated to {@link JavaFxRenderContext}
 * and {@link JavaFxElementRegistry} so that the renderer remains a lifecycle
 * coordinator rather than an element-specific renderer.</p>
 */
public final class JavaFxRenderer implements Renderer {

    private final JavaFxElementRegistry elementRegistry;
    private final JavaFxComponentMountManager componentMountManager;
    private final JavaFxRenderContext renderContext;

    /*
     * The current application model supports one native application window
     * for one Slate runtime lifecycle.
     *
     * These references belong only to the JavaFX backend and therefore do not
     * leak JavaFX types into slate.core.
     */
    private Stage applicationStage;
    private boolean mounted;

    /**
     * Creates a JavaFX renderer with the standard discovered element
     * renderers.
     */
    public JavaFxRenderer() {
        this(new JavaFxElementRegistry());
    }

    /**
     * Creates a renderer using the supplied element registry.
     *
     * @param elementRegistry registry containing JavaFX element renderers
     */
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
         * Convert the resolved Window properties into one validated immutable
         * backend configuration before creating native UI.
         */
        JavaFxWindowConfiguration configuration = JavaFxWindowConfiguration.from(root);

        Stage stage = new Stage();

        try {
            /*
             * The current backend uses VBox as the temporary root layout
             * container. Final layout semantics will eventually belong to
             * Slate/CSS rather than this backend implementation detail.
             */
            VBox rootContainer = new VBox();

            for (ComponentTreeNode child : root.getChildren()) {
                Node nativeNode = renderContext.render(child);

                if (nativeNode != null) {
                    rootContainer.getChildren().add(nativeNode);
                }
            }

            /*
             * The resolved Window dimensions are used to create the initial
             * native Scene size.
             */
            Scene scene = new Scene(
                    rootContainer,
                    configuration.getWidth(),
                    configuration.getHeight()
            );

            /*
             * Apply all static Window properties in one place.
             */
            configuration.applyTo(stage);

            stage.setScene(scene);

            /*
             * Slate explicitly owns application shutdown. The close request
             * is consumed so the backend can first release its own component
             * ownership records.
             */
            stage.setOnCloseRequest(event -> {
                event.consume();
                shutdownApplication();
            });

            /*
             * The renderer becomes the owner of the Stage only after the
             * native scene has been constructed successfully.
             */
            applicationStage = stage;
            mounted = true;

            /*
             * The native window must be visible before applying initial
             * maximized/fullscreen state so that the state belongs to the
             * actual displayed application window.
             */
            stage.show();
            configuration.applyInitialState(stage);

        } catch (RuntimeException | Error failure) {
            cleanupFailedMount(stage);
            throw failure;
        }
    }

    /**
     * Shuts down the current Slate application window lifecycle.
     *
     * <p>This method runs on the JavaFX application thread because it is
     * normally reached from a JavaFX Stage close event.</p>
     */
    private void shutdownApplication() {
        /*
         * Clearing backend component ownership is safe here because the
         * application lifecycle is ending. Reconciliation-level native
         * subtree detachment is still a later feature.
         */
        componentMountManager.clear();

        Stage stage = applicationStage;

        applicationStage = null;
        mounted = false;

        /*
         * JavaFxRuntime owns the actual JavaFX toolkit shutdown.
         */
        JavaFxRuntime.shutdown();

        /*
         * The Stage normally disappears as part of Platform.exit().
         * Keeping the local reference only for the shutdown operation avoids
         * exposing native window ownership beyond this renderer.
         */
        if (stage != null && stage.isShowing()) {
            stage.hide();
        }
    }

    /**
     * Releases backend ownership when application mounting fails before the
     * lifecycle becomes fully established.
     */
    private void cleanupFailedMount(Stage stage) {
        componentMountManager.clear();

        if (applicationStage == stage) {
            applicationStage = null;
            mounted = false;
        }

        if (stage.isShowing()) {
            stage.hide();
        }
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
     * Indicates whether this renderer currently owns a mounted application
     * window.
     */
    public boolean isMounted() {
        return mounted;
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