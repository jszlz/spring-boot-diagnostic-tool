package com.diagnostic.core.collector;

import com.diagnostic.core.model.JvmMetrics;
import com.diagnostic.core.storage.JvmMetricsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JVM 指标收集器协调器
 * Main orchestrator for JVM metrics collection
 * Coordinates individual collectors and manages scheduled collection
 */
public class JvmMetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(JvmMetricsCollector.class);

    private final MemoryCollector memoryCollector;
    private final GcCollector gcCollector;
    private final ThreadCollector threadCollector;
    private final SystemCollector systemCollector;
    private final JvmMetricsStorage storage;
    private final ScheduledExecutorService scheduler;
    private final int collectionIntervalSeconds;

    private volatile boolean started = false;

    public JvmMetricsCollector(JvmMetricsStorage storage, int collectionIntervalSeconds) {
        this.memoryCollector = new MemoryCollector();
        this.gcCollector = new GcCollector();
        this.threadCollector = new ThreadCollector();
        this.systemCollector = new SystemCollector();
        this.storage = storage;
        this.collectionIntervalSeconds = Math.max(5, collectionIntervalSeconds); // Minimum 5 seconds
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("jvm-metrics-collector");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 启动定时收集
     * Start scheduled metric collection
     */
    public void start() {
        if (started) {
            logger.warn("JVM metrics collector already started");
            return;
        }

        started = true;
        scheduleCollection();
        logger.info("JVM metrics collector started with interval {} seconds", collectionIntervalSeconds);
    }

    /**
     * 停止定时收集
     * Stop scheduled metric collection
     */
    public void stop() {
        if (!started) {
            return;
        }

        started = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("JVM metrics collector stopped");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("JVM metrics collector shutdown interrupted");
        }
    }

    /**
     * 同步收集所有指标
     * Synchronously collect all metrics (used for on-demand collection)
     */
    public JvmMetrics collectMetrics() {
        try {
            long timestamp = System.currentTimeMillis();

            JvmMetrics.Builder builder = JvmMetrics.builder()
                    .timestamp(timestamp);

            // Collect heap memory metrics
            try {
                builder.heapMemory(memoryCollector.collectHeapMetrics());
            } catch (Exception e) {
                logger.error("Error collecting heap memory metrics: {}", e.getMessage());
            }

            // Collect non-heap memory metrics
            try {
                builder.nonHeapMemory(memoryCollector.collectNonHeapMetrics());
            } catch (Exception e) {
                logger.error("Error collecting non-heap memory metrics: {}", e.getMessage());
            }

            // Collect GC metrics
            try {
                builder.gc(gcCollector.collectGcMetrics());
            } catch (Exception e) {
                logger.error("Error collecting GC metrics: {}", e.getMessage());
            }

            // Collect thread metrics
            try {
                builder.threads(threadCollector.collectThreadMetrics());
            } catch (Exception e) {
                logger.error("Error collecting thread metrics: {}", e.getMessage());
            }

            // Collect CPU metrics
            try {
                builder.cpu(systemCollector.collectCpuMetrics());
            } catch (Exception e) {
                logger.error("Error collecting CPU metrics: {}", e.getMessage());
            }

            // Collect system resource metrics
            try {
                builder.system(systemCollector.collectSystemMetrics());
            } catch (Exception e) {
                logger.error("Error collecting system resource metrics: {}", e.getMessage());
            }

            JvmMetrics metrics = builder.build();
            
            // Store metrics
            if (storage != null) {
                storage.store(metrics);
            }

            return metrics;
        } catch (Exception e) {
            logger.error("Error during metrics collection: {}", e.getMessage());
            return new JvmMetrics();
        }
    }

    /**
     * 调度定时收集任务
     * Schedule periodic collection task
     */
    private void scheduleCollection() {
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        collectMetrics();
                        logger.debug("JVM metrics collected successfully");
                    } catch (Exception e) {
                        logger.error("Error in scheduled metrics collection: {}", e.getMessage());
                    }
                },
                0, // Initial delay
                collectionIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * 检查收集器是否已启动
     * Check if collector is started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 获取收集间隔（秒）
     * Get collection interval in seconds
     */
    public int getCollectionIntervalSeconds() {
        return collectionIntervalSeconds;
    }
}
