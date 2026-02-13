package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 垃圾回收指标模型
 * Garbage collection metrics including young and full GC
 */
public class GarbageCollectionMetrics {
    private GcCollectorMetrics youngGc;
    private GcCollectorMetrics fullGc;
    private double gcTimePercent; // Percentage of total uptime spent in GC

    public GarbageCollectionMetrics() {
    }

    private GarbageCollectionMetrics(Builder builder) {
        this.youngGc = builder.youngGc;
        this.fullGc = builder.fullGc;
        this.gcTimePercent = builder.gcTimePercent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public GcCollectorMetrics getYoungGc() {
        return youngGc;
    }

    public void setYoungGc(GcCollectorMetrics youngGc) {
        this.youngGc = youngGc;
    }

    public GcCollectorMetrics getFullGc() {
        return fullGc;
    }

    public void setFullGc(GcCollectorMetrics fullGc) {
        this.fullGc = fullGc;
    }

    public double getGcTimePercent() {
        return gcTimePercent;
    }

    public void setGcTimePercent(double gcTimePercent) {
        this.gcTimePercent = gcTimePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageCollectionMetrics that = (GarbageCollectionMetrics) o;
        return Double.compare(that.gcTimePercent, gcTimePercent) == 0 &&
                Objects.equals(youngGc, that.youngGc) &&
                Objects.equals(fullGc, that.fullGc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(youngGc, fullGc, gcTimePercent);
    }

    public static class Builder {
        private GcCollectorMetrics youngGc;
        private GcCollectorMetrics fullGc;
        private double gcTimePercent;

        public Builder youngGc(GcCollectorMetrics youngGc) {
            this.youngGc = youngGc;
            return this;
        }

        public Builder fullGc(GcCollectorMetrics fullGc) {
            this.fullGc = fullGc;
            return this;
        }

        public Builder gcTimePercent(double gcTimePercent) {
            this.gcTimePercent = gcTimePercent;
            return this;
        }

        public GarbageCollectionMetrics build() {
            return new GarbageCollectionMetrics(this);
        }
    }
}
