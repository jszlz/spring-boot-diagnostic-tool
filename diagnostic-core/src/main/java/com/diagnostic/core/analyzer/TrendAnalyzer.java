package com.diagnostic.core.analyzer;

import com.diagnostic.core.model.EndpointMetrics;
import com.diagnostic.core.model.TrendAnalysis;
import com.diagnostic.core.storage.MetricsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for performance trends over time.
 * Uses linear regression to calculate trends and detect performance degradation.
 */
public class TrendAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(TrendAnalyzer.class);
    private static final double DEGRADATION_THRESHOLD = 0.2; // 20% increase is considered degradation
    private static final int MIN_DATA_POINTS = 10; // Minimum data points for trend analysis

    private final MetricsStorage metricsStorage;

    public TrendAnalyzer(MetricsStorage metricsStorage) {
        this.metricsStorage = metricsStorage;
    }

    /**
     * Analyze trend for a specific endpoint.
     *
     * @param endpoint the endpoint name
     * @return trend analysis result
     */
    public TrendAnalysis analyzeTrend(String endpoint) {
        List<EndpointMetrics> metrics = metricsStorage.getMetrics(endpoint);
        
        if (metrics.size() < MIN_DATA_POINTS) {
            logger.debug("Insufficient data points for trend analysis: {} (need at least {})",
                       metrics.size(), MIN_DATA_POINTS);
            return new TrendAnalysis(endpoint, TrendAnalysis.TrendDirection.STABLE, 0.0, false);
        }

        // Sort by timestamp
        List<EndpointMetrics> sortedMetrics = metrics.stream()
                .sorted(Comparator.comparingLong(EndpointMetrics::getTimestamp))
                .collect(Collectors.toList());

        // Calculate trend using linear regression
        double slope = calculateTrendSlope(sortedMetrics);
        TrendAnalysis.TrendDirection direction = determineTrendDirection(slope);
        boolean degrading = isPerformanceDegrading(sortedMetrics, slope);

        return new TrendAnalysis(endpoint, direction, slope, degrading);
    }

    /**
     * Analyze trends for all endpoints.
     *
     * @return map of endpoint to trend analysis
     */
    public Map<String, TrendAnalysis> analyzeAllTrends() {
        Map<String, TrendAnalysis> trends = new HashMap<>();
        
        for (String endpoint : metricsStorage.getAllEndpoints()) {
            try {
                TrendAnalysis trend = analyzeTrend(endpoint);
                trends.put(endpoint, trend);
            } catch (Exception e) {
                logger.error("Error analyzing trend for endpoint {}: {}", endpoint, e.getMessage());
            }
        }

        return trends;
    }

    /**
     * Calculate trend slope using linear regression.
     * Positive slope indicates increasing response time (degradation).
     * Negative slope indicates decreasing response time (improvement).
     *
     * @param metrics sorted list of metrics
     * @return slope value
     */
    private double calculateTrendSlope(List<EndpointMetrics> metrics) {
        int n = metrics.size();
        
        // Use index as x (time series) and response time as y
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = metrics.get(i).getDuration() / 1_000_000.0; // Convert to milliseconds
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Calculate slope: (n*sumXY - sumX*sumY) / (n*sumX2 - sumX*sumX)
        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = (n * sumX2) - (sumX * sumX);

        if (denominator == 0) {
            return 0.0;
        }

        return numerator / denominator;
    }

    /**
     * Determine trend direction based on slope.
     *
     * @param slope the calculated slope
     * @return trend direction
     */
    private TrendAnalysis.TrendDirection determineTrendDirection(double slope) {
        if (Math.abs(slope) < 0.01) { // Nearly flat
            return TrendAnalysis.TrendDirection.STABLE;
        } else if (slope > 0) {
            return TrendAnalysis.TrendDirection.RISING;
        } else {
            return TrendAnalysis.TrendDirection.FALLING;
        }
    }

    /**
     * Check if performance is degrading significantly.
     *
     * @param metrics sorted list of metrics
     * @param slope the trend slope
     * @return true if degrading, false otherwise
     */
    private boolean isPerformanceDegrading(List<EndpointMetrics> metrics, double slope) {
        if (slope <= 0) {
            return false; // Performance is improving or stable
        }

        // Compare recent average to historical average
        int splitPoint = metrics.size() / 2;
        List<EndpointMetrics> historical = metrics.subList(0, splitPoint);
        List<EndpointMetrics> recent = metrics.subList(splitPoint, metrics.size());

        double historicalAvg = calculateAverageResponseTime(historical);
        double recentAvg = calculateAverageResponseTime(recent);

        if (historicalAvg == 0) {
            return false;
        }

        double changeRate = (recentAvg - historicalAvg) / historicalAvg;
        
        return changeRate > DEGRADATION_THRESHOLD;
    }

    /**
     * Calculate average response time for a list of metrics.
     *
     * @param metrics list of metrics
     * @return average response time in milliseconds
     */
    private double calculateAverageResponseTime(List<EndpointMetrics> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double sum = metrics.stream()
                .mapToDouble(m -> m.getDuration() / 1_000_000.0)
                .sum();

        return sum / metrics.size();
    }

    /**
     * Get time series data for an endpoint.
     *
     * @param endpoint the endpoint name
     * @return list of time-value pairs
     */
    public List<Map<String, Object>> getTimeSeriesData(String endpoint) {
        List<EndpointMetrics> metrics = metricsStorage.getMetrics(endpoint);
        
        return metrics.stream()
                .sorted(Comparator.comparingLong(EndpointMetrics::getTimestamp))
                .map(m -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("timestamp", m.getTimestamp());
                    dataPoint.put("responseTime", m.getDuration() / 1_000_000.0);
                    dataPoint.put("statusCode", m.getStatusCode());
                    return dataPoint;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get time series data for an endpoint within a time range.
     *
     * @param endpoint the endpoint name
     * @param startTime start timestamp in milliseconds
     * @param endTime end timestamp in milliseconds
     * @return list of time-value pairs
     */
    public List<Map<String, Object>> getTimeSeriesData(String endpoint, long startTime, long endTime) {
        List<EndpointMetrics> metrics = metricsStorage.getMetrics(endpoint, startTime, endTime);
        
        return metrics.stream()
                .sorted(Comparator.comparingLong(EndpointMetrics::getTimestamp))
                .map(m -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("timestamp", m.getTimestamp());
                    dataPoint.put("responseTime", m.getDuration() / 1_000_000.0);
                    dataPoint.put("statusCode", m.getStatusCode());
                    return dataPoint;
                })
                .collect(Collectors.toList());
    }
}
