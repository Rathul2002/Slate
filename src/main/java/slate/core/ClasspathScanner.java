package slate.core;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanner {

    public List<Class<?>> findClasses(Class<?> referenceClass) {

        Package referencePackage = referenceClass.getPackage();

        if (referencePackage == null) {
            throw new IllegalStateException("Class must belong to a package");
        }

        String packageName = referencePackage.getName();

        String packagePath = packageName.replace('.', '/');

        ClassLoader classLoader = referenceClass.getClassLoader();

        List<Class<?>> classes = new ArrayList<>();

        try {

            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {

                URL resource = resources.nextElement();

                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {

                    File directory = new File(new URI(resource.toString()));

                    findClassesFromDirectory(
                            directory,
                            packageName,
                            classLoader,
                            classes
                    );

                } else if ("jar".equals(protocol)) {

                    findClassesFromJar(
                            resource,
                            packageName,
                            packagePath,
                            classLoader,
                            classes
                    );
                }
            }

            return classes;

        } catch (Exception e) {

            throw new IllegalStateException("Failed to scan classpath", e);
        }
    }

    private void findClassesFromDirectory(
            File directory,
            String packageName,
            ClassLoader classLoader,
            List<Class<?>> classes
    ) {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                findClassesFromDirectory(
                        file,
                        packageName + "." + file.getName(),
                        classLoader,
                        classes
                );

            } else if (file.getName().endsWith(".class")) {

                String className = file.getName().substring(0, file.getName().length() - 6);

                loadClass(packageName + "." + className, classLoader, classes);
            }
        }
    }

    private void findClassesFromJar(
            URL resource,
            String packageName,
            String packagePath,
            ClassLoader classLoader,
            List<Class<?>> classes
    ) throws IOException {

        JarURLConnection connection = (JarURLConnection) resource.openConnection();

        try (JarFile jarFile = connection.getJarFile()) {

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();

                String name = entry.getName();

                if (entry.isDirectory()) {
                    continue;
                }

                if (!name.endsWith(".class")) {
                    continue;
                }

                if (!name.startsWith(packagePath + "/")) {
                    continue;
                }

                String className = name.substring(0, name.length() - 6).replace('/', '.');

                loadClass(className, classLoader, classes);
            }
        }
    }

    private void loadClass(
            String className,
            ClassLoader classLoader,
            List<Class<?>> classes
    ) {

        try {

            Class<?> clazz = Class.forName(className, false, classLoader);

            classes.add(clazz);

        } catch (ClassNotFoundException e) {

            throw new IllegalStateException("Could not load class: " + className, e);
        }
    }
}