package com.diagnostic.core.collector;

import com.diagnostic.core.model.HeapMemoryMetrics;
import com.diagnostic.core.model.MemoryRegion;
import com.diagnostic.core.model.NonHeapMemoryMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * 内存收集器
 * Collector for heap and non-heap memory metrics using MXBeans
 */
public class MemoryCollector {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCollector.class);

    private final MemoryMXBean memoryMXBean;
    private final List<MemoryPoolMXBean> memoryPoolMXBeans;

    public MemoryCollector() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * 收集堆内存指标
     * Collect heap memory metrics including Old Gen, Young Gen, Eden, and Survivor spaces
     */
    public HeapMemoryMetrics collectHeapMetrics() {
        try {
            HeapMemoryMetrics.Builder builder = HeapMemoryMetrics.builder();

            for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
                if (!pool.getType().toString().equals("HEAP")) {
                    continue;
                }

                String poolName = pool.getName();
                MemoryRegion region = collectMemoryPool(pool);

                if (region == null) {
                    continue;
                }

                // Match pool names for different GC implementations
                if (isOldGenPool(poolName)) {
                    builder.oldGen(region);
                } else if (isYoungGenPool(poolName)) {
                    builder.youngGen(region);
                } else if (isEdenPool(poolName)) {
                    builder.edenSpace(region);
                } else if (isSurvivorPool(poolName)) {
                    builder.survivorSpace(region);
                }
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Error collecting heap memory metrics: {}", e.getMessage());
            return new HeapMemoryMetrics();
        }
    }

    /**
     * 收集非堆内存指标
     * Collect non-heap memory metrics including Metaspace, Code Cache, and Compressed Class Space
     */
    public NonHeapMemoryMetrics collectNonHeapMetrics() {
        try {
            NonHeapMemoryMetrics.Builder builder = NonHeapMemoryMetrics.builder();

            for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
                if (!pool.getType().toString().equals("NON_HEAP")) {
                    continue;
                }

                String poolName = pool.getName();
                MemoryRegion region = collectMemoryPool(pool);

                if (region == null) {
                    continue;
                }

                // Match pool names for non-heap regions
                if (isMetaspacePool(poolName)) {
                    builder.metaspace(region);
                } else if (isCodeCachePool(poolName)) {
                    builder.codeCache(region);
                } else if (isCompressedClassSpacePool(poolName)) {
                    builder.compressedClassSpace(region);
                }
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Error collecting non-heap memory metrics: {}", e.getMessage());
            return new NonHeapMemoryMetrics();
        }
    }

    /**
     * 收集单个内存池的指标
     * Collect metrics for a single memory pool
     */
    private MemoryRegion collectMemoryPool(MemoryPoolMXBean pool) {
        try {
            MemoryUsage usage = pool.getUsage();
            if (usage == null) {
                return null;
            }

            return MemoryRegion.builder()
                    .name(pool.getName())
                    .used(usage.getUsed())
                    .max(usage.getMax())
                    .committed(usage.getCommitted())
                    .build();
        } catch (Exception e) {
            logger.warn("Error collecting memory pool '{}': {}", pool.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否为老年代内存池
     * Check if pool is Old Generation
     * Patterns: "PS Old Gen", "G1 Old Gen", "Tenured Gen", "CMS Old Gen"
     */
    private boolean isOldGenPool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("old") || 
               lowerName.contains("tenured") ||
               (lowerName.contains("cms") && !lowerName.contains("perm"));
    }

    /**
     * 检查是否为年轻代内存池
     * Check if pool is Young Generation
     * Patterns: "PS Young Gen", "G1 Young Gen", "Par Young Gen"
     */
    private boolean isYoungGenPool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("young") && !lowerName.contains("eden") && !lowerName.contains("survivor");
    }

    /**
     * 检查是否为伊甸园区
     * Check if pool is Eden Space
     * Patterns: "PS Eden Space", "G1 Eden Space", "Par Eden Space", "Eden Space"
     */
    private boolean isEdenPool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("eden");
    }

    /**
     * 检查是否为幸存者区
     * Check if pool is Survivor Space
     * Patterns: "PS Survivor Space", "G1 Survivor Space", "Par Survivor Space", "Survivor Space"
     */
    private boolean isSurvivorPool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("survivor");
    }

    /**
     * 检查是否为元空间
     * Check if pool is Metaspace
     * Patterns: "Metaspace"
     */
    private boolean isMetaspacePool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("metaspace") && !lowerName.contains("compressed");
    }

    /**
     * 检查是否为代码缓存
     * Check if pool is Code Cache
     * Patterns: "Code Cache"
     */
    private boolean isCodeCachePool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("code cache") || lowerName.equals("code cache");
    }

    /**
     * 检查是否为压缩类空间
     * Check if pool is Compressed Class Space
     * Patterns: "Compressed Class Space"
     */
    private boolean isCompressedClassSpacePool(String poolName) {
        String lowerName = poolName.toLowerCase();
        return lowerName.contains("compressed class");
    }
}
