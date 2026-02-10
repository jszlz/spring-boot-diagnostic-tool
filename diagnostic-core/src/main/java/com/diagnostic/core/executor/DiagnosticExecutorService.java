package com.diagnostic.core.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * Executor service for diagnostic tool background tasks.
 * Manages a thread pool for async operations and ensures graceful shutdown.
 */
public class DiagnosticExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticExecutorService.class);
    private static final int DEFAULT_CORE_POOL_SIZE = 2;
    private static final int DEFAULT_MAX_POOL_SIZE = 4;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 100;

    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduledExecutor;

    public DiagnosticExecutorService() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public DiagnosticExecutorService(int corePoolSize, int maxPoolSize) {
        // Create thread pool for async tasks
        this.executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new DiagnosticThreadFactory("diagnostic-worker"),
            new ThreadPoolExecutor.CallerRunsPolicy() // Fallback to caller thread if queue is full
        );

        // Create scheduled executor for periodic tasks
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            1,
            new DiagnosticThreadFactory("diagnostic-scheduler")
        );

        logger.info("Diagnostic executor service initialized with core pool size: {}, max pool size: {}",
                   corePoolSize, maxPoolSize);
    }

    /**
     * Submit a task for async execution.
     *
     * @param task the task to execute
     * @return future representing the task
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * Submit a callable task for async execution.
     *
     * @param task the task to execute
     * @param <T> the result type
     * @return future representing the task result
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * Execute a task asynchronously without returning a future.
     *
     * @param task the task to execute
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * Schedule a task to run periodically.
     *
     * @param task the task to execute
     * @param initialDelay initial delay before first execution
     * @param period period between executions
     * @param unit time unit
     * @return scheduled future
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * Schedule a task to run once after a delay.
     *
     * @param task the task to execute
     * @param delay delay before execution
     * @param unit time unit
     * @return scheduled future
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }

    /**
     * Get current pool size.
     *
     * @return current pool size
     */
    public int getPoolSize() {
        return executor.getPoolSize();
    }

    /**
     * Get active thread count.
     *
     * @return active thread count
     */
    public int getActiveCount() {
        return executor.getActiveCount();
    }

    /**
     * Get queue size.
     *
     * @return number of tasks in queue
     */
    public int getQueueSize() {
        return executor.getQueue().size();
    }

    /**
     * Get completed task count.
     *
     * @return completed task count
     */
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    /**
     * Check if executor is shutdown.
     *
     * @return true if shutdown, false otherwise
     */
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * Gracefully shutdown the executor service.
     * Waits for running tasks to complete.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down diagnostic executor service...");
        
        // Shutdown main executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();
                
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error("Executor did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for executor shutdown");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Shutdown scheduled executor
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Diagnostic executor service shutdown complete");
    }

    /**
     * Custom thread factory for diagnostic threads.
     */
    private static class DiagnosticThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private int threadNumber = 1;

        DiagnosticThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-" + threadNumber++);
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
}
