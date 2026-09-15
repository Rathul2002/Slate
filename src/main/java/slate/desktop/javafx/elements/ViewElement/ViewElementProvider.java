package slate.desktop.javafx.elements.ViewElement;

import slate.core.SlateElementProvider;

import java.util.Set;

public class ViewElementProvider implements SlateElementProvider {
    @Override
    public Set<String> getElementTypes() {
        return Set.of("view");
    }
}
