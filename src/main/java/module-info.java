module io.github.rathul.slate {

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.xml;

    uses slate.core.SlateElementProvider;
    uses slate.core.renderer.RendererProvider;
    uses slate.desktop.javafx.JavaFxElementRenderer;

    exports slate.core;
    exports slate.core.annotation;
    exports slate.core.renderer;
    exports slate.desktop.javafx;


    provides slate.core.renderer.RendererProvider
            with slate.desktop.javafx.JavaFxRenderer.Provider;

    provides slate.core.SlateElementProvider
            with slate.desktop.javafx.elements.WindowElement.WindowElementProvider,
                    slate.desktop.javafx.elements.ButtonElement.ButtonElementProvider,
                    slate.desktop.javafx.elements.TextElement.TextElementProvider,
                    slate.desktop.javafx.elements.ViewElement.ViewElementProvider;

    provides slate.desktop.javafx.JavaFxElementRenderer
            with slate.desktop.javafx.elements.ViewElement.ViewElementRenderer,
                    slate.desktop.javafx.elements.TextElement.TextElementRenderer,
                    slate.desktop.javafx.elements.ButtonElement.ButtonElementRenderer;
}