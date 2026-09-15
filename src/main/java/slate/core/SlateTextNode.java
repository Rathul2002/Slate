package slate.core;

import java.util.Collections;

public final class SlateTextNode extends SlateNode {

    public SlateTextNode(String text) {
        super(
                Kind.TEXT,
                null,
                validateText(text),
                Collections.emptyMap(),
                Collections.emptyList()
        );
    }

    private static String validateText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        return text;
    }
}