package com.diagnostic.core.model;

import java.util.Objects;

/**
 * GC 收集器指标模型
 * Metrics for a specific garbage collector
 */
public class GcCollectorMetrics {
    private String collectorName;
    private long collectionCount;
    private long collectionTime;    // milliseconds
    private double avgPauseTime;    // milliseconds

    public GcCollectorMetrics() {
    }

    private GcCollectorMetrics(Builder builder) {
        this.collectorName = builder.collectorName;
        this.collectionCount = builder.collectionCount;
        this.collectionTime = builder.collectionTime;
        this.avgPauseTime = calculateAvgPauseTime(builder.collectionTime, builder.collectionCount);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 计算平均暂停时间
     * Calculate average pause time
     */
    private double calculateAvgPauseTime(long collectionTime, long collectionCount) {
        if (collectionCount > 0) {
            return (double) collectionTime / collectionCount;
        }
        return 0.0;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public double getAvgPauseTime() {
        return avgPauseTime;
    }

    public void setAvgPauseTime(double avgPauseTime) {
        this.avgPauseTime = avgPauseTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GcCollectorMetrics that = (GcCollectorMetrics) o;
        return collectionCount == that.collectionCount &&
                collectionTime == that.collectionTime &&
                Double.compare(that.avgPauseTime, avgPauseTime) == 0 &&
                Objects.equals(collectorName, that.collectorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectorName, collectionCount, collectionTime, avgPauseTime);
    }

    public static class Builder {
        private String collectorName;
        private long collectionCount;
        private long collectionTime;

        public Builder collectorName(String collectorName) {
            this.collectorName = collectorName;
            return this;
        }

        public Builder collectionCount(long collectionCount) {
            this.collectionCount = collectionCount;
            return this;
        }

        public Builder collectionTime(long collectionTime) {
            this.collectionTime = collectionTime;
            return this;
        }

        public GcCollectorMetrics build() {
            return new GcCollectorMetrics(this);
        }
    }
}
