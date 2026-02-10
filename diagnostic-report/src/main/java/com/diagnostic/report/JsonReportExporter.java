package com.diagnostic.report;

import com.diagnostic.core.model.HealthReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exporter for health reports in JSON format.
 * Uses Jackson to serialize health reports to JSON.
 */
@Component
public class JsonReportExporter {

    private static final Logger logger = LoggerFactory.getLogger(JsonReportExporter.class);

    private final ObjectMapper objectMapper;

    public JsonReportExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * Export health report to JSON string.
     *
     * @param report the health report
     * @return JSON string
     */
    public String exportToJson(HealthReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (Exception e) {
            logger.error("Error exporting report to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to export report to JSON", e);
        }
    }

    /**
     * Export health report to JSON file.
     *
     * @param report the health report
     * @param outputPath the output file path
     */
    public void exportToFile(HealthReport report, String outputPath) {
        try {
            // Ensure directory exists
            Path path = Paths.get(outputPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // Write to file
            objectMapper.writeValue(new File(outputPath), report);
            logger.info("Health report exported to JSON file: {}", outputPath);
        } catch (IOException e) {
            logger.error("Error exporting report to file {}: {}", outputPath, e.getMessage());
            throw new RuntimeException("Failed to export report to file", e);
        }
    }

    /**
     * Export health report summary to JSON string.
     *
     * @param summary the summary map
     * @return JSON string
     */
    public String exportSummaryToJson(java.util.Map<String, Object> summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            logger.error("Error exporting summary to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to export summary to JSON", e);
        }
    }
}
