package slate.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes the optional initialization hook on the application root.
 *
 * <p>The root class may declare a public zero-argument {@code onInit()}
 * method. The method is intentionally optional so applications that do not
 * need startup logic require no additional code.</p>
 *
 * <p>This class contains only root lifecycle dispatch. It does not construct
 * UI and does not depend on JavaFX.</p>
 */
public final class RootInitializer {

    public static void initialize(
            Object rootInstance
    ) {
        if (rootInstance == null) {
            throw new IllegalArgumentException("Root instance cannot be null");
        }

        Method method;

        try {
            method = rootInstance.getClass().getMethod("onInit");

        } catch (NoSuchMethodException e) {
            /*
             * Initialization is optional. A root without onInit() is valid.
             */
            return;
        }

        if (method.getParameterCount() != 0) {
            throw new IllegalStateException("@Root initialization method onInit() " + "must not accept parameters: "
                    + rootInstance.getClass().getName());
        }

        if (method.getReturnType() != void.class) {
            throw new IllegalStateException("@Root initialization method onInit() " + "must return void: "
                            + rootInstance.getClass().getName());
        }

        try {

            method.invoke(rootInstance);

        } catch (IllegalAccessException e) {

            throw new IllegalStateException("Cannot access @Root initialization method onInit() in "
                            + rootInstance.getClass().getName(), e);

        } catch (InvocationTargetException e) {

            Throwable cause = e.getCause() != null ? e.getCause() : e;

            throw new IllegalStateException("@Root initialization failed in " + rootInstance.getClass().getName(), cause);
        }
    }
}