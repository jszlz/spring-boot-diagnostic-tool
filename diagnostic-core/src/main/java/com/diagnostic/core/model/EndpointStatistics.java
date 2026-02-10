package com.diagnostic.core.model;

import java.util.ArrayList;
import java.util.List;

public class EndpointStatistics {
    private String endpoint;
    private long totalRequests;
    private double qps;
    private double averageResponseTime;
    private double p95ResponseTime;
    private double p99ResponseTime;
    private double errorRate;
    private String codeLocation;
    private List<Long> recentResponseTimes;

    public EndpointStatistics() {
        this.recentResponseTimes = new ArrayList<>();
    }

    public EndpointStatistics(String endpoint) {
        this.endpoint = endpoint;
        this.recentResponseTimes = new ArrayList<>();
    }

    public void addMetric(EndpointMetrics metrics) {
        totalRequests++;
        recentResponseTimes.add(metrics.getDuration());
    }

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

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public double getP95ResponseTime() {
        return p95ResponseTime;
    }

    public void setP95ResponseTime(double p95ResponseTime) {
        this.p95ResponseTime = p95ResponseTime;
    }

    public double getP99ResponseTime() {
        return p99ResponseTime;
    }

    public void setP99ResponseTime(double p99ResponseTime) {
        this.p99ResponseTime = p99ResponseTime;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public String getCodeLocation() {
        return codeLocation;
    }

    public void setCodeLocation(String codeLocation) {
        this.codeLocation = codeLocation;
    }

    public List<Long> getRecentResponseTimes() {
        return recentResponseTimes;
    }

    public void setRecentResponseTimes(List<Long> recentResponseTimes) {
        this.recentResponseTimes = recentResponseTimes;
    }
}
