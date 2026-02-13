package com.diagnostic.core.model;

import java.util.Objects;

/**
 * CPU 指标模型
 * CPU usage metrics
 */
public class CpuMetrics {
    private double processCpuUsage;  // 0.0 to 100.0
    private double systemCpuUsage;   // 0.0 to 100.0
    private double systemLoadAverage;

    public CpuMetrics() {
    }

    private CpuMetrics(Builder builder) {
        this.processCpuUsage = builder.processCpuUsage;
        this.systemCpuUsage = builder.systemCpuUsage;
        this.systemLoadAverage = builder.systemLoadAverage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getProcessCpuUsage() {
        return processCpuUsage;
    }

    public void setProcessCpuUsage(double processCpuUsage) {
        this.processCpuUsage = processCpuUsage;
    }

    public double getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(double systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CpuMetrics that = (CpuMetrics) o;
        return Double.compare(that.processCpuUsage, processCpuUsage) == 0 &&
                Double.compare(that.systemCpuUsage, systemCpuUsage) == 0 &&
                Double.compare(that.systemLoadAverage, systemLoadAverage) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(processCpuUsage, systemCpuUsage, systemLoadAverage);
    }

    public static class Builder {
        private double processCpuUsage;
        private double systemCpuUsage;
        private double systemLoadAverage;

        public Builder processCpuUsage(double processCpuUsage) {
            this.processCpuUsage = processCpuUsage;
            return this;
        }

        public Builder systemCpuUsage(double systemCpuUsage) {
            this.systemCpuUsage = systemCpuUsage;
            return this;
        }

        public Builder systemLoadAverage(double systemLoadAverage) {
            this.systemLoadAverage = systemLoadAverage;
            return this;
        }

        public CpuMetrics build() {
            return new CpuMetrics(this);
        }
    }
}
