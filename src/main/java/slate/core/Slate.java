package slate.core;

import slate.core.annotation.SlateApp;

public final class Slate {

    private static boolean running;
    private static ApplicationContext context;

    public static void run(Class<?> applicationClass) {
        if (applicationClass == null) {
            throw new IllegalArgumentException("Application class cannot be null");
        }

        if (!applicationClass.isAnnotationPresent(SlateApp.class)) {
            throw new IllegalArgumentException("Application class must be annotated with @SlateApp");
        }

        if (running) {
            throw new IllegalStateException("Slate is already running");
        }

        context = new ApplicationContext(applicationClass);
        context.start();
        running = true;

        System.out.println("Slate application started: "
                + context.getApplicationClass().getName());

        System.out.println("Root application started: "
                + context.getRootClass().getName());

    }

    public static ApplicationContext getContext() {

        if (!running || context == null) {
            throw new IllegalStateException("Slate application is not running");
        }
        return context;
    }

}