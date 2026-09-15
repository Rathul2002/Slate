package slate.desktop.javafx.elements.ViewElement;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import slate.core.ComponentTreeNode;
import slate.desktop.javafx.JavaFxElementRenderer;
import slate.desktop.javafx.JavaFxPropertySupport;
import slate.desktop.javafx.JavaFxRenderContext;

/**
 * JavaFX implementation of the Slate <View> element.
 *
 * <p>The current baseline uses VBox so sibling children are laid out
 * vertically instead of overlapping each other. Layout behavior can be
 * expanded later when Slate gains its own layout semantics.</p>
 */
public final class ViewElementRenderer implements JavaFxElementRenderer {

    @Override
    public String getElementType() {
        return "view";
    }

    @Override
    public Node render(ComponentTreeNode node, JavaFxRenderContext context) {
        VBox view = new VBox();

        applyProperties(node, view);

        for (ComponentTreeNode child : node.getChildren()) {

            Node childNode = context.render(child);

            if (childNode != null) {
                view.getChildren().add(childNode);
            }
        }

        return view;
    }

    /**
     * Applies the static layout properties supported by the current View
     * baseline.
     */
    private void applyProperties(
            ComponentTreeNode node,
            VBox view
    ) {
        double spacing = JavaFxPropertySupport.getDouble(node, "spacing", view.getSpacing());

        view.setSpacing(spacing);
    }
}