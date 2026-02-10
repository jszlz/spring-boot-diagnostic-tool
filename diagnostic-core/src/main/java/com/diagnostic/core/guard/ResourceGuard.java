package com.diagnostic.core.guard;

import com.diagnostic.core.model.EndpointMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource guard component to protect application from excessive resource usage.
 * Monitors memory usage and implements degradation strategies when limits are reached.
 */
public class ResourceGuard {

    private static final Logger logger = LoggerFactory.getLogger(ResourceGuard.class);

    private final long maxMemoryBytes;
    private final double maxMemoryUsageThreshold;
    private final int maxCachedMetrics;
    private volatile double currentSamplingRate;
    private volatile boolean degraded;

    public ResourceGuard() {
        this(Runtime.getRuntime().maxMemory(), 0.9, 10000);
    }

    public ResourceGuard(long maxMemoryBytes, double maxMemoryUsageThreshold, int maxCachedMetrics) {
        this.maxMemoryBytes = maxMemoryBytes;
        this.maxMemoryUsageThreshold = maxMemoryUsageThreshold;
        this.maxCachedMetrics = maxCachedMetrics;
        this.currentSamplingRate = 1.0;
        this.degraded = false;
    }

    /**
     * Check if a new metric can be collected based on current resource usage.
     *
     * @return true if metric can be collected, false otherwise
     */
    public boolean canCollectMetric() {
        double memoryUsage = getMemoryUsageRatio();
        
        if (memoryUsage > maxMemoryUsageThreshold) {
            if (!degraded) {
                logger.warn("Memory usage {}% exceeds threshold {}%, entering degraded mode",
                          String.format("%.1f", memoryUsage * 100),
                          String.format("%.1f", maxMemoryUsageThreshold * 100));
                degraded = true;
            }
            
            // Reduce sampling rate based on memory pressure
            currentSamplingRate = calculateDegradedSamplingRate(memoryUsage);
            
            // Probabilistic collection based on reduced sampling rate
            return Math.random() < currentSamplingRate;
        } else {
            if (degraded) {
                logger.info("Memory usage normalized, exiting degraded mode");
                degraded = false;
                currentSamplingRate = 1.0;
            }
            return true;
        }
    }

    /**
     * Calculate degraded sampling rate based on memory usage.
     *
     * @param memoryUsage current memory usage ratio (0.0 to 1.0)
     * @return sampling rate (0.0 to 1.0)
     */
    private double calculateDegradedSamplingRate(double memoryUsage) {
        if (memoryUsage >= 0.95) {
            return 0.05; // Only 5% sampling at critical memory levels
        } else if (memoryUsage >= 0.92) {
            return 0.1; // 10% sampling
        } else if (memoryUsage >= maxMemoryUsageThreshold) {
            return 0.3; // 30% sampling
        }
        return 1.0; // Full sampling
    }

    /**
     * Get current memory usage ratio.
     *
     * @return memory usage ratio (0.0 to 1.0)
     */
    public double getMemoryUsageRatio() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return (double) usedMemory / maxMemoryBytes;
    }

    /**
     * Get current memory usage in megabytes.
     *
     * @return memory usage in MB
     */
    public long getMemoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return usedMemory / (1024 * 1024);
    }

    /**
     * Enforce metric limit on a cache by removing oldest entries.
     *
     * @param metricsCache the cache to enforce limits on
     */
    public void enforceMetricLimit(Map<String, List<EndpointMetrics>> metricsCache) {
        if (metricsCache == null) {
            return;
        }

        metricsCache.forEach((endpoint, metrics) -> {
            if (metrics.size() > maxCachedMetrics) {
                int toRemove = metrics.size() - maxCachedMetrics;
                
                // Remove oldest metrics (from the beginning of the list)
                synchronized (metrics) {
                    if (metrics.size() > maxCachedMetrics) {
                        metrics.subList(0, toRemove).clear();
                        logger.debug("Removed {} old metrics for endpoint {} to enforce limit",
                                   toRemove, endpoint);
                    }
                }
            }
        });
    }

    /**
     * Check if the system is in degraded mode.
     *
     * @return true if degraded, false otherwise
     */
    public boolean isDegraded() {
        return degraded;
    }

    /**
     * Get current sampling rate (may be reduced in degraded mode).
     *
     * @return current sampling rate (0.0 to 1.0)
     */
    public double getCurrentSamplingRate() {
        return currentSamplingRate;
    }

    /**
     * Force garbage collection and log memory status.
     * Should be used sparingly as it can impact performance.
     */
    public void forceGarbageCollection() {
        logger.info("Forcing garbage collection due to memory pressure");
        System.gc();
        
        try {
            Thread.sleep(100); // Give GC time to run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("Memory after GC: {} MB used", getMemoryUsageMB());
    }

    /**
     * Get memory status information.
     *
     * @return map containing memory status details
     */
    public Map<String, Object> getMemoryStatus() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> status = new HashMap<>();
        status.put("maxMemoryMB", maxMemory / (1024 * 1024));
        status.put("totalMemoryMB", totalMemory / (1024 * 1024));
        status.put("usedMemoryMB", usedMemory / (1024 * 1024));
        status.put("freeMemoryMB", freeMemory / (1024 * 1024));
        status.put("usageRatio", getMemoryUsageRatio());
        status.put("degraded", degraded);
        status.put("currentSamplingRate", currentSamplingRate);
        
        return status;
    }
}
