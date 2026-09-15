package slate.core;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Registry of element names understood by the Slate runtime.
 *
 * <p>The registry no longer hard-codes the available element names. Element
 * definitions are contributed by SlateElementProvider implementations and
 * discovered through Java's ServiceLoader mechanism.</p>
 *
 * <p>This class remains backend-independent. It knows which XML element names
 * are valid, but it does not know how those elements are rendered.</p>
 */
public final class BuiltInElementRegistry {

    private final Set<String> elementTypes = new LinkedHashSet<>();

    public BuiltInElementRegistry() {
        discoverProviders();

        if (elementTypes.isEmpty()) {
            throw new IllegalStateException("No Slate element providers were discovered");
        }
    }

    /**
     * Discovers all Slate element providers visible to the current runtime.
     */
    private void discoverProviders() {

        ServiceLoader<SlateElementProvider> loader = ServiceLoader.load(SlateElementProvider.class);

        boolean foundProvider = false;

        for (SlateElementProvider provider : loader) {

            foundProvider = true;

            Set<String> providedTypes = provider.getElementTypes();

            if (providedTypes == null) {
                throw new IllegalStateException("Slate element provider returned null element types: " + provider.getClass().getName());
            }

            for (String type : providedTypes) {
                register(type, provider);
            }
        }

        if (!foundProvider) {
            throw new IllegalStateException(
                    "No Slate element providers were discovered"
            );
        }
    }

    /**
     * Registers one element name contributed by a provider.
     */
    private void register(String type, SlateElementProvider provider) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Slate element provider returned a blank element name: "
                         + provider.getClass().getName());
        }

        String normalized = type.toLowerCase(Locale.ROOT);

        if (!elementTypes.add(normalized)) {
            throw new IllegalStateException("Duplicate Slate element type: " + normalized);
        }
    }

    /**
     * Resolves and normalizes a built-in element name.
     *
     * @param type XML element name
     * @return normalized lower-case element name
     */
    public String resolve(String type) {

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Element type cannot be null or empty");
        }

        String normalized = type.toLowerCase(Locale.ROOT);

        if (!elementTypes.contains(normalized)) {
            throw new IllegalStateException("Unknown Slate element: " + type);
        }

        return normalized;
    }

    public boolean contains(String type) {

        if (type == null || type.isBlank()) {
            return false;
        }

        return elementTypes.contains(type.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns a read-only view of all discovered element names.
     *
     * <p>This is primarily useful for diagnostics and future validation.</p>
     */
    public Set<String> getElementTypes() {
        return Set.copyOf(elementTypes);
    }
}