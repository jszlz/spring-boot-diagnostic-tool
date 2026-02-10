package com.diagnostic.core.analyzer;

import com.diagnostic.core.collector.PerformanceCollector;
import com.diagnostic.core.model.*;
import com.diagnostic.core.registry.DependencyRegistry;
import com.diagnostic.core.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for application health and architecture risks.
 * Identifies various types of risks including single points of failure,
 * circular dependencies, slow endpoints, high error rates, and excessive dependencies.
 */
public class HealthAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(HealthAnalyzer.class);
    private static final double EXCESSIVE_DEPENDENCY_THRESHOLD = 3.0; // 3 standard deviations

    private final DependencyRegistry dependencyRegistry;
    private final PerformanceCollector performanceCollector;
    private final TopologyBuilder topologyBuilder;
    private final int slowEndpointThresholdMs;
    private final double highErrorRateThreshold;

    public HealthAnalyzer(DependencyRegistry dependencyRegistry,
                         PerformanceCollector performanceCollector,
                         TopologyBuilder topologyBuilder) {
        this(dependencyRegistry, performanceCollector, topologyBuilder, 1000, 0.05);
    }

    public HealthAnalyzer(DependencyRegistry dependencyRegistry,
                         PerformanceCollector performanceCollector,
                         TopologyBuilder topologyBuilder,
                         int slowEndpointThresholdMs,
                         double highErrorRateThreshold) {
        this.dependencyRegistry = dependencyRegistry;
        this.performanceCollector = performanceCollector;
        this.topologyBuilder = topologyBuilder;
        this.slowEndpointThresholdMs = slowEndpointThresholdMs;
        this.highErrorRateThreshold = highErrorRateThreshold;
    }

    /**
     * Analyze all risks in the application.
     *
     * @return list of identified risks
     */
    public List<ArchitectureRisk> analyzeRisks() {
        List<ArchitectureRisk> risks = new ArrayList<>();

        try {
            risks.addAll(detectSinglePointsOfFailure());
            risks.addAll(detectCircularDependencies());
            risks.addAll(detectSlowEndpoints());
            risks.addAll(detectHighErrorRates());
            risks.addAll(detectExcessiveDependencies());

            logger.info("Health analysis complete: {} risks identified", risks.size());
        } catch (Exception e) {
            logger.error("Error during health analysis: {}", e.getMessage());
        }

        return risks;
    }

    /**
     * Detect single points of failure in dependencies.
     *
     * @return list of single point of failure risks
     */
    private List<ArchitectureRisk> detectSinglePointsOfFailure() {
        List<ArchitectureRisk> risks = new ArrayList<>();

        for (ExternalDependency dep : dependencyRegistry.getAllDependencies()) {
            if (dep.isCritical() && !dep.hasRedundancy()) {
                ArchitectureRisk risk = ArchitectureRisk.builder()
                        .type(RiskType.SINGLE_POINT_OF_FAILURE)
                        .severity(Severity.HIGH)
                        .component(dep.getId())
                        .description(String.format("关键依赖 '%s' 没有冗余备份，存在单点故障风险", dep.getId()))
                        .addRecommendation("配置主从复制或集群模式")
                        .addRecommendation("实现故障转移机制")
                        .addRecommendation("添加熔断器保护")
                        .build();
                risks.add(risk);
            }
        }

        return risks;
    }

    /**
     * Detect circular dependencies in the topology.
     *
     * @return list of circular dependency risks
     */
    private List<ArchitectureRisk> detectCircularDependencies() {
        List<ArchitectureRisk> risks = new ArrayList<>();

        try {
            DependencyTopology topology = topologyBuilder.buildTopology();
            Set<TopologyNode> cyclicNodes = topology.getCyclicNodes();

            if (!cyclicNodes.isEmpty()) {
                String components = cyclicNodes.stream()
                        .map(TopologyNode::getId)
                        .collect(Collectors.joining(", "));

                ArchitectureRisk risk = ArchitectureRisk.builder()
                        .type(RiskType.CIRCULAR_DEPENDENCY)
                        .severity(Severity.HIGH)
                        .component(components)
                        .description(String.format("检测到 %d 个组件存在循环依赖: %s", 
                                   cyclicNodes.size(), components))
                        .addRecommendation("重构代码以消除循环依赖")
                        .addRecommendation("使用依赖注入和接口解耦")
                        .addRecommendation("考虑引入事件驱动架构")
                        .build();
                risks.add(risk);
            }
        } catch (Exception e) {
            logger.error("Error detecting circular dependencies: {}", e.getMessage());
        }

        return risks;
    }

    /**
     * Detect slow endpoints based on threshold.
     *
     * @return list of performance bottleneck risks
     */
    private List<ArchitectureRisk> detectSlowEndpoints() {
        List<ArchitectureRisk> risks = new ArrayList<>();

        Map<String, EndpointStatistics> allStats = performanceCollector.getAllStatistics();
        for (Map.Entry<String, EndpointStatistics> entry : allStats.entrySet()) {
            EndpointStatistics stats = entry.getValue();
            
            if (stats.getAverageResponseTime() > slowEndpointThresholdMs) {
                Severity severity = determineSeverityByResponseTime(stats.getAverageResponseTime());
                
                ArchitectureRisk risk = ArchitectureRisk.builder()
                        .type(RiskType.PERFORMANCE_BOTTLENECK)
                        .severity(severity)
                        .component(stats.getEndpoint())
                        .description(String.format("端点 '%s' 平均响应时间 %.2f ms 超过阈值 %d ms",
                                   stats.getEndpoint(),
                                   stats.getAverageResponseTime(),
                                   slowEndpointThresholdMs))
                        .addRecommendation("优化数据库查询")
                        .addRecommendation("添加缓存层")
                        .addRecommendation("考虑异步处理")
                        .addRecommendation("检查N+1查询问题")
                        .build();
                risks.add(risk);
            }
        }

        return risks;
    }

    /**
     * Detect endpoints with high error rates.
     *
     * @return list of high error rate risks
     */
    private List<ArchitectureRisk> detectHighErrorRates() {
        List<ArchitectureRisk> risks = new ArrayList<>();

        Map<String, EndpointStatistics> allStats = performanceCollector.getAllStatistics();
        for (Map.Entry<String, EndpointStatistics> entry : allStats.entrySet()) {
            EndpointStatistics stats = entry.getValue();
            
            if (stats.getErrorRate() > highErrorRateThreshold) {
                Severity severity = determineSeverityByErrorRate(stats.getErrorRate());
                
                ArchitectureRisk risk = ArchitectureRisk.builder()
                        .type(RiskType.HIGH_ERROR_RATE)
                        .severity(severity)
                        .component(stats.getEndpoint())
                        .description(String.format("端点 '%s' 错误率 %.2f%% 超过阈值 %.2f%%",
                                   stats.getEndpoint(),
                                   stats.getErrorRate() * 100,
                                   highErrorRateThreshold * 100))
                        .addRecommendation("检查错误日志定位根本原因")
                        .addRecommendation("添加输入验证")
                        .addRecommendation("改进错误处理逻辑")
                        .addRecommendation("考虑添加重试机制")
                        .build();
                risks.add(risk);
            }
        }

        return risks;
    }

    /**
     * Detect excessive dependencies (dependencies with abnormally high call counts).
     *
     * @return list of excessive dependency risks
     */
    private List<ArchitectureRisk> detectExcessiveDependencies() {
        List<ArchitectureRisk> risks = new ArrayList<>();

        Collection<ExternalDependency> dependencies = dependencyRegistry.getAllDependencies();
        if (dependencies.size() < 2) {
            return risks; // Need at least 2 dependencies for statistical analysis
        }

        // Calculate mean and standard deviation of call counts
        List<Long> callCounts = dependencies.stream()
                .map(ExternalDependency::getCallCount)
                .collect(Collectors.toList());

        double mean = callCounts.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        double variance = callCounts.stream()
                .mapToDouble(count -> Math.pow(count - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // Identify dependencies with call counts > mean + 3*stdDev
        double threshold = mean + (EXCESSIVE_DEPENDENCY_THRESHOLD * stdDev);

        for (ExternalDependency dep : dependencies) {
            if (dep.getCallCount() > threshold && stdDev > 0) {
                ArchitectureRisk risk = ArchitectureRisk.builder()
                        .type(RiskType.EXCESSIVE_DEPENDENCY)
                        .severity(Severity.MEDIUM)
                        .component(dep.getId())
                        .description(String.format("依赖 '%s' 的调用频率 (%d) 异常高，可能存在过度依赖",
                                   dep.getId(), dep.getCallCount()))
                        .addRecommendation("考虑添加缓存减少调用次数")
                        .addRecommendation("批量处理请求")
                        .addRecommendation("检查是否存在不必要的重复调用")
                        .build();
                risks.add(risk);
            }
        }

        return risks;
    }

    /**
     * Determine severity based on response time.
     *
     * @param responseTime response time in milliseconds
     * @return severity level
     */
    private Severity determineSeverityByResponseTime(double responseTime) {
        if (responseTime > slowEndpointThresholdMs * 3) {
            return Severity.HIGH;
        } else if (responseTime > slowEndpointThresholdMs * 2) {
            return Severity.MEDIUM;
        } else {
            return Severity.LOW;
        }
    }

    /**
     * Determine severity based on error rate.
     *
     * @param errorRate error rate (0.0 to 1.0)
     * @return severity level
     */
    private Severity determineSeverityByErrorRate(double errorRate) {
        if (errorRate > 0.2) { // > 20%
            return Severity.HIGH;
        } else if (errorRate > 0.1) { // > 10%
            return Severity.MEDIUM;
        } else {
            return Severity.LOW;
        }
    }
}
