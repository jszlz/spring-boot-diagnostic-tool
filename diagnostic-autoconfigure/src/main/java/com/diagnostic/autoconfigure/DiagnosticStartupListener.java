package com.diagnostic.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener for application startup events.
 * Outputs diagnostic tool initialization information when the application is ready.
 */
@Component
public class DiagnosticStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticStartupListener.class);

    private final DiagnosticProperties properties;

    public DiagnosticStartupListener(DiagnosticProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!properties.isEnabled()) {
            logger.info("Diagnostic tool is disabled");
            return;
        }

        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║   Spring Boot Diagnostic Tool - Initialized Successfully     ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");
        logger.info("");
        logger.info("Configuration:");
        logger.info("  - Enabled: {}", properties.isEnabled());
        logger.info("  - Monitor All Endpoints: {}", properties.isMonitorAllEndpoints());
        logger.info("  - Slow Endpoint Threshold: {} ms", properties.getSlowEndpointThresholdMs());
        logger.info("  - High Error Rate Threshold: {}%", properties.getHighErrorRateThreshold() * 100);
        logger.info("  - Sampling Rate: {}%", properties.getSamplingRate() * 100);
        logger.info("  - Data Retention: {} days", properties.getDataRetentionDays());
        logger.info("  - API Enabled: {}", properties.isApiEnabled());
        
        if (properties.isApiEnabled()) {
            logger.info("");
            logger.info("REST API Endpoints:");
            logger.info("  - GET  /diagnostic/health          - Get health report");
            logger.info("  - GET  /diagnostic/topology        - Get dependency topology");
            logger.info("  - GET  /diagnostic/endpoints       - Get all endpoint statistics");
            logger.info("  - GET  /diagnostic/risks           - Get architecture risks");
            logger.info("  - GET  /diagnostic/status          - Health check");
        }
        
        logger.info("");
        logger.info("Report Output Path: {}", properties.getReportOutputPath());
        
        if (properties.isAutoGenerateReportOnStartup()) {
            logger.info("Auto-generate report on startup: ENABLED");
        }
        
        logger.info("");
        logger.info("Diagnostic tool is ready to monitor your application!");
        logger.info("═══════════════════════════════════════════════════════════════");
    }
}
