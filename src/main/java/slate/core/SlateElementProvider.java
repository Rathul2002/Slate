package slate.core;

import java.util.Set;

/**
 * Describes Slate element names contributed to the Slate language.
 *
 * <p>This interface belongs to the core runtime because element validity is
 * independent of the native backend. A provider declares which XML element
 * names are understood by Slate.</p>
 *
 * <p>Rendering is deliberately not part of this contract. A separate backend
 * such as JavaFX may provide the native implementation through its own
 * extension mechanism.</p>
 */
public interface SlateElementProvider {

    /**
     * Returns the Slate element names contributed by this provider.
     *
     * <p>Names are normalized by the registry, so providers may use either
     * upper-case or lower-case XML names.</p>
     */
    Set<String> getElementTypes();
}