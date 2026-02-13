package com.diagnostic.core.model;

import java.util.Objects;

/**
 * 系统资源指标模型
 * System resource metrics including memory and file descriptors
 */
public class SystemResourceMetrics {
    private int availableProcessors;
    private long totalPhysicalMemory;      // bytes
    private long freePhysicalMemory;       // bytes
    private long committedVirtualMemory;   // bytes
    private FileDescriptorMetrics fileDescriptors; // nullable

    public SystemResourceMetrics() {
    }

    private SystemResourceMetrics(Builder builder) {
        this.availableProcessors = builder.availableProcessors;
        this.totalPhysicalMemory = builder.totalPhysicalMemory;
        this.freePhysicalMemory = builder.freePhysicalMemory;
        this.committedVirtualMemory = builder.committedVirtualMemory;
        this.fileDescriptors = builder.fileDescriptors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public long getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(long totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public long getFreePhysicalMemory() {
        return freePhysicalMemory;
    }

    public void setFreePhysicalMemory(long freePhysicalMemory) {
        this.freePhysicalMemory = freePhysicalMemory;
    }

    public long getCommittedVirtualMemory() {
        return committedVirtualMemory;
    }

    public void setCommittedVirtualMemory(long committedVirtualMemory) {
        this.committedVirtualMemory = committedVirtualMemory;
    }

    public FileDescriptorMetrics getFileDescriptors() {
        return fileDescriptors;
    }

    public void setFileDescriptors(FileDescriptorMetrics fileDescriptors) {
        this.fileDescriptors = fileDescriptors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemResourceMetrics that = (SystemResourceMetrics) o;
        return availableProcessors == that.availableProcessors &&
                totalPhysicalMemory == that.totalPhysicalMemory &&
                freePhysicalMemory == that.freePhysicalMemory &&
                committedVirtualMemory == that.committedVirtualMemory &&
                Objects.equals(fileDescriptors, that.fileDescriptors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(availableProcessors, totalPhysicalMemory, freePhysicalMemory, 
                           committedVirtualMemory, fileDescriptors);
    }

    public static class Builder {
        private int availableProcessors;
        private long totalPhysicalMemory;
        private long freePhysicalMemory;
        private long committedVirtualMemory;
        private FileDescriptorMetrics fileDescriptors;

        public Builder availableProcessors(int availableProcessors) {
            this.availableProcessors = availableProcessors;
            return this;
        }

        public Builder totalPhysicalMemory(long totalPhysicalMemory) {
            this.totalPhysicalMemory = totalPhysicalMemory;
            return this;
        }

        public Builder freePhysicalMemory(long freePhysicalMemory) {
            this.freePhysicalMemory = freePhysicalMemory;
            return this;
        }

        public Builder committedVirtualMemory(long committedVirtualMemory) {
            this.committedVirtualMemory = committedVirtualMemory;
            return this;
        }

        public Builder fileDescriptors(FileDescriptorMetrics fileDescriptors) {
            this.fileDescriptors = fileDescriptors;
            return this;
        }

        public SystemResourceMetrics build() {
            return new SystemResourceMetrics(this);
        }
    }
}
