package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 内存区域指标模型
 * Represents metrics for a specific memory region (heap or non-heap)
 */
public class MemoryRegion {
    private String name;
    private long used;          // bytes currently used
    private long max;           // maximum bytes (-1 if undefined)
    private long committed;     // bytes committed by JVM
    private double usagePercent; // (used / max) * 100, or (used / committed) * 100 if max is -1

    public MemoryRegion() {
    }

    private MemoryRegion(Builder builder) {
        this.name = builder.name;
        this.used = builder.used;
        this.max = builder.max;
        this.committed = builder.committed;
        this.usagePercent = calculateUsagePercent(builder.used, builder.max, builder.committed);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 计算内存使用百分比
     * Calculate memory usage percentage
     */
    private double calculateUsagePercent(long used, long max, long committed) {
        if (max > 0) {
            return (double) used / max * 100.0;
        } else if (committed > 0) {
            // If max is -1 (undefined), use committed instead
            return (double) used / committed * 100.0;
        }
        return 0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getCommitted() {
        return committed;
    }

    public void setCommitted(long committed) {
        this.committed = committed;
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
        MemoryRegion that = (MemoryRegion) o;
        return used == that.used &&
                max == that.max &&
                committed == that.committed &&
                Double.compare(that.usagePercent, usagePercent) == 0 &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, used, max, committed, usagePercent);
    }

    public static class Builder {
        private String name;
        private long used;
        private long max;
        private long committed;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder used(long used) {
            this.used = used;
            return this;
        }

        public Builder max(long max) {
            this.max = max;
            return this;
        }

        public Builder committed(long committed) {
            this.committed = committed;
            return this;
        }

        public MemoryRegion build() {
            return new MemoryRegion(this);
        }
    }
}
