package com.diagnostic.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthReport {
    private long timestamp;
    private DependencyTopology topology;
    private List<ArchitectureRisk> risks;
    private Map<String, EndpointStatistics> endpointStatistics;
    private Map<String, TrendAnalysis> trends;

    public HealthReport() {
        this.endpointStatistics = new HashMap<>();
        this.trends = new HashMap<>();
    }

    private HealthReport(Builder builder) {
        this.timestamp = builder.timestamp;
        this.topology = builder.topology;
        this.risks = builder.risks;
        this.endpointStatistics = builder.endpointStatistics;
        this.trends = builder.trends;
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

    public DependencyTopology getTopology() {
        return topology;
    }

    public void setTopology(DependencyTopology topology) {
        this.topology = topology;
    }

    public List<ArchitectureRisk> getRisks() {
        return risks;
    }

    public void setRisks(List<ArchitectureRisk> risks) {
        this.risks = risks;
    }

    public Map<String, EndpointStatistics> getEndpointStatistics() {
        return endpointStatistics;
    }

    public void setEndpointStatistics(Map<String, EndpointStatistics> endpointStatistics) {
        this.endpointStatistics = endpointStatistics;
    }

    public Map<String, TrendAnalysis> getTrends() {
        return trends;
    }

    public void setTrends(Map<String, TrendAnalysis> trends) {
        this.trends = trends;
    }

    public static class Builder {
        private long timestamp;
        private DependencyTopology topology;
        private List<ArchitectureRisk> risks;
        private Map<String, EndpointStatistics> endpointStatistics = new HashMap<>();
        private Map<String, TrendAnalysis> trends = new HashMap<>();

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder topology(DependencyTopology topology) {
            this.topology = topology;
            return this;
        }

        public Builder risks(List<ArchitectureRisk> risks) {
            this.risks = risks;
            return this;
        }

        public Builder endpointStatistics(Map<String, EndpointStatistics> endpointStatistics) {
            this.endpointStatistics = endpointStatistics;
            return this;
        }

        public Builder trends(Map<String, TrendAnalysis> trends) {
            this.trends = trends;
            return this;
        }

        public HealthReport build() {
            return new HealthReport(this);
        }
    }
}
