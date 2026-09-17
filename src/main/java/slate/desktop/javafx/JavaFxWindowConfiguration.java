package slate.desktop.javafx;

import javafx.stage.Stage;
import slate.core.ComponentTreeNode;

/**
 * Immutable semantic configuration for the Slate Window element.
 *
 * <p>This class translates static Window properties from the runtime tree into
 * validated values understood by the JavaFX backend.</p>
 *
 * <p>The configuration represents initial Window setup. It does not represent
 * future reactive Window state, which will belong to Slate's later runtime
 * state and reconciliation systems.</p>
 */
final class JavaFxWindowConfiguration {

    private static final String DEFAULT_TITLE = "Slate Application";

    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;

    private static final boolean DEFAULT_RESIZABLE = true;

    private static final double DEFAULT_MIN_WIDTH = 0;
    private static final double DEFAULT_MIN_HEIGHT = 0;

    private static final double DEFAULT_MAX_WIDTH = Double.MAX_VALUE;
    private static final double DEFAULT_MAX_HEIGHT = Double.MAX_VALUE;

    private static final boolean DEFAULT_MAXIMIZED = false;
    private static final boolean DEFAULT_FULLSCREEN = false;

    private final String title;

    private final double width;
    private final double height;

    private final boolean resizable;

    private final double minWidth;
    private final double minHeight;

    private final double maxWidth;
    private final double maxHeight;

    private final boolean maximized;
    private final boolean fullscreen;

    private JavaFxWindowConfiguration(
            String title,
            double width,
            double height,
            boolean resizable,
            double minWidth,
            double minHeight,
            double maxWidth,
            double maxHeight,
            boolean maximized,
            boolean fullscreen
    ) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.resizable = resizable;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maximized = maximized;
        this.fullscreen = fullscreen;
    }

    /**
     * Reads and validates the static properties of a Window node.
     *
     * <p>All XML values are still represented by the runtime property map,
     * while this class is responsible for converting them into validated
     * native configuration values.</p>
     */
    static JavaFxWindowConfiguration from(ComponentTreeNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Window node cannot be null");
        }

        String title = JavaFxPropertySupport.getString(
                node,
                "title",
                DEFAULT_TITLE
        );

        double width = JavaFxPropertySupport.getDouble(
                node,
                "width",
                DEFAULT_WIDTH
        );

        double height = JavaFxPropertySupport.getDouble(
                node,
                "height",
                DEFAULT_HEIGHT
        );

        boolean resizable = JavaFxPropertySupport.getBoolean(
                node,
                "resizable",
                DEFAULT_RESIZABLE
        );

        double minWidth = JavaFxPropertySupport.getDouble(
                node,
                "minWidth",
                DEFAULT_MIN_WIDTH
        );

        double minHeight = JavaFxPropertySupport.getDouble(
                node,
                "minHeight",
                DEFAULT_MIN_HEIGHT
        );

        double maxWidth = JavaFxPropertySupport.getDouble(
                node,
                "maxWidth",
                DEFAULT_MAX_WIDTH
        );

        double maxHeight = JavaFxPropertySupport.getDouble(
                node,
                "maxHeight",
                DEFAULT_MAX_HEIGHT
        );

        boolean maximized = JavaFxPropertySupport.getBoolean(
                node,
                "maximized",
                DEFAULT_MAXIMIZED
        );

        boolean fullscreen = JavaFxPropertySupport.getBoolean(
                node,
                "fullscreen",
                DEFAULT_FULLSCREEN
        );

        validateWindowRelationships(
                node,
                width,
                height,
                minWidth,
                minHeight,
                maxWidth,
                maxHeight,
                maximized,
                fullscreen
        );

        return new JavaFxWindowConfiguration(
                title,
                width,
                height,
                resizable,
                minWidth,
                minHeight,
                maxWidth,
                maxHeight,
                maximized,
                fullscreen
        );
    }

    /**
     * Applies the normal Stage configuration.
     *
     * <p>The initial maximized/fullscreen state is intentionally handled
     * separately because it represents a native Window state transition
     * rather than a normal Stage property.</p>
     */
    void applyTo(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }

        stage.setTitle(title);
        stage.setResizable(resizable);

        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);

        stage.setMaxWidth(maxWidth);
        stage.setMaxHeight(maxHeight);
    }

    /**
     * Applies the initial native Window state after the Stage has been shown.
     *
     * <p>Fullscreen takes precedence at the native lifecycle level because it
     * is a stronger Window presentation state than maximized mode.</p>
     */
    void applyInitialState(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }

        if (fullscreen) {
            stage.setFullScreen(true);
            return;
        }

        if (maximized) {
            stage.setMaximized(true);
        }
    }

    double getWidth() {
        return width;
    }

    double getHeight() {
        return height;
    }

    private static void validateWindowRelationships(
            ComponentTreeNode node,
            double width,
            double height,
            double minWidth,
            double minHeight,
            double maxWidth,
            double maxHeight,
            boolean maximized,
            boolean fullscreen
    ) {

        if (minWidth > maxWidth) {
            throw invalidWindowProperty(
                    node,
                    "minWidth",
                    minWidth,
                    "cannot be greater than maxWidth (" + maxWidth + ")"
            );
        }

        if (minHeight > maxHeight) {
            throw invalidWindowProperty(
                    node,
                    "minHeight",
                    minHeight,
                    "cannot be greater than maxHeight (" + maxHeight + ")"
            );
        }

        if (width < minWidth || width > maxWidth) {
            throw invalidWindowProperty(
                    node,
                    "width",
                    width,
                    "must be between minWidth ("
                            + minWidth
                            + ") and maxWidth ("
                            + maxWidth
                            + ")"
            );
        }

        if (height < minHeight || height > maxHeight) {
            throw invalidWindowProperty(
                    node,
                    "height",
                    height,
                    "must be between minHeight ("
                            + minHeight
                            + ") and maxHeight ("
                            + maxHeight
                            + ")"
            );
        }

        if (maximized && fullscreen) {
            throw new IllegalArgumentException(
                    "Invalid Window configuration: "
                            + "<window> cannot be both "
                            + "maximized and fullscreen"
            );
        }
    }

    private static IllegalArgumentException invalidWindowProperty(
            ComponentTreeNode node,
            String property,
            Object value,
            String reason
    ) {
        return new IllegalArgumentException(
                "Invalid property '"
                        + property
                        + "' on <"
                        + node.getType()
                        + ">: '"
                        + value
                        + "'; "
                        + reason
        );
    }
}