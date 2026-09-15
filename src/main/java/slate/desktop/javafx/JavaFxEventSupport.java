package slate.desktop.javafx;

import javafx.scene.control.Button;
import slate.core.ComponentInstance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides the first native event bridge for Slate's JavaFX backend.
 *
 * <p>This class intentionally implements only the initial Slate event use case:
 * a Button click mapped to a zero-argument method on the owning component's
 * behavior instance.</p>
 *
 * <p>Application code interacts with Slate XML and Java behavior rather than
 * directly attaching JavaFX listeners.</p>
 */
public final class JavaFxEventSupport {

    /**
     * Binds the Slate {@code onClick} property of a Button to the owning
     * component's behavior method.
     *
     * @param button the native JavaFX button
     * @param componentInstance the Slate component that owns the button
     * @param handlerName the Java behavior method name
     */
    public static void bindButtonClick(Button button, ComponentInstance componentInstance, String handlerName) {
        if (button == null) {
            throw new IllegalArgumentException("Button cannot be null");
        }

        if (componentInstance == null) {
            throw new IllegalArgumentException("Component instance cannot be null");
        }

        if (handlerName == null || handlerName.isBlank()) {
            throw new IllegalArgumentException("Click handler name cannot be null or empty");
        }

        if (!componentInstance.hasBehavior()) {
            throw new IllegalStateException("Button click handler '" + handlerName
                            + "' requires a behavior instance for component: "
                            + componentInstance.getName());
        }

        Object behavior = componentInstance.getBehaviorInstance();

        Method method = findHandlerMethod(behavior.getClass(), handlerName);

        button.setOnAction(event -> invokeHandler(
                method,
                behavior,
                componentInstance.getName(),
                handlerName
        ));
    }

    /**
     * Locates a zero-argument behavior method by name.
     *
     * <p>The first version intentionally requires a zero-argument method.
     * Event payload abstractions can be introduced later once the basic event
     * lifecycle is established.</p>
     */
    private static Method findHandlerMethod(Class<?> behaviorClass, String handlerName) {
        try {
            Method method = behaviorClass.getMethod(handlerName);

            if (method.getParameterCount() != 0) {
                throw new IllegalStateException("Click handler must have zero parameters: "
                                + behaviorClass.getName()
                                + "."
                                + handlerName);
            }

            return method;

        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No zero-argument click handler found: "
                            + behaviorClass.getName()
                            + "."
                            + handlerName, e);
        }
    }

    /**
     * Invokes the application behavior method and preserves useful runtime
     * failure information rather than silently swallowing exceptions.
     */
    private static void invokeHandler(
            Method method,
            Object behavior,
            String componentName,
            String handlerName
    ) {
        try {
            method.invoke(behavior);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access click handler '"
                            + handlerName
                            + "' on component: "
                            + componentName, e);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw new IllegalStateException("Click handler '"
                            + handlerName
                            + "' failed for component: "
                            + componentName, cause);
        }
    }
}