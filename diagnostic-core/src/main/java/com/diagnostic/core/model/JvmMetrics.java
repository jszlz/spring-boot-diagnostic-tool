package com.diagnostic.core.model;

import java.util.Objects;

/**
 * JVM 指标聚合模型
 * Aggregate model for all JVM and system metrics
 */
public class JvmMetrics {
    private long timestamp;
    private HeapMemoryMetrics heapMemory;
    private NonHeapMemoryMetrics nonHeapMemory;
    private GarbageCollectionMetrics gc;
    private ThreadMetrics threads;
    private CpuMetrics cpu;
    private SystemResourceMetrics system;

    public JvmMetrics() {
    }

    private JvmMetrics(Builder builder) {
        this.timestamp = builder.timestamp;
        this.heapMemory = builder.heapMemory;
        this.nonHeapMemory = builder.nonHeapMemory;
        this.gc = builder.gc;
        this.threads = builder.threads;
        this.cpu = builder.cpu;
        this.system = builder.system;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public HeapMemoryMetrics getHeapMemory() {
        return heapMemory;
    }

    public void setHeapMemory(HeapMemoryMetrics heapMemory) {
        this.heapMemory = heapMemory;
    }

    public NonHeapMemoryMetrics getNonHeapMemory() {
        return nonHeapMemory;
    }

    public void setNonHeapMemory(NonHeapMemoryMetrics nonHeapMemory) {
        this.nonHeapMemory = nonHeapMemory;
    }

    public GarbageCollectionMetrics getGc() {
        return gc;
    }

    public void setGc(GarbageCollectionMetrics gc) {
        this.gc = gc;
    }

    public ThreadMetrics getThreads() {
        return threads;
    }

    public void setThreads(ThreadMetrics threads) {
        this.threads = threads;
    }

    public CpuMetrics getCpu() {
        return cpu;
    }

    public void setCpu(CpuMetrics cpu) {
        this.cpu = cpu;
    }

    public SystemResourceMetrics getSystem() {
        return system;
    }

    public void setSystem(SystemResourceMetrics system) {
        this.system = system;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JvmMetrics that = (JvmMetrics) o;
        return timestamp == that.timestamp &&
                Objects.equals(heapMemory, that.heapMemory) &&
                Objects.equals(nonHeapMemory, that.nonHeapMemory) &&
                Objects.equals(gc, that.gc) &&
                Objects.equals(threads, that.threads) &&
                Objects.equals(cpu, that.cpu) &&
                Objects.equals(system, that.system);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, heapMemory, nonHeapMemory, gc, threads, cpu, system);
    }

    public static class Builder {
        private long timestamp;
        private HeapMemoryMetrics heapMemory;
        private NonHeapMemoryMetrics nonHeapMemory;
        private GarbageCollectionMetrics gc;
        private ThreadMetrics threads;
        private CpuMetrics cpu;
        private SystemResourceMetrics system;

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder heapMemory(HeapMemoryMetrics heapMemory) {
            this.heapMemory = heapMemory;
            return this;
        }

        public Builder nonHeapMemory(NonHeapMemoryMetrics nonHeapMemory) {
            this.nonHeapMemory = nonHeapMemory;
            return this;
        }

        public Builder gc(GarbageCollectionMetrics gc) {
            this.gc = gc;
            return this;
        }

        public Builder threads(ThreadMetrics threads) {
            this.threads = threads;
            return this;
        }

        public Builder cpu(CpuMetrics cpu) {
            this.cpu = cpu;
            return this;
        }

        public Builder system(SystemResourceMetrics system) {
            this.system = system;
            return this;
        }

        public JvmMetrics build() {
            return new JvmMetrics(this);
        }
    }
}
