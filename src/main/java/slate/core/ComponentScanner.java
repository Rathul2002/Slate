package slate.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discovers reusable Slate XML components and their optional Java behavior
 * classes.
 *
 * <p>Component identity remains locked to the XML filename. The scanner only
 * creates shared {@link ComponentDefinition} objects; runtime component
 * instances are created later by the component tree builder.</p>
 */
public class ComponentScanner {

    private final ClasspathScanner classpathScanner;
    private final ResourceScanner resourceScanner;
    private final ResourceLoader resourceLoader;
    private final XmlParser xmlParser;

    public ComponentScanner() {
        this.classpathScanner = new ClasspathScanner();
        this.resourceScanner = new ResourceScanner();
        this.resourceLoader = new ResourceLoader();
        this.xmlParser = new XmlParser();
    }

    /**
     * Scans the application artifact for component XML resources and optional
     * Java behavior classes.
     */
    public void scan(Class<?> applicationClass, ComponentRegistry registry) {

        if (applicationClass == null) {
            throw new IllegalArgumentException("Application class cannot be null");
        }

        if (registry == null) {
            throw new IllegalArgumentException("Component registry cannot be null");
        }

        Map<String, Class<?>> javaClasses = findJavaClasses(applicationClass);

        List<String> xmlResources = resourceScanner.findXmlResources(applicationClass);

        for (String resourcePath : xmlResources) {

            String componentName = getComponentName(resourcePath);

            /*
             * Window.xml is application bootstrap UI rather than a reusable
             * component definition.
             */
            if ("Window".equals(componentName)) {
                continue;
            }

            SlateNode root = loadComponentRoot(applicationClass, resourcePath);

            Class<?> behaviorClass = javaClasses.get(componentName);

            ComponentDefinition definition = new ComponentDefinition(
                    componentName,
                    resourcePath,
                    root,
                    behaviorClass
            );

            registry.register(definition);
        }
    }

    private Map<String, Class<?>> findJavaClasses(Class<?> applicationClass) {

        List<Class<?>> classes = classpathScanner.findClasses(applicationClass);

        Map<String, Class<?>> javaClasses = new HashMap<>();

        for (Class<?> clazz : classes) {

            String className = clazz.getSimpleName();

            if (javaClasses.containsKey(className)) {
                throw new IllegalStateException("Multiple Java classes found with component name: " + className);
            }

            javaClasses.put(className, clazz);
        }

        return javaClasses;
    }

    /**
     * Loads component XML from the artifact belonging to the application.
     *
     * <p>The application class is passed to ResourceLoader so named-module
     * execution and packaged JAR execution use the application's resource
     * owner rather than the framework's thread context class loader.</p>
     */
    private SlateNode loadComponentRoot(Class<?> applicationClass, String resourcePath) {

        try (InputStream stream = resourceLoader.load(applicationClass, resourcePath)) {

            return xmlParser.parse(stream);

        } catch (Exception e) {

            throw new IllegalStateException("Failed to load component XML: " + resourcePath, e);
        }
    }

    /**
     * Derives component identity from the XML filename.
     *
     * <p>For example:</p>
     *
     * components/Hero.xml -> Hero
     */
    private String getComponentName(String resource) {

        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Component resource path cannot be null or empty");
        }

        if (!resource.endsWith(".xml")) {
            throw new IllegalArgumentException("Component resource must be an XML file: " + resource);
        }

        int lastSlash = resource.lastIndexOf('/');

        String fileName = lastSlash >= 0 ? resource.substring(lastSlash + 1) : resource;

        return fileName.substring(0, fileName.length() - 4);
    }
}