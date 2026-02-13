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
                String endpoint = metrics != null && metrics.getEndpoint() != null 
                    ? metrics.getEndpoint() 
                    : "unknown";
                logger.error("Error collecting metrics for endpoint {}: {}", 
                           endpoint, e.getMessage(), e);
            }
        });
    }

    /**
     * Update statistics for an endpoint based on new metrics.
     *
     * @param metrics the new metrics
     */
    private void updateStatistics(EndpointMetrics metrics) {
        if (metrics == null) {
            logger.warn("Received null metrics in updateStatistics");
            return;
        }
        
        String endpoint = metrics.getEndpoint();
        if (endpoint == null || endpoint.isEmpty()) {
            logger.warn("Received metrics with null or empty endpoint");
            return;
        }
        
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

        // Calculate error count
        long errorCount = calculateErrorCount(metrics);
        stats.setErrorCount(errorCount);

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

        // Rebuild recent errors list and status code distribution
        rebuildErrorDetails(stats, metrics);

        // Update code location and endpoint type if available
        if (!metrics.isEmpty()) {
            EndpointMetrics sample = metrics.get(0);
            if (sample.getControllerClass() != null && sample.getControllerMethod() != null) {
                String codeLocation = sample.getControllerClass() + "." + sample.getControllerMethod();
                stats.setCodeLocation(codeLocation);
            }
            // Set endpoint type from the first metric (all should be the same)
            if (sample.getEndpointType() != null) {
                stats.setEndpointType(sample.getEndpointType());
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

        long errorCount = calculateErrorCount(metrics);
        return (double) errorCount / metrics.size();
    }

    /**
     * Calculate error count (4xx and 5xx responses).
     *
     * @param metrics list of metrics
     * @return error count
     */
    private long calculateErrorCount(List<EndpointMetrics> metrics) {
        if (metrics.isEmpty()) {
            return 0;
        }

        return metrics.stream()
                .filter(m -> m.getStatusCode() >= 400)
                .count();
    }

    /**
     * Rebuild error details list and status code distribution.
     * This ensures error details are in sync with error count.
     *
     * @param stats the statistics object to update
     * @param metrics list of all metrics
     */
    private void rebuildErrorDetails(EndpointStatistics stats, List<EndpointMetrics> metrics) {
        if (stats == null) {
            return;
        }
        
        String endpoint = stats.getEndpoint();
        
        // First, try to rebuild from storage for data freshness
        logger.debug("Rebuilding error details for endpoint: {} (source: storage)", endpoint);
        rebuildErrorDetailsFromStorage(endpoint, stats);
        
        // If storage returned data, we're done
        if (!stats.getRecentErrors().isEmpty()) {
            logger.debug("Successfully rebuilt error details from storage for endpoint: {}", endpoint);
            return;
        }
        
        // Fallback: use in-memory metrics if storage is empty
        if (metrics == null || metrics.isEmpty()) {
            logger.debug("No metrics available for endpoint: {}", endpoint);
            return;
        }
        
        logger.debug("Rebuilding error details for endpoint: {} (source: memory fallback)", endpoint);

        // Clear existing error details
        stats.getRecentErrors().clear();
        stats.getErrorStatusCodeDistribution().clear();

        // Filter error metrics (status code >= 400)
        List<EndpointMetrics> errorMetrics = metrics.stream()
                .filter(m -> m.getStatusCode() >= 400)
                .collect(Collectors.toList());

        // Log warning if storage and memory counts differ significantly
        int storageErrorCount = metricsStorage.getErrorMetrics(endpoint).size();
        if (Math.abs(storageErrorCount - errorMetrics.size()) > 10) {
            logger.warn("Significant difference between storage ({}) and memory ({}) error counts for endpoint: {}", 
                       storageErrorCount, errorMetrics.size(), endpoint);
        }

        // Keep only the most recent 100 errors
        int startIndex = Math.max(0, errorMetrics.size() - 100);
        List<EndpointMetrics> recentErrorMetrics = errorMetrics.subList(startIndex, errorMetrics.size());

        // Rebuild error details list
        for (EndpointMetrics metric : recentErrorMetrics) {
            com.diagnostic.core.model.ErrorDetail error = new com.diagnostic.core.model.ErrorDetail(
                metric.getTimestamp(),
                metric.getStatusCode(),
                metric.getMethod(),
                metric.getEndpoint(),
                metric.getDuration() / 1_000_000  // Convert to milliseconds
            );
            stats.getRecentErrors().add(error);
        }

        // Rebuild status code distribution (use all error metrics, not just recent 100)
        for (EndpointMetrics metric : errorMetrics) {
            stats.getErrorStatusCodeDistribution().merge(
                metric.getStatusCode(), 
                1L, 
                Long::sum
            );
        }
        
        logger.debug("Rebuilt error details from memory: {} recent errors for endpoint: {}", 
                     stats.getRecentErrors().size(), endpoint);
    }

    /**
     * Rebuild error details from storage for an endpoint.
     * This ensures error details are synchronized with error count.
     *
     * @param endpoint the endpoint name
     * @param stats the statistics object to update
     */
    private void rebuildErrorDetailsFromStorage(String endpoint, EndpointStatistics stats) {
        logger.debug("Rebuilding error details from storage for endpoint: {}", endpoint);
        
        // Query storage for all error metrics
        List<EndpointMetrics> errorMetrics = metricsStorage.getErrorMetrics(endpoint);
        
        logger.debug("Found {} error metrics in storage for endpoint: {}", 
                     errorMetrics.size(), endpoint);
        
        // Clear existing error details
        stats.getRecentErrors().clear();
        stats.getErrorStatusCodeDistribution().clear();
        
        if (errorMetrics.isEmpty()) {
            if (stats.getErrorCount() > 0) {
                logger.warn("Error count is {} but no error metrics found in storage for endpoint: {}. " +
                           "This indicates a data inconsistency.", stats.getErrorCount(), endpoint);
            }
            return;
        }
        
        // Keep only the most recent 100 errors for the details list
        int startIndex = Math.max(0, errorMetrics.size() - 100);
        List<EndpointMetrics> recentErrorMetrics = errorMetrics.subList(startIndex, errorMetrics.size());
        
        // Rebuild error details list
        for (EndpointMetrics metric : recentErrorMetrics) {
            com.diagnostic.core.model.ErrorDetail error = new com.diagnostic.core.model.ErrorDetail(
                metric.getTimestamp(),
                metric.getStatusCode(),
                metric.getMethod(),
                metric.getEndpoint(),
                metric.getDuration() / 1_000_000  // Convert nanoseconds to milliseconds
            );
            stats.getRecentErrors().add(error);
        }
        
        // Rebuild status code distribution (use all error metrics, not just recent 100)
        for (EndpointMetrics metric : errorMetrics) {
            stats.getErrorStatusCodeDistribution().merge(
                metric.getStatusCode(), 
                1L, 
                Long::sum
            );
        }
        
        logger.debug("Rebuilt error details: {} recent errors, {} status codes for endpoint: {}", 
                     stats.getRecentErrors().size(), 
                     stats.getErrorStatusCodeDistribution().size(), 
                     endpoint);
    }

    /**
     * Get error details for an endpoint, ensuring data freshness.
     * This method queries storage directly to avoid stale cache issues.
     *
     * @param endpoint the endpoint name
     * @return list of error details
     */
    public List<com.diagnostic.core.model.ErrorDetail> getErrorDetails(String endpoint) {
        logger.debug("Getting error details for endpoint: {}", endpoint);
        
        // Query storage directly for error metrics
        List<EndpointMetrics> errorMetrics = metricsStorage.getErrorMetrics(endpoint);
        
        logger.debug("Retrieved {} error metrics from storage for endpoint: {}", 
                     errorMetrics.size(), endpoint);
        
        // Keep only the most recent 100 errors
        int startIndex = Math.max(0, errorMetrics.size() - 100);
        List<EndpointMetrics> recentErrorMetrics = errorMetrics.subList(startIndex, errorMetrics.size());
        
        // Convert to ErrorDetail objects
        List<com.diagnostic.core.model.ErrorDetail> errorDetails = new ArrayList<>();
        for (EndpointMetrics metric : recentErrorMetrics) {
            com.diagnostic.core.model.ErrorDetail error = new com.diagnostic.core.model.ErrorDetail(
                metric.getTimestamp(),
                metric.getStatusCode(),
                metric.getMethod(),
                metric.getEndpoint(),
                metric.getDuration() / 1_000_000  // Convert nanoseconds to milliseconds
            );
            errorDetails.add(error);
        }
        
        logger.debug("Returning {} error details for endpoint: {}", errorDetails.size(), endpoint);
        return errorDetails;
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
