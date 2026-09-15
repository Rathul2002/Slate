package slate.desktop.javafx.elements.ButtonElement;

import javafx.scene.Node;
import javafx.scene.control.Button;
import slate.core.ComponentTreeNode;
import slate.desktop.javafx.JavaFxElementRenderer;
import slate.desktop.javafx.JavaFxPropertySupport;
import slate.desktop.javafx.JavaFxRenderContext;

/**
 * JavaFX implementation of the Slate <Button> element.
 *
 * <p>The current implementation renders only static button text. Event
 * binding will later be added through Slate's event system rather than by
 * exposing JavaFX event APIs to application code.</p>
 */
public final class ButtonElementRenderer implements JavaFxElementRenderer {

    @Override
    public String getElementType() {
        return "button";
    }

    @Override
    public Node render(ComponentTreeNode node, JavaFxRenderContext context) {
        Button button = new Button();

        String textProperty = JavaFxPropertySupport.getString(node, "text");

        if (textProperty != null) {
            button.setText(textProperty);
        } else {
            button.setText(collectText(node));
        }

        return button;
    }

    private String collectText(ComponentTreeNode node) {
        StringBuilder text = new StringBuilder();

        for (ComponentTreeNode child : node.getChildren()) {

            if (!child.isText()) {
                throw new IllegalStateException("<Button> can currently contain only text nodes");
            }

            if (text.length() > 0) {
                text.append(" ");
            }

            text.append(child.getText());
        }

        return text.toString();
    }
}