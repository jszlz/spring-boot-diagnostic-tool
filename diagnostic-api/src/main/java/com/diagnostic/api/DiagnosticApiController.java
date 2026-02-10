package com.diagnostic.api;

import com.diagnostic.core.analyzer.HealthAnalyzer;
import com.diagnostic.core.collector.PerformanceCollector;
import com.diagnostic.core.model.ArchitectureRisk;
import com.diagnostic.core.model.DependencyTopology;
import com.diagnostic.core.model.EndpointStatistics;
import com.diagnostic.core.model.HealthReport;
import com.diagnostic.core.topology.TopologyBuilder;
import com.diagnostic.report.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for diagnostic tool.
 * Provides endpoints to access health reports, topology, endpoint statistics, and risks.
 */
@RestController
@RequestMapping("/diagnostic")
public class DiagnosticApiController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticApiController.class);

    private final ReportGenerator reportGenerator;
    private final TopologyBuilder topologyBuilder;
    private final PerformanceCollector performanceCollector;
    private final HealthAnalyzer healthAnalyzer;

    public DiagnosticApiController(ReportGenerator reportGenerator,
                                  TopologyBuilder topologyBuilder,
                                  PerformanceCollector performanceCollector,
                                  HealthAnalyzer healthAnalyzer) {
        this.reportGenerator = reportGenerator;
        this.topologyBuilder = topologyBuilder;
        this.performanceCollector = performanceCollector;
        this.healthAnalyzer = healthAnalyzer;
    }

    /**
     * Get the current health report.
     *
     * @return health report
     */
    @GetMapping("/health")
    public ResponseEntity<HealthReport> getHealthReport() {
        try {
            logger.info("Generating health report via API");
            HealthReport report = reportGenerator.generateReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error generating health report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the health report summary.
     *
     * @return summary map
     */
    @GetMapping("/health/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        try {
            HealthReport report = reportGenerator.generateReport();
            Map<String, Object> summary = reportGenerator.generateSummary(report);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error generating health summary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the dependency topology.
     *
     * @return dependency topology
     */
    @GetMapping("/topology")
    public ResponseEntity<DependencyTopology> getTopology() {
        try {
            logger.info("Building topology via API");
            DependencyTopology topology = topologyBuilder.buildTopology();
            return ResponseEntity.ok(topology);
        } catch (Exception e) {
            logger.error("Error building topology: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the topology in JSON format (graph data only).
     *
     * @return topology graph data
     */
    @GetMapping("/topology/json")
    public ResponseEntity<Map<String, Object>> getTopologyJson() {
        try {
            DependencyTopology topology = topologyBuilder.buildTopology();
            return ResponseEntity.ok(topology.getGraphData());
        } catch (Exception e) {
            logger.error("Error getting topology JSON: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the topology in DOT format.
     *
     * @return DOT format string
     */
    @GetMapping(value = "/topology/dot", produces = "text/plain")
    public ResponseEntity<String> getTopologyDot() {
        try {
            DependencyTopology topology = topologyBuilder.buildTopology();
            String dot = topologyBuilder.exportToDot(topology);
            return ResponseEntity.ok(dot);
        } catch (Exception e) {
            logger.error("Error getting topology DOT: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all monitored endpoints and their statistics.
     *
     * @return map of endpoint to statistics
     */
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, EndpointStatistics>> getAllEndpoints() {
        try {
            logger.info("Getting all endpoint statistics via API");
            Map<String, EndpointStatistics> stats = performanceCollector.getAllStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting endpoint statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get statistics for a specific endpoint.
     *
     * @param name the endpoint name
     * @return endpoint statistics
     */
    @GetMapping("/endpoints/{name}")
    public ResponseEntity<EndpointStatistics> getEndpoint(@PathVariable String name) {
        try {
            logger.info("Getting statistics for endpoint: {}", name);
            EndpointStatistics stats = performanceCollector.getStatistics(name);
            
            if (stats == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting endpoint statistics for {}: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all identified architecture risks.
     *
     * @return list of risks
     */
    @GetMapping("/risks")
    public ResponseEntity<List<ArchitectureRisk>> getRisks() {
        try {
            logger.info("Analyzing risks via API");
            List<ArchitectureRisk> risks = healthAnalyzer.analyzeRisks();
            return ResponseEntity.ok(risks);
        } catch (Exception e) {
            logger.error("Error analyzing risks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get high severity risks only.
     *
     * @return list of high severity risks
     */
    @GetMapping("/risks/high")
    public ResponseEntity<List<ArchitectureRisk>> getHighSeverityRisks() {
        try {
            HealthReport report = reportGenerator.generateReport();
            List<ArchitectureRisk> highRisks = reportGenerator.getHighSeverityRisks(report);
            return ResponseEntity.ok(highRisks);
        } catch (Exception e) {
            logger.error("Error getting high severity risks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Refresh statistics for a specific endpoint.
     *
     * @param name the endpoint name
     * @return success message
     */
    @PostMapping("/endpoints/{name}/refresh")
    public ResponseEntity<Map<String, String>> refreshEndpoint(@PathVariable String name) {
        try {
            logger.info("Refreshing statistics for endpoint: {}", name);
            performanceCollector.refreshStatistics(name);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Statistics refreshed for endpoint: " + name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error refreshing endpoint statistics: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Refresh statistics for all endpoints.
     *
     * @return success message
     */
    @PostMapping("/endpoints/refresh")
    public ResponseEntity<Map<String, String>> refreshAllEndpoints() {
        try {
            logger.info("Refreshing statistics for all endpoints");
            performanceCollector.refreshAllStatistics();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Statistics refreshed for all endpoints");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error refreshing all endpoint statistics: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint.
     *
     * @return status message
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Diagnostic Tool API");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(status);
    }
}
