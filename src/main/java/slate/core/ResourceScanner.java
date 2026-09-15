package slate.core;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Discovers XML resources belonging to the application artifact.
 *
 * <p>The scanner intentionally avoids {@code java.class.path}. For packaged
 * applications the application {@code @SlateApp} class identifies its own
 * code source, allowing resources to be discovered from either an exploded
 * classes directory or the application's JAR.</p>
 */
public class ResourceScanner {

    public List<String> findXmlResources(
            Class<?> referenceClass
    ) {

        if (referenceClass == null) {
            throw new IllegalArgumentException("Reference class cannot be null");
        }

        ClassLoader classLoader = referenceClass.getClassLoader();

        Set<String> resources = new HashSet<>();

        try {

            boolean scannedApplicationArtifact = scanApplicationArtifact(referenceClass, resources);

            /*
             * Some IDE or custom class-loader environments may not expose a
             * useful CodeSource. In that case inspect class-loader file roots.
             */
            if (!scannedApplicationArtifact) {

                Enumeration<URL> urls = classLoader.getResources("");

                while (urls.hasMoreElements()) {

                    URL url = urls.nextElement();

                    if ("file".equals(url.getProtocol())) {

                        File directory = new File(new URI(url.toString()));

                        findFromDirectory(directory, "", resources);
                    }
                }
            }

            /*
             * Retain the development-project resource directory fallback used
             * by the current source-based workflow.
             */
            File resourcesDirectory = new File("resources");

            if (resourcesDirectory.exists() && resourcesDirectory.isDirectory()) {

                findFromDirectory(resourcesDirectory, "", resources);
            }

            List<String> result = new ArrayList<>(resources);

            result.sort(String::compareTo);

            return result;

        } catch (Exception e) {

            throw new IllegalStateException("Failed to scan XML resources", e);
        }
    }

    /**
     * Scans the code source containing the application class.
     *
     * @return true when a usable application artifact was discovered
     */
    private boolean scanApplicationArtifact(Class<?> referenceClass, Set<String> resources) {

        try {

            CodeSource codeSource = referenceClass.getProtectionDomain().getCodeSource();

            if (codeSource == null || codeSource.getLocation() == null) {
                return false;
            }

            URL location = codeSource.getLocation();

            if ("file".equals(location.getProtocol())) {

                File file = new File(location.toURI());

                /*
                 * Exploded application/classes directory.
                 */
                if (file.isDirectory()) {

                    findFromDirectory(file, "", resources);

                    return true;
                }

                /*
                 * Packaged application JAR.
                 */
                if (file.isFile() && file.getName().endsWith(".jar")) {

                    scanJar(file, resources);

                    return true;
                }
            }

        } catch (Exception ignored) {
            /*
             * Fall back to class-loader based discovery.
             */
        }

        return false;
    }

    private void findFromDirectory(File directory, String path, Set<String> resources) {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            String resourcePath = path.isEmpty() ? file.getName() : path + "/" + file.getName();

            if (file.isDirectory()) {

                findFromDirectory(file, resourcePath, resources);

            } else if (file.getName().endsWith(".xml")) {

                resources.add(resourcePath);
            }
        }
    }

    private void scanJar(File jarFile, Set<String> resources) {

        try (JarFile jar = new JarFile(jarFile)) {

            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();

                if (name.endsWith(".xml")) {
                    resources.add(name);
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan JAR: " + jarFile.getAbsolutePath(), e);
        }
    }
}