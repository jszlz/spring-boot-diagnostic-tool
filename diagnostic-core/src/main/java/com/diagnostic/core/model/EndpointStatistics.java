package com.diagnostic.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointStatistics {
    private String endpoint;
    private long totalRequests;
    private long errorCount;  // 错误请求数
    private double qps;
    private double averageResponseTime;
    private double p95ResponseTime;
    private double p99ResponseTime;
    private double errorRate;
    private String codeLocation;
    private List<Long> recentResponseTimes;
    private EndpointType endpointType;
    private List<ErrorDetail> recentErrors;  // 最近的错误详情（最多保留100条）
    private Map<Integer, Long> errorStatusCodeDistribution;  // 错误状态码分布

    private static final int MAX_RECENT_ERRORS = 100;

    public EndpointStatistics() {
        this.recentResponseTimes = new ArrayList<>();
        this.recentErrors = new ArrayList<>();
        this.errorStatusCodeDistribution = new ConcurrentHashMap<>();
    }

    public EndpointStatistics(String endpoint) {
        this.endpoint = endpoint;
        this.recentResponseTimes = new ArrayList<>();
        this.recentErrors = new ArrayList<>();
        this.errorStatusCodeDistribution = new ConcurrentHashMap<>();
    }

    public void addMetric(EndpointMetrics metrics) {
        totalRequests++;
        recentResponseTimes.add(metrics.getDuration());
        
        // 如果是错误请求，记录错误详情
        if (metrics.getStatusCode() >= 400) {
            ErrorDetail error = new ErrorDetail(
                metrics.getTimestamp(),
                metrics.getStatusCode(),
                metrics.getMethod(),
                metrics.getEndpoint(),
                metrics.getDuration() / 1_000_000  // 转换为毫秒
            );
            
            // 保持最近的错误记录
            if (recentErrors.size() >= MAX_RECENT_ERRORS) {
                recentErrors.remove(0);  // 移除最旧的
            }
            recentErrors.add(error);
            
            // 更新状态码分布
            errorStatusCodeDistribution.merge(metrics.getStatusCode(), 1L, Long::sum);
        }
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

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
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

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    public List<ErrorDetail> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<ErrorDetail> recentErrors) {
        this.recentErrors = recentErrors;
    }

    public Map<Integer, Long> getErrorStatusCodeDistribution() {
        return errorStatusCodeDistribution;
    }

    public void setErrorStatusCodeDistribution(Map<Integer, Long> errorStatusCodeDistribution) {
        this.errorStatusCodeDistribution = errorStatusCodeDistribution;
    }
}
