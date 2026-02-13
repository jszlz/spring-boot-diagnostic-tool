package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 线程指标模型
 * Thread pool metrics
 */
public class ThreadMetrics {
    private int threadCount;
    private int peakThreadCount;
    private int daemonThreadCount;
    private long totalStartedThreadCount;

    public ThreadMetrics() {
    }

    private ThreadMetrics(Builder builder) {
        this.threadCount = builder.threadCount;
        this.peakThreadCount = builder.peakThreadCount;
        this.daemonThreadCount = builder.daemonThreadCount;
        this.totalStartedThreadCount = builder.totalStartedThreadCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(int peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(int daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public void setTotalStartedThreadCount(long totalStartedThreadCount) {
        this.totalStartedThreadCount = totalStartedThreadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadMetrics that = (ThreadMetrics) o;
        return threadCount == that.threadCount &&
                peakThreadCount == that.peakThreadCount &&
                daemonThreadCount == that.daemonThreadCount &&
                totalStartedThreadCount == that.totalStartedThreadCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadCount, peakThreadCount, daemonThreadCount, totalStartedThreadCount);
    }

    public static class Builder {
        private int threadCount;
        private int peakThreadCount;
        private int daemonThreadCount;
        private long totalStartedThreadCount;

        public Builder threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder peakThreadCount(int peakThreadCount) {
            this.peakThreadCount = peakThreadCount;
            return this;
        }

        public Builder daemonThreadCount(int daemonThreadCount) {
            this.daemonThreadCount = daemonThreadCount;
            return this;
        }

        public Builder totalStartedThreadCount(long totalStartedThreadCount) {
            this.totalStartedThreadCount = totalStartedThreadCount;
            return this;
        }

        public ThreadMetrics build() {
            return new ThreadMetrics(this);
        }
    }
}
