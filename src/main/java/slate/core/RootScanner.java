package slate.core;

import slate.core.annotation.Root;

import java.util.List;

public class RootScanner {

    private final ClasspathScanner classpathScanner;

    public RootScanner() {
        this.classpathScanner = new ClasspathScanner();
    }

    public Class<?> findRoot(Class<?> applicationClass) {

        List<Class<?>> classes = classpathScanner.findClasses(applicationClass);

        Class<?> rootClass = null;

        for (Class<?> clazz : classes) {

            if (!clazz.isAnnotationPresent(Root.class)) {
                continue;
            }

            if (rootClass != null) {
                throw new IllegalStateException("Multiple @Root classes found");
            }

            rootClass = clazz;
        }

        if (rootClass == null) {
            throw new IllegalStateException("No class annotated with @Root found in package: "
                    + applicationClass.getPackage().getName());
        }

        return rootClass;
    }
}