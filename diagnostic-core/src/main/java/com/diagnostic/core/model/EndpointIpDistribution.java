package com.diagnostic.core.model;

import java.util.List;

/**
 * 端点IP分布信息
 */
public class EndpointIpDistribution {
    private String endpoint;              // 端点名称
    private long totalRequests;           // 总请求数
    private int uniqueIpCount;            // 唯一IP数量
    private List<IpStatistics> topIps;    // Top IP列表
    private double concentrationRate;     // 集中度（Top1 IP占比）
    private boolean isAnomalous;          // 是否异常
    private String anomalyDescription;    // 异常描述

    // Constructors
    public EndpointIpDistribution() {
    }

    private EndpointIpDistribution(Builder builder) {
        this.endpoint = builder.endpoint;
        this.totalRequests = builder.totalRequests;
        this.uniqueIpCount = builder.uniqueIpCount;
        this.topIps = builder.topIps;
        this.concentrationRate = builder.concentrationRate;
        this.isAnomalous = builder.isAnomalous;
        this.anomalyDescription = builder.anomalyDescription;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getUniqueIpCount() {
        return uniqueIpCount;
    }

    public void setUniqueIpCount(int uniqueIpCount) {
        this.uniqueIpCount = uniqueIpCount;
    }

    public List<IpStatistics> getTopIps() {
        return topIps;
    }

    public void setTopIps(List<IpStatistics> topIps) {
        this.topIps = topIps;
    }

    public double getConcentrationRate() {
        return concentrationRate;
    }

    public void setConcentrationRate(double concentrationRate) {
        this.concentrationRate = concentrationRate;
    }

    public boolean isAnomalous() {
        return isAnomalous;
    }

    public void setAnomalous(boolean anomalous) {
        isAnomalous = anomalous;
    }

    public String getAnomalyDescription() {
        return anomalyDescription;
    }

    public void setAnomalyDescription(String anomalyDescription) {
        this.anomalyDescription = anomalyDescription;
    }

    // Builder
    public static class Builder {
        private String endpoint;
        private long totalRequests;
        private int uniqueIpCount;
        private List<IpStatistics> topIps;
        private double concentrationRate;
        private boolean isAnomalous;
        private String anomalyDescription;

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder totalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder uniqueIpCount(int uniqueIpCount) {
            this.uniqueIpCount = uniqueIpCount;
            return this;
        }

        public Builder topIps(List<IpStatistics> topIps) {
            this.topIps = topIps;
            return this;
        }

        public Builder concentrationRate(double concentrationRate) {
            this.concentrationRate = concentrationRate;
            return this;
        }

        public Builder isAnomalous(boolean isAnomalous) {
            this.isAnomalous = isAnomalous;
            return this;
        }

        public Builder anomalyDescription(String anomalyDescription) {
            this.anomalyDescription = anomalyDescription;
            return this;
        }

        public EndpointIpDistribution build() {
            return new EndpointIpDistribution(this);
        }
    }
}
