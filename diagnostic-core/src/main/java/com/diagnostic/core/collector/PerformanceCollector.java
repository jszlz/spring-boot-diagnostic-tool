package com.diagnostic.core.collector;

import com.diagnostic.core.model.EndpointMetrics;
import com.diagnostic.core.model.EndpointStatistics;
import com.diagnostic.core.storage.MetricsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Collector for performance metrics.
 * Asynchronously collects and processes endpoint performance data,
 * calculating statistics like QPS, average response time, percentiles, and error rates.
 */
public class PerformanceCollector {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceCollector.class);
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private final ExecutorService asyncExecutor;
    private final MetricsStorage metricsStorage;
    private final Map<String, EndpointStatistics> statisticsCache;
    private final Map<String, List<EndpointMetrics>> recentMetrics;
    private final int slowEndpointThresholdMs;

    public PerformanceCollector(MetricsStorage metricsStorage) {
        this(metricsStorage, 1000); // Default 1000ms threshold
    }

    public PerformanceCollector(MetricsStorage metricsStorage, int slowEndpointThresholdMs) {
        this.metricsStorage = metricsStorage;
        this.slowEndpointThresholdMs = slowEndpointThresholdMs;
        this.asyncExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("diagnostic-collector-" + t.getId());
                    t.setDaemon(true);
                    return t;
                });
        this.statisticsCache = new ConcurrentHashMap<>();
        this.recentMetrics = new ConcurrentHashMap<>();
    }

    /**
     * Collect a performance metric asynchronously.
     *
     * @param metrics the metrics to collect
     */
    public void collect(EndpointMetrics metrics) {
        if (metrics == null) {
            return;
        }

        // Async processing to avoid blocking request threads
        asyncExecutor.submit(() -> {
            try {
                metricsStorage.store(metrics);
                updateStatistics(metrics);
            } catch (Exception e) {
                logger.error("Error collecting metrics for endpoint {}: {}", 
                           metrics.getEndpoint(), e.getMessage());
            }
        });
    }

    /**
     * Update statistics for an endpoint based on new metrics.
     *
     * @param metrics the new metrics
     */
    private void updateStatistics(EndpointMetrics metrics) {
        String endpoint = metrics.getEndpoint();
        
        // Add to recent metrics
        recentMetrics.computeIfAbsent(endpoint, k -> Collections.synchronizedList(new ArrayList<>()))
                     .add(metrics);

        // Calculate statistics
        statisticsCache.compute(endpoint, (key, stats) -> {
            if (stats == null) {
                stats = new EndpointStatistics(key);
            }
            stats.addMetric(metrics);
            
            // Recalculate all statistics
            List<EndpointMetrics> allMetrics = recentMetrics.get(endpoint);
            calculateStatistics(stats, allMetrics);
            
            return stats;
        });
    }

    /**
     * Calculate all statistics for an endpoint.
     *
     * @param stats the statistics object to update
     * @param metrics list of metrics to calculate from
     */
    private void calculateStatistics(EndpointStatistics stats, List<EndpointMetrics> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        // Total requests
        stats.setTotalRequests(metrics.size());

        // Calculate QPS (queries per second)
        double qps = calculateQPS(metrics);
        stats.setQps(qps);

        // Calculate average response time
        double avgResponseTime = calculateAverageResponseTime(metrics);
        stats.setAverageResponseTime(avgResponseTime);

        // Calculate percentiles
        double p95 = calculatePercentile(metrics, 0.95);
        double p99 = calculatePercentile(metrics, 0.99);
        stats.setP95ResponseTime(p95);
        stats.setP99ResponseTime(p99);

        // Calculate error rate
        double errorRate = calculateErrorRate(metrics);
        stats.setErrorRate(errorRate);

        // Update code location if available
        if (!metrics.isEmpty()) {
            EndpointMetrics sample = metrics.get(0);
            if (sample.getControllerClass() != null && sample.getControllerMethod() != null) {
                String codeLocation = sample.getControllerClass() + "." + sample.getControllerMethod();
                stats.setCodeLocation(codeLocation);
            }
        }
    }

    /**
     * Calculate QPS (queries per second) for an endpoint.
     *
     * @param metrics list of metrics
     * @return QPS value
     */
    private double calculateQPS(List<EndpointMetrics> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        long minTimestamp = metrics.stream()
                .mapToLong(EndpointMetrics::getTimestamp)
                .min()
                .orElse(System.currentTimeMillis());
        
        long maxTimestamp = metrics.stream()
                .mapToLong(EndpointMetrics::getTimestamp)
                .max()
                .orElse(System.currentTimeMillis());

        long timeWindowSeconds = (maxTimestamp - minTimestamp) / 1000;
        if (timeWindowSeconds == 0) {
            return metrics.size(); // All requests in same second
        }

        return (double) metrics.size() / timeWindowSeconds;
    }

    /**
     * Calculate average response time in milliseconds.
     *
     * @param metrics list of metrics
     * @return average response time
     */
    private double calculateAverageResponseTime(List<EndpointMetrics> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double totalDuration = metrics.stream()
                .mapToLong(EndpointMetrics::getDuration)
                .sum();

        // Convert from nanoseconds to milliseconds
        return (totalDuration / metrics.size()) / 1_000_000.0;
    }

    /**
     * Calculate percentile response time.
     *
     * @param metrics list of metrics
     * @param percentile percentile value (0.0 to 1.0)
     * @return percentile response time in milliseconds
     */
    private double calculatePercentile(List<EndpointMetrics> metrics, double percentile) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        List<Long> durations = metrics.stream()
                .map(EndpointMetrics::getDuration)
                .sorted()
                .collect(Collectors.toList());

        int index = (int) Math.ceil(percentile * durations.size()) - 1;
        index = Math.max(0, Math.min(index, durations.size() - 1));

        // Convert from nanoseconds to milliseconds
        return durations.get(index) / 1_000_000.0;
    }

    /**
     * Calculate error rate (4xx and 5xx responses).
     *
     * @param metrics list of metrics
     * @return error rate (0.0 to 1.0)
     */
    private double calculateErrorRate(List<EndpointMetrics> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        long errorCount = metrics.stream()
                .filter(m -> m.getStatusCode() >= 400)
                .count();

        return (double) errorCount / metrics.size();
    }

    /**
     * Check if an endpoint is slow based on threshold.
     *
     * @param endpoint the endpoint name
     * @return true if slow, false otherwise
     */
    public boolean isSlowEndpoint(String endpoint) {
        EndpointStatistics stats = statisticsCache.get(endpoint);
        if (stats == null) {
            return false;
        }
        return stats.getAverageResponseTime() > slowEndpointThresholdMs;
    }

    /**
     * Get statistics for a specific endpoint.
     *
     * @param endpoint the endpoint name
     * @return statistics, or null if not found
     */
    public EndpointStatistics getStatistics(String endpoint) {
        return statisticsCache.get(endpoint);
    }

    /**
     * Get statistics for all endpoints.
     *
     * @return map of endpoint to statistics
     */
    public Map<String, EndpointStatistics> getAllStatistics() {
        return new HashMap<>(statisticsCache);
    }

    /**
     * Refresh statistics for an endpoint by recalculating from stored metrics.
     *
     * @param endpoint the endpoint name
     */
    public void refreshStatistics(String endpoint) {
        List<EndpointMetrics> metrics = metricsStorage.getMetrics(endpoint);
        if (!metrics.isEmpty()) {
            EndpointStatistics stats = new EndpointStatistics(endpoint);
            calculateStatistics(stats, metrics);
            statisticsCache.put(endpoint, stats);
        }
    }

    /**
     * Refresh statistics for all endpoints.
     */
    public void refreshAllStatistics() {
        for (String endpoint : metricsStorage.getAllEndpoints()) {
            refreshStatistics(endpoint);
        }
    }

    /**
     * Shutdown the async executor gracefully.
     */
    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Performance collector shutdown complete");
    }
}
