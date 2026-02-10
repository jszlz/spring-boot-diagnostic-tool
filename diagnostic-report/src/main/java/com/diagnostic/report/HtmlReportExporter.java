package com.diagnostic.report;

import com.diagnostic.core.model.HealthReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exporter for health reports in HTML format.
 * Uses Thymeleaf to render HTML reports from templates.
 */
@Component
public class HtmlReportExporter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlReportExporter.class);
    private static final String TEMPLATE_NAME = "health-report";

    private final TemplateEngine templateEngine;

    public HtmlReportExporter() {
        this.templateEngine = createTemplateEngine();
    }

    /**
     * Create and configure Thymeleaf template engine.
     *
     * @return configured template engine
     */
    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }

    /**
     * Export health report to HTML string.
     *
     * @param report the health report
     * @return HTML string
     */
    public String exportToHtml(HealthReport report) {
        try {
            Context context = new Context();
            context.setVariable("report", report);
            
            return templateEngine.process(TEMPLATE_NAME, context);
        } catch (Exception e) {
            logger.error("Error exporting report to HTML: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export report to HTML", e);
        }
    }

    /**
     * Export health report to HTML file.
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

            // Generate HTML
            String html = exportToHtml(report);

            // Write to file
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(html);
            }

            logger.info("Health report exported to HTML file: {}", outputPath);
        } catch (IOException e) {
            logger.error("Error exporting report to file {}: {}", outputPath, e.getMessage());
            throw new RuntimeException("Failed to export report to file", e);
        }
    }

    /**
     * Export health report to both JSON and HTML files.
     *
     * @param report the health report
     * @param baseOutputPath the base output path (without extension)
     * @param jsonExporter the JSON exporter
     */
    public void exportToFiles(HealthReport report, String baseOutputPath, JsonReportExporter jsonExporter) {
        String htmlPath = baseOutputPath + ".html";
        String jsonPath = baseOutputPath + ".json";

        exportToFile(report, htmlPath);
        jsonExporter.exportToFile(report, jsonPath);

        logger.info("Health report exported to: {} and {}", htmlPath, jsonPath);
    }
}
