package com.diagnostic.report;

import com.diagnostic.core.analyzer.HealthAnalyzer;
import com.diagnostic.core.analyzer.TrendAnalyzer;
import com.diagnostic.core.collector.PerformanceCollector;
import com.diagnostic.core.model.*;
import com.diagnostic.core.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for health reports.
 * Integrates topology, risks, performance statistics, and trends into a comprehensive report.
 */
@Component
public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private final TopologyBuilder topologyBuilder;
    private final HealthAnalyzer healthAnalyzer;
    private final PerformanceCollector performanceCollector;
    private final TrendAnalyzer trendAnalyzer;

    public ReportGenerator(TopologyBuilder topologyBuilder,
                          HealthAnalyzer healthAnalyzer,
                          PerformanceCollector performanceCollector,
                          TrendAnalyzer trendAnalyzer) {
        this.topologyBuilder = topologyBuilder;
        this.healthAnalyzer = healthAnalyzer;
        this.performanceCollector = performanceCollector;
        this.trendAnalyzer = trendAnalyzer;
    }

    /**
     * Generate a comprehensive health report.
     *
     * @return health report
     */
    public HealthReport generateReport() {
        logger.info("Generating health report...");

        try {
            // Build topology
            DependencyTopology topology = topologyBuilder.buildTopology();

            // Analyze risks
            List<ArchitectureRisk> risks = healthAnalyzer.analyzeRisks();

            // Get endpoint statistics
            Map<String, EndpointStatistics> endpointStats = performanceCollector.getAllStatistics();

            // Analyze trends
            Map<String, TrendAnalysis> trends = trendAnalyzer.analyzeAllTrends();

            // Associate code locations with endpoints
            associateCodeLocations(endpointStats);

            // Build report
            HealthReport report = HealthReport.builder()
                    .timestamp(System.currentTimeMillis())
                    .topology(topology)
                    .risks(risks)
                    .endpointStatistics(endpointStats)
                    .trends(trends)
                    .build();

            logger.info("Health report generated successfully: {} endpoints, {} risks",
                       endpointStats.size(), risks.size());

            return report;
        } catch (Exception e) {
            logger.error("Error generating health report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate health report", e);
        }
    }

    /**
     * Associate code locations with endpoint statistics.
     * This ensures each endpoint has its controller class and method information.
     *
     * @param endpointStats map of endpoint statistics
     */
    private void associateCodeLocations(Map<String, EndpointStatistics> endpointStats) {
        for (Map.Entry<String, EndpointStatistics> entry : endpointStats.entrySet()) {
            EndpointStatistics stats = entry.getValue();
            
            // Code location should already be set by PerformanceCollector
            // This is a verification step
            if (stats.getCodeLocation() == null || stats.getCodeLocation().isEmpty()) {
                logger.debug("No code location found for endpoint: {}", entry.getKey());
            }
        }
    }

    /**
     * Generate a summary of the health report.
     *
     * @param report the health report
     * @return summary map
     */
    public Map<String, Object> generateSummary(HealthReport report) {
        int totalEndpoints = report.getEndpointStatistics().size();
        int totalRisks = report.getRisks().size();
        
        long highSeverityRisks = report.getRisks().stream()
                .filter(r -> r.getSeverity() == Severity.HIGH)
                .count();
        
        long mediumSeverityRisks = report.getRisks().stream()
                .filter(r -> r.getSeverity() == Severity.MEDIUM)
                .count();
        
        long lowSeverityRisks = report.getRisks().stream()
                .filter(r -> r.getSeverity() == Severity.LOW)
                .count();

        long degradingEndpoints = report.getTrends().values().stream()
                .filter(TrendAnalysis::isDegrading)
                .count();

        int totalDependencies = report.getTopology().getGraph() != null 
                ? report.getTopology().getGraph().vertexSet().size() - 1 // Exclude application node
                : 0;

        int cyclicNodes = report.getTopology().getCyclicNodes().size();

        Map<String, Object> summary = new HashMap<>();
        summary.put("timestamp", report.getTimestamp());
        summary.put("totalEndpoints", totalEndpoints);
        summary.put("totalRisks", totalRisks);
        summary.put("highSeverityRisks", highSeverityRisks);
        summary.put("mediumSeverityRisks", mediumSeverityRisks);
        summary.put("lowSeverityRisks", lowSeverityRisks);
        summary.put("degradingEndpoints", degradingEndpoints);
        summary.put("totalDependencies", totalDependencies);
        summary.put("cyclicNodes", cyclicNodes);
        
        return summary;
    }

    /**
     * Get endpoints with performance degradation.
     *
     * @param report the health report
     * @return list of degrading endpoint names
     */
    public List<String> getDegradingEndpoints(HealthReport report) {
        return report.getTrends().entrySet().stream()
                .filter(e -> e.getValue().isDegrading())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get high severity risks.
     *
     * @param report the health report
     * @return list of high severity risks
     */
    public List<ArchitectureRisk> getHighSeverityRisks(HealthReport report) {
        return report.getRisks().stream()
                .filter(r -> r.getSeverity() == Severity.HIGH)
                .collect(java.util.stream.Collectors.toList());
    }
}
