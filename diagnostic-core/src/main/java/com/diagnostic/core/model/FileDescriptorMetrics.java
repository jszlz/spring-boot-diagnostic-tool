package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 文件描述符指标模型
 * File descriptor metrics (Unix-like systems only)
 */
public class FileDescriptorMetrics {
    private long openCount;
    private long maxCount;
    private double usagePercent;

    public FileDescriptorMetrics() {
    }

    private FileDescriptorMetrics(Builder builder) {
        this.openCount = builder.openCount;
        this.maxCount = builder.maxCount;
        this.usagePercent = calculateUsagePercent(builder.openCount, builder.maxCount);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 计算文件描述符使用百分比
     * Calculate file descriptor usage percentage
     */
    private double calculateUsagePercent(long openCount, long maxCount) {
        if (maxCount > 0) {
            return (double) openCount / maxCount * 100.0;
        }
        return 0.0;
    }

    public long getOpenCount() {
        return openCount;
    }

    public void setOpenCount(long openCount) {
        this.openCount = openCount;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }

    public double getUsagePercent() {
        return usagePercent;
    }

    public void setUsagePercent(double usagePercent) {
        this.usagePercent = usagePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescriptorMetrics that = (FileDescriptorMetrics) o;
        return openCount == that.openCount &&
                maxCount == that.maxCount &&
                Double.compare(that.usagePercent, usagePercent) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(openCount, maxCount, usagePercent);
    }

    public static class Builder {
        private long openCount;
        private long maxCount;

        public Builder openCount(long openCount) {
            this.openCount = openCount;
            return this;
        }

        public Builder maxCount(long maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public FileDescriptorMetrics build() {
            return new FileDescriptorMetrics(this);
        }
    }
}
