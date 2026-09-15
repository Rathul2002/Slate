package slate.desktop.javafx.elements.TextElement;

import slate.core.SlateElementProvider;

import java.util.Set;

public class TextElementProvider implements SlateElementProvider {
    @Override
    public Set<String> getElementTypes() {
        return Set.of("text");
    }
}
