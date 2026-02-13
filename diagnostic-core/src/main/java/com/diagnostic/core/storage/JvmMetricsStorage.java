package com.diagnostic.core.storage;

import com.diagnostic.core.model.JvmMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * JVM 指标时序存储
 * Time-series storage for JVM metrics with in-memory caching
 * (Disk persistence will be implemented in task 7)
 */
public class JvmMetricsStorage {

    private static final Logger logger = LoggerFactory.getLogger(JvmMetricsStorage.class);

    private final ConcurrentHashMap<String, List<JvmMetrics>> timeSeriesCache;
    private final int maxMemoryDataPoints;
    private final int retentionDays;

    private static final String DEFAULT_SERIES = "default";

    public JvmMetricsStorage(int maxMemoryDataPoints, int retentionDays) {
        this.timeSeriesCache = new ConcurrentHashMap<>();
        this.maxMemoryDataPoints = maxMemoryDataPoints;
        this.retentionDays = retentionDays;
    }

    /**
     * 存储 JVM 指标
     * Store JVM metrics
     */
    public void store(JvmMetrics metrics) {
        if (metrics == null) {
            return;
        }

        try {
            List<JvmMetrics> series = timeSeriesCache.computeIfAbsent(
                    DEFAULT_SERIES,
                    k -> new CopyOnWriteArrayList<>()
            );

            series.add(metrics);

            // Check if we need to flush to disk (will be implemented in task 7)
            if (series.size() > maxMemoryDataPoints) {
                logger.debug("Memory cache limit reached, oldest data points will be removed");
                // For now, just remove oldest entries to prevent OOM
                while (series.size() > maxMemoryDataPoints) {
                    series.remove(0);
                }
            }
        } catch (Exception e) {
            logger.error("Error storing JVM metrics: {}", e.getMessage());
        }
    }

    /**
     * 获取最新的指标
     * Get the latest metrics
     */
    public JvmMetrics getLatest() {
        List<JvmMetrics> series = timeSeriesCache.get(DEFAULT_SERIES);
        if (series == null || series.isEmpty()) {
            return null;
        }
        return series.get(series.size() - 1);
    }

    /**
     * 获取指定时间范围的指标
     * Get metrics within a time range
     */
    public List<JvmMetrics> getMetrics(long startTime, long endTime) {
        List<JvmMetrics> series = timeSeriesCache.get(DEFAULT_SERIES);
        if (series == null || series.isEmpty()) {
            return Collections.emptyList();
        }

        return series.stream()
                .filter(m -> m.getTimestamp() >= startTime && m.getTimestamp() <= endTime)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有指标
     * Get all metrics
     */
    public List<JvmMetrics> getAllMetrics() {
        List<JvmMetrics> series = timeSeriesCache.get(DEFAULT_SERIES);
        if (series == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(series);
    }

    /**
     * 清空缓存
     * Clear cache
     */
    public void clear() {
        timeSeriesCache.clear();
        logger.info("JVM metrics cache cleared");
    }

    /**
     * 获取缓存中的数据点数量
     * Get number of data points in cache
     */
    public int size() {
        List<JvmMetrics> series = timeSeriesCache.get(DEFAULT_SERIES);
        return series == null ? 0 : series.size();
    }
}
