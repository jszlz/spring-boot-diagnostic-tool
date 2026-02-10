package com.diagnostic.autoconfigure;

import com.diagnostic.core.executor.DiagnosticExecutorService;
import com.diagnostic.core.model.HealthReport;
import com.diagnostic.report.HtmlReportExporter;
import com.diagnostic.report.JsonReportExporter;
import com.diagnostic.report.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled task for automatic report generation.
 * Generates health reports periodically if enabled in configuration.
 */
@Component
@ConditionalOnProperty(name = "diagnostic.auto-generate-report-on-startup", havingValue = "true")
public class ScheduledReportTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledReportTask.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final DiagnosticProperties properties;
    private final ReportGenerator reportGenerator;
    private final JsonReportExporter jsonExporter;
    private final HtmlReportExporter htmlExporter;
    private final DiagnosticExecutorService executorService;

    public ScheduledReportTask(DiagnosticProperties properties,
                              ReportGenerator reportGenerator,
                              JsonReportExporter jsonExporter,
                              HtmlReportExporter htmlExporter,
                              DiagnosticExecutorService executorService) {
        this.properties = properties;
        this.reportGenerator = reportGenerator;
        this.jsonExporter = jsonExporter;
        this.htmlExporter = htmlExporter;
        this.executorService = executorService;
    }

    @PostConstruct
    public void init() {
        if (properties.isAutoGenerateReportOnStartup()) {
            logger.info("Scheduling automatic report generation on startup");
            
            // Generate initial report after 30 seconds
            executorService.schedule(this::generateReport, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * Generate and save health report.
     */
    private void generateReport() {
        try {
            logger.info("Generating scheduled health report...");
            
            HealthReport report = reportGenerator.generateReport();
            
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String baseOutputPath = properties.getReportOutputPath() + "/health-report-" + timestamp;
            
            htmlExporter.exportToFiles(report, baseOutputPath, jsonExporter);
            
            logger.info("Scheduled health report generated successfully: {}", baseOutputPath);
        } catch (Exception e) {
            logger.error("Error generating scheduled health report: {}", e.getMessage(), e);
        }
    }
}
