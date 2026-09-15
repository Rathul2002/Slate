package slate.desktop.javafx.elements.ButtonElement;

import slate.core.SlateElementProvider;

import java.util.Set;

public class ButtonElementProvider implements SlateElementProvider {
    @Override
    public Set<String> getElementTypes() {
        return Set.of("button");
    }
}
