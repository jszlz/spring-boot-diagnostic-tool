package com.diagnostic.core.model;

import java.util.List;

/**
 * IP访问统计信息
 */
public class IpStatistics {
    private String ip;                      // IP地址
    private long requestCount;              // 请求次数
    private long errorCount;                // 错误次数
    private double errorRate;               // 错误率
    private double avgResponseTime;         // 平均响应时间(ms)
    private long firstRequestTime;          // 首次请求时间
    private long lastRequestTime;           // 最后请求时间
    private List<String> accessedEndpoints; // 访问的端点列表
    private String userAgent;               // 用户代理
    private AnomalyLevel anomalyLevel;      // 异常等级
    private String anomalyReason;           // 异常原因
    private String country;                 // 国家
    private String region;                  // 区域
    private String province;                // 省份
    private String city;                    // 城市
    private String isp;                     // ISP

    public enum AnomalyLevel {
        NORMAL,      // 正常
        SUSPICIOUS,  // 可疑
        DANGEROUS    // 危险
    }

    // Constructors
    public IpStatistics() {
    }

    private IpStatistics(Builder builder) {
        this.ip = builder.ip;
        this.requestCount = builder.requestCount;
        this.errorCount = builder.errorCount;
        this.errorRate = builder.errorRate;
        this.avgResponseTime = builder.avgResponseTime;
        this.firstRequestTime = builder.firstRequestTime;
        this.lastRequestTime = builder.lastRequestTime;
        this.accessedEndpoints = builder.accessedEndpoints;
        this.userAgent = builder.userAgent;
        this.anomalyLevel = builder.anomalyLevel;
        this.anomalyReason = builder.anomalyReason;
        this.country = builder.country;
        this.region = builder.region;
        this.province = builder.province;
        this.city = builder.city;
        this.isp = builder.isp;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public long getFirstRequestTime() {
        return firstRequestTime;
    }

    public void setFirstRequestTime(long firstRequestTime) {
        this.firstRequestTime = firstRequestTime;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }

    public List<String> getAccessedEndpoints() {
        return accessedEndpoints;
    }

    public void setAccessedEndpoints(List<String> accessedEndpoints) {
        this.accessedEndpoints = accessedEndpoints;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public AnomalyLevel getAnomalyLevel() {
        return anomalyLevel;
    }

    public void setAnomalyLevel(AnomalyLevel anomalyLevel) {
        this.anomalyLevel = anomalyLevel;
    }

    public String getAnomalyReason() {
        return anomalyReason;
    }

    public void setAnomalyReason(String anomalyReason) {
        this.anomalyReason = anomalyReason;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    // Builder
    public static class Builder {
        private String ip;
        private long requestCount;
        private long errorCount;
        private double errorRate;
        private double avgResponseTime;
        private long firstRequestTime;
        private long lastRequestTime;
        private List<String> accessedEndpoints;
        private String userAgent;
        private AnomalyLevel anomalyLevel;
        private String anomalyReason;
        private String country;
        private String region;
        private String province;
        private String city;
        private String isp;

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder requestCount(long requestCount) {
            this.requestCount = requestCount;
            return this;
        }

        public Builder errorCount(long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public Builder errorRate(double errorRate) {
            this.errorRate = errorRate;
            return this;
        }

        public Builder avgResponseTime(double avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
            return this;
        }

        public Builder firstRequestTime(long firstRequestTime) {
            this.firstRequestTime = firstRequestTime;
            return this;
        }

        public Builder lastRequestTime(long lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
            return this;
        }

        public Builder accessedEndpoints(List<String> accessedEndpoints) {
            this.accessedEndpoints = accessedEndpoints;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder anomalyLevel(AnomalyLevel anomalyLevel) {
            this.anomalyLevel = anomalyLevel;
            return this;
        }

        public Builder anomalyReason(String anomalyReason) {
            this.anomalyReason = anomalyReason;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder province(String province) {
            this.province = province;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder isp(String isp) {
            this.isp = isp;
            return this;
        }

        public IpStatistics build() {
            return new IpStatistics(this);
        }
    }
}
