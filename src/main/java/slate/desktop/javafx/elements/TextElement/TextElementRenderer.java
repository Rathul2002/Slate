package slate.desktop.javafx.elements.TextElement;

import javafx.scene.Node;
import javafx.scene.control.Label;
import slate.core.ComponentTreeNode;
import slate.desktop.javafx.JavaFxElementRenderer;
import slate.desktop.javafx.JavaFxPropertySupport;
import slate.desktop.javafx.JavaFxRenderContext;

/**
 * JavaFX implementation of the Slate <Text> element.
 *
 * <p>The static baseline supports a text property and direct text children.
 * Rich text, styling and reactive updates are future features.</p>
 */
public final class TextElementRenderer implements JavaFxElementRenderer {

    @Override
    public String getElementType() {
        return "text";
    }

    @Override
    public Node render(ComponentTreeNode node, JavaFxRenderContext context) {
        Label label = new Label();

        String textProperty = JavaFxPropertySupport.getString(node, "text");

        if (textProperty != null) {
            label.setText(textProperty);
        } else {
            label.setText(collectText(node));
        }

        return label;
    }

    /**
     * Supports the child-text form:
     *
     * <pre>
     * {@code
     * <Text>
     *     Hello Slate
     * </Text>
     * }
     * </pre>
     */
    private String collectText(ComponentTreeNode node) {
        StringBuilder text = new StringBuilder();

        for (ComponentTreeNode child : node.getChildren()) {

            if (!child.isText()) {
                throw new IllegalStateException("<Text> can currently contain only text nodes");
            }

            if (text.length() > 0) {
                text.append(" ");
            }

            text.append(child.getText());
        }

        return text.toString();
    }
}