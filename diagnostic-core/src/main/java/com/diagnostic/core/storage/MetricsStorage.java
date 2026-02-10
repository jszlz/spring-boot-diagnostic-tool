package com.diagnostic.core.storage;

import com.diagnostic.core.model.EndpointMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Storage component for performance metrics.
 * Provides both in-memory caching and disk persistence for metrics data.
 * Implements data retention policies to manage storage size.
 */
public class MetricsStorage {

    private static final Logger logger = LoggerFactory.getLogger(MetricsStorage.class);
    private static final String STORAGE_DIR = "./diagnostic-data/metrics";
    private static final int MAX_MEMORY_METRICS_PER_ENDPOINT = 10000;

    private final Map<String, List<EndpointMetrics>> memoryCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int dataRetentionDays;

    public MetricsStorage() {
        this(7); // Default 7 days retention
    }

    public MetricsStorage(int dataRetentionDays) {
        this.dataRetentionDays = dataRetentionDays;
        ensureStorageDirectoryExists();
    }

    /**
     * Store a metric in memory cache.
     * If memory limit is reached, flush old data to disk.
     *
     * @param metrics the metrics to store
     */
    public void store(EndpointMetrics metrics) {
        if (metrics == null || metrics.getEndpoint() == null) {
            logger.warn("Attempted to store null metrics or metrics with null endpoint");
            return;
        }

        String endpoint = metrics.getEndpoint();
        memoryCache.computeIfAbsent(endpoint, k -> Collections.synchronizedList(new ArrayList<>()))
                   .add(metrics);

        // Check if we need to flush to disk
        List<EndpointMetrics> endpointMetrics = memoryCache.get(endpoint);
        if (endpointMetrics.size() > MAX_MEMORY_METRICS_PER_ENDPOINT) {
            flushToDisk(endpoint);
        }
    }

    /**
     * Get all metrics for a specific endpoint from memory cache.
     *
     * @param endpoint the endpoint name
     * @return list of metrics
     */
    public List<EndpointMetrics> getMetrics(String endpoint) {
        List<EndpointMetrics> metrics = memoryCache.get(endpoint);
        return metrics != null ? new ArrayList<>(metrics) : new ArrayList<>();
    }

    /**
     * Get all metrics for a specific endpoint within a time range.
     *
     * @param endpoint the endpoint name
     * @param startTime start timestamp in milliseconds
     * @param endTime end timestamp in milliseconds
     * @return list of metrics within the time range
     */
    public List<EndpointMetrics> getMetrics(String endpoint, long startTime, long endTime) {
        List<EndpointMetrics> allMetrics = getMetrics(endpoint);
        return allMetrics.stream()
                .filter(m -> m.getTimestamp() >= startTime && m.getTimestamp() <= endTime)
                .collect(Collectors.toList());
    }

    /**
     * Get all endpoints that have metrics stored.
     *
     * @return set of endpoint names
     */
    public Set<String> getAllEndpoints() {
        return new HashSet<>(memoryCache.keySet());
    }

    /**
     * Flush metrics for a specific endpoint to disk.
     *
     * @param endpoint the endpoint name
     */
    public void flushToDisk(String endpoint) {
        List<EndpointMetrics> metrics = memoryCache.get(endpoint);
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        try {
            String fileName = generateFileName(endpoint);
            Path filePath = Paths.get(STORAGE_DIR, fileName);
            
            // Read existing data if file exists
            List<EndpointMetrics> existingMetrics = new ArrayList<>();
            if (Files.exists(filePath)) {
                existingMetrics = readMetricsFromFile(filePath.toFile());
            }

            // Append new metrics
            existingMetrics.addAll(metrics);

            // Write to file
            objectMapper.writeValue(filePath.toFile(), existingMetrics);
            
            // Clear memory cache for this endpoint
            memoryCache.get(endpoint).clear();
            
            logger.debug("Flushed {} metrics for endpoint {} to disk", metrics.size(), endpoint);
        } catch (IOException e) {
            logger.error("Failed to flush metrics to disk for endpoint {}: {}", endpoint, e.getMessage());
        }
    }

    /**
     * Flush all metrics to disk.
     */
    public void flushAll() {
        for (String endpoint : memoryCache.keySet()) {
            flushToDisk(endpoint);
        }
    }

    /**
     * Load metrics from disk for a specific endpoint.
     *
     * @param endpoint the endpoint name
     * @return list of metrics loaded from disk
     */
    public List<EndpointMetrics> loadFromDisk(String endpoint) {
        try {
            String fileName = generateFileName(endpoint);
            Path filePath = Paths.get(STORAGE_DIR, fileName);
            
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }

            return readMetricsFromFile(filePath.toFile());
        } catch (IOException e) {
            logger.error("Failed to load metrics from disk for endpoint {}: {}", endpoint, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Clean up old data based on retention policy.
     */
    public void cleanupOldData() {
        try {
            File storageDir = new File(STORAGE_DIR);
            if (!storageDir.exists()) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (dataRetentionDays * 24L * 60 * 60 * 1000);
            LocalDate cutoffDate = Instant.ofEpochMilli(cutoffTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            File[] files = storageDir.listFiles();
            if (files == null) {
                return;
            }

            int deletedCount = 0;
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    // Extract date from filename (format: metrics-endpoint-yyyy-MM-dd.json)
                    String[] parts = file.getName().split("-");
                    if (parts.length >= 4) {
                        try {
                            String dateStr = parts[parts.length - 3] + "-" + 
                                           parts[parts.length - 2] + "-" + 
                                           parts[parts.length - 1].replace(".json", "");
                            LocalDate fileDate = LocalDate.parse(dateStr);
                            
                            if (fileDate.isBefore(cutoffDate)) {
                                if (file.delete()) {
                                    deletedCount++;
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Could not parse date from filename: {}", file.getName());
                        }
                    }
                }
            }

            if (deletedCount > 0) {
                logger.info("Cleaned up {} old metric files", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage());
        }
    }

    /**
     * Clear all metrics from memory cache.
     */
    public void clearMemoryCache() {
        memoryCache.clear();
        logger.info("Cleared all metrics from memory cache");
    }

    /**
     * Get the current size of memory cache for a specific endpoint.
     *
     * @param endpoint the endpoint name
     * @return number of metrics in memory
     */
    public int getMemoryCacheSize(String endpoint) {
        List<EndpointMetrics> metrics = memoryCache.get(endpoint);
        return metrics != null ? metrics.size() : 0;
    }

    private void ensureStorageDirectoryExists() {
        try {
            Path path = Paths.get(STORAGE_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created storage directory: {}", STORAGE_DIR);
            }
        } catch (IOException e) {
            logger.error("Failed to create storage directory: {}", e.getMessage());
        }
    }

    private String generateFileName(String endpoint) {
        String sanitizedEndpoint = endpoint.replaceAll("[^a-zA-Z0-9-]", "_");
        LocalDate today = LocalDate.now();
        return String.format("metrics-%s-%s.json", sanitizedEndpoint, today);
    }

    private List<EndpointMetrics> readMetricsFromFile(File file) throws IOException {
        EndpointMetrics[] metricsArray = objectMapper.readValue(file, EndpointMetrics[].class);
        return new ArrayList<>(Arrays.asList(metricsArray));
    }
}
