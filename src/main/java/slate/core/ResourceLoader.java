package slate.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Loads application resources in a classloader-, module- and JAR-friendly way.
 *
 * <p>The resource owner is supplied explicitly so Slate can distinguish
 * application resources from framework resources. This is particularly
 * important once applications run as named Java modules.</p>
 *
 * <p>The lookup order is:</p>
 *
 *     Application module/class resource lookup.</li>
 *     Application class-loader lookup.</li>
 *     Application CodeSource directory/JAR lookup.</li>
 *     Development-time {@code resources/} directory fallback.</li>
 */
public class ResourceLoader {

    /**
     * Loads a resource belonging to the supplied application class.
     *
     * @param referenceClass class that identifies the application resource owner
     * @param resourceName resource path such as {@code Window.xml} or
     *                     {@code components/Hero.xml}
     * @return opened resource stream
     */
    public InputStream load(Class<?> referenceClass, String resourceName) {
        validateReferenceClass(referenceClass);
        validateResourceName(resourceName);

        /*
         * First use the application class itself.
         *
         * Class.getResourceAsStream(...) is important for named modules
         * because it performs resource lookup relative to the class's
         * defining module rather than relying only on the thread context
         * class loader.
         */
        InputStream resource = loadFromApplicationClass(referenceClass, resourceName);

        if (resource != null) {
            return resource;
        }

        /*
         * Second, preserve the normal class-loader path used by the
         * development and classpath-based execution models.
         */
        ClassLoader classLoader = referenceClass.getClassLoader();

        if (classLoader != null) {
            resource = classLoader.getResourceAsStream(normalizeResourcePath(resourceName));

            if (resource != null) {
                return resource;
            }
        }

        /*
         * Third, resolve directly against the application's CodeSource.
         *
         * This makes loading work when the application is running from:
         *
         *     target/classes
         *
         * or:
         *
         *     application.jar
         *
         * It also means the loader does not depend on java.class.path.
         */
        resource = loadFromCodeSource(referenceClass, resourceName);

        if (resource != null) {
            return resource;
        }

        /*
         * Final fallback for the existing source-development workflow.
         */
        resource = loadFromDevelopmentResources(resourceName);

        if (resource != null) {
            return resource;
        }

        throw new IllegalStateException("Resource not found: " + resourceName);
    }

    /**
     * Loads a resource through the application class/module.
     */
    private InputStream loadFromApplicationClass(Class<?> referenceClass, String resourceName) {
        String normalized = normalizeResourcePath(resourceName);

        /*
         * Absolute resource lookup makes the resource path independent from
         * the Java package of the reference class.
         */
        return referenceClass.getResourceAsStream("/" + normalized);
    }

    /**
     * Loads an application resource directly from its CodeSource.
     *
     * <p>This works for both exploded application directories and packaged
     * application JARs and therefore remains suitable for Slate's JAR-friendly
     * runtime.</p>
     */
    private InputStream loadFromCodeSource(Class<?> referenceClass, String resourceName) {
        try {
            CodeSource codeSource = referenceClass.getProtectionDomain().getCodeSource();

            if (codeSource == null || codeSource.getLocation() == null) {
                return null;
            }

            URL location = codeSource.getLocation();

            if (!"file".equalsIgnoreCase(location.getProtocol())) {
                return null;
            }

            File source = new File(location.toURI());

            String normalized = normalizeResourcePath(resourceName);

            /*
             * Exploded classes/resources directory.
             */
            if (source.isDirectory()) {

                File resourceFile = new File(source, normalized);

                if (resourceFile.isFile()) {
                    return new FileInputStream(resourceFile);
                }

                return null;
            }

            /*
             * Packaged application JAR.
             */
            if (source.isFile() && source.getName().endsWith(".jar")) {

                JarFile jarFile = new JarFile(source);

                ZipEntry entry = jarFile.getEntry(normalized);

                if (entry == null) {
                    jarFile.close();
                    return null;
                }

                InputStream stream = jarFile.getInputStream(entry);

                /*
                 * Closing the returned stream must also close the JarFile.
                 */
                return new JarResourceInputStream(jarFile, stream);
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads from the development project's local resources directory.
     *
     * <p>This fallback is intentionally last because production applications
     * should resolve resources from their application artifact.</p>
     */
    private InputStream loadFromDevelopmentResources(String resourceName) {
        File resourcesDirectory = new File("resources");

        File resourceFile = new File(resourcesDirectory, normalizeResourcePath(resourceName));

        if (!resourceFile.isFile()) {
            return null;
        }

        try {
            return new FileInputStream(resourceFile);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + resourceName, e);
        }
    }

    private String normalizeResourcePath(String resourceName) {
        String normalized = resourceName.replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Resource name cannot be empty");
        }

        return normalized;
    }

    private void validateReferenceClass(Class<?> referenceClass) {
        Objects.requireNonNull(referenceClass, "Reference class cannot be null");
    }

    private void validateResourceName(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) {
            throw new IllegalArgumentException("Resource name cannot be null or empty");
        }
    }

    /**
     * InputStream wrapper that closes both the resource stream and its
     * underlying JarFile.
     */
    private static final class JarResourceInputStream extends InputStream {

        private final JarFile jarFile;
        private final InputStream delegate;

        private JarResourceInputStream(JarFile jarFile, InputStream delegate) {
            this.jarFile = jarFile;
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            return delegate.read(buffer);
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            return delegate.read(buffer, offset, length);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                jarFile.close();
            }
        }
    }
}