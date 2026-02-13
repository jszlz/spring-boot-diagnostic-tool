package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 非堆内存指标模型
 * Non-heap memory metrics including metaspace, code cache, etc.
 */
public class NonHeapMemoryMetrics {
    private MemoryRegion metaspace;
    private MemoryRegion codeCache;
    private MemoryRegion compressedClassSpace;

    public NonHeapMemoryMetrics() {
    }

    private NonHeapMemoryMetrics(Builder builder) {
        this.metaspace = builder.metaspace;
        this.codeCache = builder.codeCache;
        this.compressedClassSpace = builder.compressedClassSpace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public MemoryRegion getMetaspace() {
        return metaspace;
    }

    public void setMetaspace(MemoryRegion metaspace) {
        this.metaspace = metaspace;
    }

    public MemoryRegion getCodeCache() {
        return codeCache;
    }

    public void setCodeCache(MemoryRegion codeCache) {
        this.codeCache = codeCache;
    }

    public MemoryRegion getCompressedClassSpace() {
        return compressedClassSpace;
    }

    public void setCompressedClassSpace(MemoryRegion compressedClassSpace) {
        this.compressedClassSpace = compressedClassSpace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonHeapMemoryMetrics that = (NonHeapMemoryMetrics) o;
        return Objects.equals(metaspace, that.metaspace) &&
                Objects.equals(codeCache, that.codeCache) &&
                Objects.equals(compressedClassSpace, that.compressedClassSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaspace, codeCache, compressedClassSpace);
    }

    public static class Builder {
        private MemoryRegion metaspace;
        private MemoryRegion codeCache;
        private MemoryRegion compressedClassSpace;

        public Builder metaspace(MemoryRegion metaspace) {
            this.metaspace = metaspace;
            return this;
        }

        public Builder codeCache(MemoryRegion codeCache) {
            this.codeCache = codeCache;
            return this;
        }

        public Builder compressedClassSpace(MemoryRegion compressedClassSpace) {
            this.compressedClassSpace = compressedClassSpace;
            return this;
        }

        public NonHeapMemoryMetrics build() {
            return new NonHeapMemoryMetrics(this);
        }
    }
}
