package slate.core;

import slate.core.renderer.Renderer;
import slate.core.renderer.RendererProvider;

import java.util.ServiceLoader;

/**
 * Owns the runtime objects belonging to one running Slate application.
 *
 * <p>The context connects application discovery, resources, component
 * resolution, tree construction and renderer selection. It deliberately does
 * not depend on any concrete UI backend.</p>
 */
public class ApplicationContext {

    private final Class<?> applicationClass;
    private final Class<?> rootClass;

    private final ResourceLoader resourceLoader;
    private final XmlParser xmlParser;

    private final ComponentRegistry componentRegistry;
    private final ComponentResolver componentResolver;
    private final ComponentLoader componentLoader;
    private final ComponentTreeBuilder componentTreeBuilder;

    private final Renderer renderer;

    private Object rootInstance;
    private SlateNode windowNode;
    private ComponentTreeNode componentTree;

    // Stores it when the context is created
    public ApplicationContext(Class<?> applicationClass) {

        if (applicationClass == null) {
            throw new IllegalArgumentException("Application class cannot be null");
        }

        this.applicationClass = applicationClass;
        /*
         * Resource loading remains in core and is backend-independent.
         * ApplicationContext supplies the application class whenever a
         * resource is requested so resources remain owned by the application
         * artifact.
         */
        this.resourceLoader = new ResourceLoader();
        this.xmlParser = new XmlParser();

        /*
         * @Root identifies the Java owner of the application. It is not an
         * XML component and does not require Root.xml.
         */
        RootScanner rootScanner = new RootScanner();
        this.rootClass = rootScanner.findRoot(applicationClass);

        /*
         * The registry is created before the resolver because the resolver
         * now resolves component names through registered definitions.
         * Component infrastructure:
         * Registry
         * ↓
         * Resolver
         * ↓
         * Loader
         * ↓
         * Component Tree Builder
         */
        this.componentRegistry = new ComponentRegistry();
        ComponentScanner componentScanner = new ComponentScanner();

        componentScanner.scan(applicationClass, componentRegistry);

        /* Component resolution pipeline. */
        this.componentResolver = new ComponentResolver(componentRegistry);
        this.componentLoader = new ComponentLoader(componentResolver);
        this.componentTreeBuilder = new ComponentTreeBuilder(componentResolver);

        /*
         * Discover the platform renderer.
         * ApplicationContext does not know whether the
         * renderer is JavaFX, Android, or something else.
         */
        this.renderer = createRenderer();
    }

    /**
     * Starts the Slate application runtime.
     *
     * <p>The root initialization hook is executed before Window.xml is
     * loaded. This gives application code a place to initialize services,
     * configuration and other non-UI infrastructure.</p>
     */
    public void start() {

        if (rootInstance != null) {
            throw new IllegalStateException("Application context is already started");
        }

        // Creates the Root class objects without knowing the name of the class during
        // runtime
        // Because Slate should manage application objects automatically
        try {
            rootInstance = rootClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create @Root instance", e);
        }

        /*
         * Application initialization happens before UI loading. The root
         * remains an application-level object rather than becoming a UI
         * controller.
         */
        RootInitializer.initialize(rootInstance);

        /*
         * Window.xml is special.
         *
         * Window.xml belongs to the application and must be loaded through the
         * application resource owner rather than the framework class loader.
         */
        try (var stream = resourceLoader.load(applicationClass, "Window.xml")) {
            SlateNode parsedWindow = xmlParser.parse(stream);

            if (!"Window".equalsIgnoreCase(parsedWindow.getType())) {

                throw new IllegalStateException("Invalid Window.xml root element: "
                        + "expected <Window> but found <"
                        + parsedWindow.getType()
                        + ">");
            }

            windowNode = parsedWindow;

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Window.xml", e);
        }

        /*
         * XML is converted into the resolved runtime tree only after the
         * application bootstrap phase has succeeded.
         */
        componentTree = componentTreeBuilder.build(windowNode);

        // The renderer implementation is discovered
        // independently of this class.
        renderer.render(componentTree);
    }

    /**
     * Discovers exactly one renderer backend through ServiceLoader.
     *
     * <p>The backend JAR owns the provider metadata. Application developers
     * do not manually register renderer providers.</p>
     */
    private Renderer createRenderer() {
        ServiceLoader<RendererProvider> loader = ServiceLoader.load(RendererProvider.class);
        RendererProvider selectedProvider = null;
        for (RendererProvider provider : loader) {
            if (selectedProvider != null) {
                throw new IllegalStateException("Multiple Slate renderer providers found");
            }
            selectedProvider = provider;
        }
        if (selectedProvider == null) {
            throw new IllegalStateException("No Slate renderer implementation found");
        }
        return selectedProvider.create();
    }

    public Class<?> getApplicationClass() {
        return applicationClass;
    }

    public Class<?> getRootClass() {
        return rootClass;
    }

    public Object getRootInstance() {
        return rootInstance;
    }

    public SlateNode getWindowXml() {
        return windowNode;
    }

    public ComponentTreeNode getComponentTree() {
        return componentTree;
    }

    public ComponentResolver getComponentResolver() {
        return componentResolver;
    }

    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }

    public ComponentLoader getComponentLoader() {
        return componentLoader;
    }

    public ComponentTreeBuilder getComponentTreeBuilder() {
        return componentTreeBuilder;
    }

    public Renderer getRenderer() {
        return renderer;
    }

}
