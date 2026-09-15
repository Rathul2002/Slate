package slate.desktop.javafx;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns the JavaFX application runtime lifecycle for Slate's desktop backend.
 *
 * <p>This class is intentionally backend-only. It must never be referenced
 * from slate-core.</p>
 *
 * <p>The runtime owns the JavaFX toolkit lifecycle and provides a synchronous
 * bridge for executing backend operations on the JavaFX application thread.</p>
 *
 * <p>The current Slate runtime supports one application lifecycle. Once the
 * JavaFX toolkit has been shut down, it cannot be restarted by Slate.</p>
 */
public final class JavaFxRuntime {

    private static final Object RUNTIME_LOCK = new Object();

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean(false);

    /**
     * Ensures that the JavaFX toolkit has been initialized.
     *
     * <p>JavaFX is initialized once for the lifetime of the Slate process.
     * After shutdown, initialization is rejected rather than attempting to
     * restart a JavaFX toolkit that has already exited.</p>
     */
    public static void initialize() {

        if (SHUTDOWN.get()) {
            throw new IllegalStateException("JavaFX runtime has already been shut down");
        }

        if (Platform.isFxApplicationThread()) {
            STARTED.set(true);
            Platform.setImplicitExit(false);
            return;
        }

        if (STARTED.get()) {
            return;
        }

        synchronized (RUNTIME_LOCK) {

            if (SHUTDOWN.get()) {
                throw new IllegalStateException("JavaFX runtime has already been shut down");
            }

            if (STARTED.get()) {
                return;
            }

            CountDownLatch startupLatch = new CountDownLatch(1);
            AtomicReference<Throwable> failure = new AtomicReference<>();

            try {

                Platform.startup(
                        () -> {
                            try {
                                /*
                                 * Slate owns toolkit shutdown rather than
                                 * allowing JavaFX to implicitly terminate when
                                 * the last application window disappears.
                                 */
                                Platform.setImplicitExit(false);
                                STARTED.set(true);
                            } catch (Throwable throwable) {
                                failure.set(throwable);
                            } finally {
                                startupLatch.countDown();
                            }
                        }
                );

            } catch (IllegalStateException alreadyStarted) {

                /*
                 * Another part of the JVM may have initialized JavaFX before
                 * Slate. The toolkit can still be used in that situation.
                 */
                Platform.runLater(
                        () -> {
                            try {
                                Platform.setImplicitExit(false);
                                STARTED.set(true);

                            } catch (Throwable throwable) {
                                failure.set(throwable);

                            } finally {
                                startupLatch.countDown();
                            }
                        }
                );
            }

            await(startupLatch, "Interrupted while starting JavaFX");

            rethrowFailure(failure.get(), "Failed to initialize JavaFX");
        }
    }

    /**
     * Executes an action on the JavaFX application thread and waits until it
     * completes.
     *
     * <p>If the caller is already running on the JavaFX thread, the action is
     * executed immediately to avoid deadlocking the toolkit.</p>
     */
    public static void run(Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException("JavaFX action cannot be null");
        }

        initialize();

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch actionLatch = new CountDownLatch(1);

        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(
                () -> {
                    try {
                        action.run();
                    } catch (Throwable throwable) {
                        failure.set(throwable);
                    } finally {
                        actionLatch.countDown();
                    }
                }
        );

        await(actionLatch, "Interrupted while waiting for JavaFX action");

        rethrowFailure(failure.get(), "JavaFX action failed");
    }

    /**
     * Shuts down the Slate-owned JavaFX runtime.
     *
     * <p>The shutdown is synchronous when called from a non-JavaFX thread so
     * callers do not continue under the assumption that the toolkit is still
     * active.</p>
     *
     * <p>After this method completes, the JavaFX toolkit is considered
     * permanently shut down for the current Slate application lifecycle.</p>
     */
    public static void shutdown() {

        synchronized (RUNTIME_LOCK) {

            if (!STARTED.get()) {
                return;
            }

            if (SHUTDOWN.get()) {
                return;
            }

            SHUTDOWN.set(true);
        }

        if (Platform.isFxApplicationThread()) {

            try {
                Platform.exit();

            } finally {
                STARTED.set(false);
            }

            return;
        }

        CountDownLatch shutdownLatch = new CountDownLatch(1);

        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(
                () -> {
                    try {
                        Platform.exit();

                    } catch (Throwable throwable) {
                        failure.set(throwable);

                    } finally {
                        STARTED.set(false);
                        shutdownLatch.countDown();
                    }
                }
        );

        await(shutdownLatch, "Interrupted while shutting down JavaFX");

        rethrowFailure(failure.get(), "Failed to shut down JavaFX");
    }


    /**
     * Returns whether the JavaFX toolkit is currently active for Slate.
     */
    public static boolean isStarted() {
        return STARTED.get() && !SHUTDOWN.get();
    }

    private static void await(CountDownLatch latch, String interruptionMessage) {
        try {
            latch.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interruptionMessage, e);
        }
    }

    private static void rethrowFailure(Throwable failure, String message) {
        if (failure == null) {
            return;
        }

        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        if (failure instanceof Error error) {
            throw error;
        }

        throw new IllegalStateException(message, failure);
    }
}