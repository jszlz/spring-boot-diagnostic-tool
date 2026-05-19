package com.diagnostic.core.analyzer;

import com.diagnostic.core.model.EndpointIpDistribution;
import com.diagnostic.core.model.EndpointMetrics;
import com.diagnostic.core.model.IpStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IP访问分析器
 * 分析端点的IP访问分布，识别异常访问模式
 */
public class IpAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(IpAnalyzer.class);
    
    // 异常检测阈值
    private static final double HIGH_CONCENTRATION_THRESHOLD = 0.7;  // 单IP占比>70%为异常
    private static final int HIGH_FREQUENCY_THRESHOLD = 100;         // 每分钟>100次为高频
    private static final double HIGH_ERROR_RATE_THRESHOLD = 0.5;     // 错误率>50%为异常
    
    public IpAnalyzer() {
    }
    
    /**
     * 分析端点的IP分布
     */
    public EndpointIpDistribution analyzeEndpointIpDistribution(
            String endpoint, 
            List<EndpointMetrics> metrics) {
        
        if (metrics == null || metrics.isEmpty()) {
            logger.warn("No metrics available for endpoint: {}", endpoint);
            return EndpointIpDistribution.builder()
                    .endpoint(endpoint)
                    .totalRequests(0)
                    .uniqueIpCount(0)
                    .topIps(new ArrayList<>())
                    .concentrationRate(0)
                    .isAnomalous(false)
                    .build();
        }
        
        // 按IP分组统计
        Map<String, List<EndpointMetrics>> ipGroups = metrics.stream()
                .filter(m -> m.getClientIp() != null && !m.getClientIp().isEmpty())
                .collect(Collectors.groupingBy(EndpointMetrics::getClientIp));
        
        logger.info("Endpoint: {}, Total metrics: {}, Metrics with IP: {}", 
                   endpoint, metrics.size(), ipGroups.values().stream().mapToInt(List::size).sum());
        
        if (ipGroups.isEmpty()) {
            logger.warn("No IP information available for endpoint: {} (total metrics: {})", endpoint, metrics.size());
            // 检查第一条记录
            if (!metrics.isEmpty()) {
                EndpointMetrics sample = metrics.get(0);
                logger.warn("Sample metric - IP: {}, UserAgent: {}, Endpoint: {}", 
                           sample.getClientIp(), sample.getUserAgent(), sample.getEndpoint());
            }
            return EndpointIpDistribution.builder()
                    .endpoint(endpoint)
                    .totalRequests(metrics.size())
                    .uniqueIpCount(0)
                    .topIps(new ArrayList<>())
                    .concentrationRate(0)
                    .isAnomalous(false)
                    .anomalyDescription("无IP信息")
                    .build();
        }
        
        // 计算每个IP的统计信息
        List<IpStatistics> ipStatsList = ipGroups.entrySet().stream()
                .map(entry -> calculateIpStatistics(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(IpStatistics::getRequestCount).reversed())
                .collect(Collectors.toList());
        
        // 计算集中度（Top1 IP占比）
        long totalRequests = metrics.stream()
                .filter(m -> m.getClientIp() != null && !m.getClientIp().isEmpty())
                .count();
        
        double concentrationRate = ipStatsList.isEmpty() ? 0 : 
                (double) ipStatsList.get(0).getRequestCount() / totalRequests;
        
        // 检测异常
        boolean isAnomalous = false;
        String anomalyDescription = null;
        
        if (concentrationRate > HIGH_CONCENTRATION_THRESHOLD) {
            isAnomalous = true;
            anomalyDescription = String.format(
                "单个IP(%s)占比%.1f%%，疑似爬虫或攻击", 
                ipStatsList.get(0).getIp(),
                concentrationRate * 100
            );
        }
        
        // 检查是否有危险级别的IP
        long dangerousIpCount = ipStatsList.stream()
                .filter(ip -> ip.getAnomalyLevel() == IpStatistics.AnomalyLevel.DANGEROUS)
                .count();
        
        if (dangerousIpCount > 0 && !isAnomalous) {
            isAnomalous = true;
            anomalyDescription = String.format("检测到%d个危险IP", dangerousIpCount);
        }
        
        return EndpointIpDistribution.builder()
                .endpoint(endpoint)
                .totalRequests(totalRequests)
                .uniqueIpCount(ipGroups.size())
                .topIps(ipStatsList.subList(0, Math.min(20, ipStatsList.size())))
                .concentrationRate(concentrationRate)
                .isAnomalous(isAnomalous)
                .anomalyDescription(anomalyDescription)
                .build();
    }
    
    /**
     * 计算单个IP的统计信息
     */
    private IpStatistics calculateIpStatistics(String ip, List<EndpointMetrics> metrics) {
        long requestCount = metrics.size();
        long errorCount = metrics.stream()
                .filter(m -> m.getStatusCode() >= 400)
                .count();
        
        double errorRate = (double) errorCount / requestCount;
        
        double avgResponseTime = metrics.stream()
                .mapToLong(EndpointMetrics::getDuration)
                .average()
                .orElse(0) / 1_000_000.0; // 转换为毫秒
        
        long firstRequestTime = metrics.stream()
                .mapToLong(EndpointMetrics::getTimestamp)
                .min()
                .orElse(0);
        
        long lastRequestTime = metrics.stream()
                .mapToLong(EndpointMetrics::getTimestamp)
                .max()
                .orElse(0);
        
        Set<String> accessedEndpoints = metrics.stream()
                .map(EndpointMetrics::getEndpoint)
                .collect(Collectors.toSet());
        
        String userAgent = metrics.stream()
                .map(EndpointMetrics::getUserAgent)
                .filter(ua -> ua != null && !ua.isEmpty())
                .findFirst()
                .orElse("Unknown");
        
        // 异常检测
        IpStatistics.AnomalyLevel anomalyLevel = detectAnomalyLevel(
                requestCount, errorRate, firstRequestTime, lastRequestTime, userAgent
        );
        
        String anomalyReason = buildAnomalyReason(
                requestCount, errorRate, firstRequestTime, lastRequestTime, userAgent
        );
        
        IpStatistics.Builder builder = IpStatistics.builder()
                .ip(ip)
                .requestCount(requestCount)
                .errorCount(errorCount)
                .errorRate(errorRate)
                .avgResponseTime(avgResponseTime)
                .firstRequestTime(firstRequestTime)
                .lastRequestTime(lastRequestTime)
                .accessedEndpoints(new ArrayList<>(accessedEndpoints))
                .userAgent(userAgent)
                .anomalyLevel(anomalyLevel)
                .anomalyReason(anomalyReason);
        
        return builder.build();
    }
    
    /**
     * 检测异常等级
     */
    private IpStatistics.AnomalyLevel detectAnomalyLevel(
            long requestCount, 
            double errorRate,
            long firstRequestTime, 
            long lastRequestTime,
            String userAgent) {
        
        // 计算请求频率（每分钟请求数）
        long timeSpanMs = lastRequestTime - firstRequestTime;
        if (timeSpanMs == 0) {
            timeSpanMs = 1000; // 避免除零，至少1秒
        }
        double requestsPerMinute = (double) requestCount / (timeSpanMs / 60000.0);
        
        // 危险级别判断
        if (requestsPerMinute > HIGH_FREQUENCY_THRESHOLD * 2 || errorRate > 0.8) {
            return IpStatistics.AnomalyLevel.DANGEROUS;
        }
        
        // 可疑级别判断
        if (requestsPerMinute > HIGH_FREQUENCY_THRESHOLD || 
            errorRate > HIGH_ERROR_RATE_THRESHOLD ||
            isSuspiciousUserAgent(userAgent)) {
            return IpStatistics.AnomalyLevel.SUSPICIOUS;
        }
        
        return IpStatistics.AnomalyLevel.NORMAL;
    }
    
    /**
     * 构建异常原因描述
     */
    private String buildAnomalyReason(
            long requestCount,
            double errorRate,
            long firstRequestTime,
            long lastRequestTime,
            String userAgent) {
        
        List<String> reasons = new ArrayList<>();
        
        // 计算请求频率
        long timeSpanMs = lastRequestTime - firstRequestTime;
        if (timeSpanMs == 0) {
            timeSpanMs = 1000;
        }
        double requestsPerMinute = (double) requestCount / (timeSpanMs / 60000.0);
        
        if (requestsPerMinute > HIGH_FREQUENCY_THRESHOLD) {
            reasons.add(String.format("高频请求(%.0f次/分钟)", requestsPerMinute));
        }
        
        if (errorRate > HIGH_ERROR_RATE_THRESHOLD) {
            reasons.add(String.format("高错误率(%.1f%%)", errorRate * 100));
        }
        
        if (isSuspiciousUserAgent(userAgent)) {
            reasons.add("可疑User-Agent");
        }
        
        return reasons.isEmpty() ? null : String.join(", ", reasons);
    }
    
    /**
     * 判断是否是可疑的User-Agent
     */
    private boolean isSuspiciousUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty() || "Unknown".equals(userAgent)) {
            return true; // 没有User-Agent是可疑的
        }
        
        String lower = userAgent.toLowerCase();
        
        // 常见爬虫特征
        String[] crawlerKeywords = {
            "bot", "spider", "crawler", "scraper", "curl", "wget", 
            "python", "java", "go-http", "okhttp", "httpclient"
        };
        
        for (String keyword : crawlerKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
}
