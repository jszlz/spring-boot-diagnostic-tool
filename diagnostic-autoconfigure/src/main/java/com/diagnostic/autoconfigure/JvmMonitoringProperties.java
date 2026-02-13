package com.diagnostic.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JVM 监控配置属性
 * Configuration properties for JVM monitoring
 */
@ConfigurationProperties(prefix = "diagnostic.jvm")
public class JvmMonitoringProperties {

    /**
     * 是否启用 JVM 监控
     * Whether JVM monitoring is enabled
     */
    private boolean enabled = true;

    /**
     * 指标收集间隔（秒）
     * Metric collection interval in seconds
     */
    private int collectionIntervalSeconds = 30;

    /**
     * 数据保留天数
     * Data retention in days
     */
    private int retentionDays = 7;

    /**
     * 内存中最大数据点数
     * Maximum data points in memory
     */
    private int maxMemoryDataPoints = 10000;

    /**
     * 堆内存使用率阈值（百分比）
     * Heap memory usage threshold (percentage)
     */
    private double heapUsageThreshold = 85.0;

    /**
     * GC 时间百分比阈值
     * GC time percentage threshold
     */
    private double gcTimeThreshold = 10.0;

    /**
     * 线程增长率阈值（百分比）
     * Thread growth rate threshold (percentage)
     */
    private double threadGrowthThreshold = 50.0;

    /**
     * CPU 使用率阈值（百分比）
     * CPU usage threshold (percentage)
     */
    private double cpuUsageThreshold = 80.0;

    /**
     * 系统内存阈值（百分比）
     * System memory threshold (percentage)
     */
    private double memoryThreshold = 10.0;

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCollectionIntervalSeconds() {
        return collectionIntervalSeconds;
    }

    public void setCollectionIntervalSeconds(int collectionIntervalSeconds) {
        this.collectionIntervalSeconds = collectionIntervalSeconds;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public int getMaxMemoryDataPoints() {
        return maxMemoryDataPoints;
    }

    public void setMaxMemoryDataPoints(int maxMemoryDataPoints) {
        this.maxMemoryDataPoints = maxMemoryDataPoints;
    }

    public double getHeapUsageThreshold() {
        return heapUsageThreshold;
    }

    public void setHeapUsageThreshold(double heapUsageThreshold) {
        this.heapUsageThreshold = heapUsageThreshold;
    }

    public double getGcTimeThreshold() {
        return gcTimeThreshold;
    }

    public void setGcTimeThreshold(double gcTimeThreshold) {
        this.gcTimeThreshold = gcTimeThreshold;
    }

    public double getThreadGrowthThreshold() {
        return threadGrowthThreshold;
    }

    public void setThreadGrowthThreshold(double threadGrowthThreshold) {
        this.threadGrowthThreshold = threadGrowthThreshold;
    }

    public double getCpuUsageThreshold() {
        return cpuUsageThreshold;
    }

    public void setCpuUsageThreshold(double cpuUsageThreshold) {
        this.cpuUsageThreshold = cpuUsageThreshold;
    }

    public double getMemoryThreshold() {
        return memoryThreshold;
    }

    public void setMemoryThreshold(double memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }
}
