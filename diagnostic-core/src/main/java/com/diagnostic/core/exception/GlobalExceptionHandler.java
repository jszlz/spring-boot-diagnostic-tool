package com.diagnostic.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Global exception handler for diagnostic tool.
 * Ensures that diagnostic tool exceptions do not break the application.
 * Implements silent failure with logging.
 */
@Component
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle exception with silent failure.
     * Logs the error but does not propagate the exception.
     *
     * @param operation description of the operation that failed
     * @param e the exception
     */
    public void handleSilently(String operation, Exception e) {
        logger.error("Diagnostic tool error during {}: {}", operation, e.getMessage());
        logger.debug("Stack trace:", e);
    }

    /**
     * Handle exception with silent failure and return default value.
     *
     * @param operation description of the operation that failed
     * @param e the exception
     * @param defaultValue default value to return
     * @param <T> type of default value
     * @return default value
     */
    public <T> T handleSilentlyWithDefault(String operation, Exception e, T defaultValue) {
        handleSilently(operation, e);
        return defaultValue;
    }

    /**
     * Execute an operation with exception handling.
     * If the operation fails, logs the error and returns null.
     *
     * @param operation description of the operation
     * @param task the task to execute
     * @param <T> return type
     * @return result of the task, or null if failed
     */
    public <T> T executeWithHandling(String operation, java.util.concurrent.Callable<T> task) {
        try {
            return task.call();
        } catch (Exception e) {
            handleSilently(operation, e);
            return null;
        }
    }

    /**
     * Execute an operation with exception handling and default value.
     *
     * @param operation description of the operation
     * @param task the task to execute
     * @param defaultValue default value if task fails
     * @param <T> return type
     * @return result of the task, or default value if failed
     */
    public <T> T executeWithHandling(String operation, java.util.concurrent.Callable<T> task, T defaultValue) {
        try {
            return task.call();
        } catch (Exception e) {
            handleSilently(operation, e);
            return defaultValue;
        }
    }

    /**
     * Execute a void operation with exception handling.
     *
     * @param operation description of the operation
     * @param task the task to execute
     */
    public void executeWithHandling(String operation, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            handleSilently(operation, e);
        }
    }

    /**
     * Check if an exception should be ignored (non-critical).
     *
     * @param e the exception
     * @return true if should be ignored, false otherwise
     */
    public boolean shouldIgnore(Exception e) {
        // Ignore certain types of exceptions that are expected
        return e instanceof InterruptedException ||
               e instanceof java.util.concurrent.TimeoutException;
    }
}
