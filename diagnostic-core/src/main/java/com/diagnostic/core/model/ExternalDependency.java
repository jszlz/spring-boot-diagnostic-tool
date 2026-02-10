package com.diagnostic.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExternalDependency {
    private String id;
    private DependencyType type;
    private String connectionString;
    private boolean critical;
    private boolean hasRedundancy;
    private long callCount;
    private Map<String, Object> metadata;

    public ExternalDependency() {
        this.metadata = new HashMap<>();
    }

    public ExternalDependency(String id, DependencyType type) {
        this.id = id;
        this.type = type;
        this.metadata = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DependencyType getType() {
        return type;
    }

    public void setType(DependencyType type) {
        this.type = type;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean hasRedundancy() {
        return hasRedundancy;
    }

    public void setHasRedundancy(boolean hasRedundancy) {
        this.hasRedundancy = hasRedundancy;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public void incrementCallCount() {
        this.callCount++;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalDependency that = (ExternalDependency) o;
        return Objects.equals(id, that.id) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return "ExternalDependency{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", callCount=" + callCount +
                '}';
    }
}
