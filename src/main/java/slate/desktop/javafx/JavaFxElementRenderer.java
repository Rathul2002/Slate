package slate.desktop.javafx;

import javafx.scene.Node;
import slate.core.ComponentTreeNode;

/**
 * JavaFX implementation of one Slate element.
 *
 * <p>Each concrete implementation represents one element type, such as
 * "text", "button" or "view". Keeping this contract small allows new
 * elements to be added independently without modifying JavaFxRenderer.</p>
 */
public interface JavaFxElementRenderer {

    /**
     * Returns the Slate element name handled by this implementation.
     */
    String getElementType();

    /**
     * Creates the native JavaFX node for a resolved Slate element.
     */
    Node render(ComponentTreeNode node, JavaFxRenderContext context);
}