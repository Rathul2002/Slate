package slate.desktop.javafx.elements.WindowElement;

import slate.core.SlateElementProvider;

import java.util.Set;

/**
 * Contributes the Window element to the Slate language.
 *
 * <p>The Window element is the special application root used by
 * Window.xml. Its native implementation is handled by JavaFxRenderer
 * because Window represents the native application boundary.</p>
 */
public final class WindowElementProvider implements SlateElementProvider {

    @Override
    public Set<String> getElementTypes() {
        return Set.of("window");
    }
}