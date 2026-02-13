package com.diagnostic.core.collector;

import com.diagnostic.core.model.GarbageCollectionMetrics;
import com.diagnostic.core.model.GcCollectorMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * GC 收集器
 * Collector for garbage collection metrics using MXBeans
 */
public class GcCollector {

    private static final Logger logger = LoggerFactory.getLogger(GcCollector.class);

    private final List<GarbageCollectorMXBean> gcMXBeans;
    private final RuntimeMXBean runtimeMXBean;

    public GcCollector() {
        this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    }

    /**
     * 收集垃圾回收指标
     * Collect garbage collection metrics including Young GC and Full GC
     */
    public GarbageCollectionMetrics collectGcMetrics() {
        try {
            GcCollectorMetrics youngGc = null;
            GcCollectorMetrics fullGc = null;
            long totalGcTime = 0;

            for (GarbageCollectorMXBean gcBean : gcMXBeans) {
                String collectorName = gcBean.getName();
                GcCollectorMetrics metrics = collectCollectorMetrics(gcBean);

                if (metrics == null) {
                    continue;
                }

                totalGcTime += metrics.getCollectionTime();

                if (isYoungGcCollector(collectorName)) {
                    youngGc = metrics;
                } else if (isFullGcCollector(collectorName)) {
                    fullGc = metrics;
                }
            }

            // Calculate GC time percentage
            long uptime = runtimeMXBean.getUptime();
            double gcTimePercent = calculateGcTimePercent(totalGcTime, uptime);

            return GarbageCollectionMetrics.builder()
                    .youngGc(youngGc)
                    .fullGc(fullGc)
                    .gcTimePercent(gcTimePercent)
                    .build();
        } catch (Exception e) {
            logger.error("Error collecting GC metrics: {}", e.getMessage());
            return new GarbageCollectionMetrics();
        }
    }

    /**
     * 收集单个 GC 收集器的指标
     * Collect metrics for a single GC collector
     */
    private GcCollectorMetrics collectCollectorMetrics(GarbageCollectorMXBean gcBean) {
        try {
            return GcCollectorMetrics.builder()
                    .collectorName(gcBean.getName())
                    .collectionCount(gcBean.getCollectionCount())
                    .collectionTime(gcBean.getCollectionTime())
                    .build();
        } catch (Exception e) {
            logger.warn("Error collecting GC collector '{}': {}", gcBean.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 计算 GC 时间百分比
     * Calculate GC time percentage relative to total uptime
     */
    private double calculateGcTimePercent(long totalGcTime, long uptime) {
        if (uptime > 0) {
            return (double) totalGcTime / uptime * 100.0;
        }
        return 0.0;
    }

    /**
     * 检查是否为年轻代 GC 收集器
     * Check if collector is Young GC
     * Patterns: "PS Scavenge", "ParNew", "G1 Young Generation", "Copy"
     */
    private boolean isYoungGcCollector(String collectorName) {
        String lowerName = collectorName.toLowerCase();
        return lowerName.contains("scavenge") ||
               lowerName.contains("parnew") ||
               lowerName.contains("copy") ||
               (lowerName.contains("g1") && lowerName.contains("young"));
    }

    /**
     * 检查是否为完全 GC 收集器
     * Check if collector is Full GC
     * Patterns: "PS MarkSweep", "ConcurrentMarkSweep", "G1 Old Generation", "MarkSweepCompact"
     */
    private boolean isFullGcCollector(String collectorName) {
        String lowerName = collectorName.toLowerCase();
        return lowerName.contains("marksweep") ||
               lowerName.contains("cms") ||
               lowerName.contains("concurrentmarksweep") ||
               (lowerName.contains("g1") && lowerName.contains("old")) ||
               lowerName.contains("old") ||
               lowerName.contains("tenured");
    }
}
