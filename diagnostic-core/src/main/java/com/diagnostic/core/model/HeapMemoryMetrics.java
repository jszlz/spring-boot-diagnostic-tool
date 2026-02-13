package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 堆内存指标模型
 * Heap memory metrics including different generations
 */
public class HeapMemoryMetrics {
    private MemoryRegion oldGen;
    private MemoryRegion youngGen;
    private MemoryRegion edenSpace;
    private MemoryRegion survivorSpace;

    public HeapMemoryMetrics() {
    }

    private HeapMemoryMetrics(Builder builder) {
        this.oldGen = builder.oldGen;
        this.youngGen = builder.youngGen;
        this.edenSpace = builder.edenSpace;
        this.survivorSpace = builder.survivorSpace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public MemoryRegion getOldGen() {
        return oldGen;
    }

    public void setOldGen(MemoryRegion oldGen) {
        this.oldGen = oldGen;
    }

    public MemoryRegion getYoungGen() {
        return youngGen;
    }

    public void setYoungGen(MemoryRegion youngGen) {
        this.youngGen = youngGen;
    }

    public MemoryRegion getEdenSpace() {
        return edenSpace;
    }

    public void setEdenSpace(MemoryRegion edenSpace) {
        this.edenSpace = edenSpace;
    }

    public MemoryRegion getSurvivorSpace() {
        return survivorSpace;
    }

    public void setSurvivorSpace(MemoryRegion survivorSpace) {
        this.survivorSpace = survivorSpace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeapMemoryMetrics that = (HeapMemoryMetrics) o;
        return Objects.equals(oldGen, that.oldGen) &&
                Objects.equals(youngGen, that.youngGen) &&
                Objects.equals(edenSpace, that.edenSpace) &&
                Objects.equals(survivorSpace, that.survivorSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldGen, youngGen, edenSpace, survivorSpace);
    }

    public static class Builder {
        private MemoryRegion oldGen;
        private MemoryRegion youngGen;
        private MemoryRegion edenSpace;
        private MemoryRegion survivorSpace;

        public Builder oldGen(MemoryRegion oldGen) {
            this.oldGen = oldGen;
            return this;
        }

        public Builder youngGen(MemoryRegion youngGen) {
            this.youngGen = youngGen;
            return this;
        }

        public Builder edenSpace(MemoryRegion edenSpace) {
            this.edenSpace = edenSpace;
            return this;
        }

        public Builder survivorSpace(MemoryRegion survivorSpace) {
            this.survivorSpace = survivorSpace;
            return this;
        }

        public HeapMemoryMetrics build() {
            return new HeapMemoryMetrics(this);
        }
    }
}
