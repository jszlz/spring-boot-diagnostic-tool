package com.diagnostic.autoconfigure;

import com.diagnostic.api.DiagnosticApiController;
import com.diagnostic.core.analyzer.HealthAnalyzer;
import com.diagnostic.core.analyzer.TrendAnalyzer;
import com.diagnostic.core.collector.PerformanceCollector;
import com.diagnostic.core.discovery.DependencyDiscoveryPostProcessor;
import com.diagnostic.core.executor.DiagnosticExecutorService;
import com.diagnostic.core.guard.ResourceGuard;
import com.diagnostic.core.interceptor.PerformanceInterceptor;
import com.diagnostic.core.registry.DependencyRegistry;
import com.diagnostic.core.storage.MetricsStorage;
import com.diagnostic.core.topology.TopologyBuilder;
import com.diagnostic.report.HtmlReportExporter;
import com.diagnostic.report.JsonReportExporter;
import com.diagnostic.report.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for the diagnostic tool.
 * This configuration is automatically loaded when the diagnostic tool is on the classpath.
 * It can be disabled by setting diagnostic.enabled=false in application properties.
 */
@Configuration
@ConditionalOnProperty(name = "diagnostic.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DiagnosticProperties.class)
public class DiagnosticAutoConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticAutoConfiguration.class);

    private final DiagnosticProperties properties;

    public DiagnosticAutoConfiguration(DiagnosticProperties properties) {
        this.properties = properties;
        logger.info("Diagnostic tool auto-configuration initialized");
    }

    // Core components

    @Bean
    @ConditionalOnMissingBean
    public DependencyRegistry dependencyRegistry() {
        return new DependencyRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public DiagnosticExecutorService diagnosticExecutorService() {
        return new DiagnosticExecutorService(
            properties.getAsyncThreadPoolSize(),
            properties.getAsyncThreadPoolSize() * 2
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsStorage metricsStorage() {
        return new MetricsStorage(properties.getDataRetentionDays());
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceGuard resourceGuard() {
        return new ResourceGuard(
            Runtime.getRuntime().maxMemory(),
            properties.getMaxMemoryUsageThreshold(),
            properties.getMaxCachedMetricsPerEndpoint()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public PerformanceCollector performanceCollector(MetricsStorage metricsStorage) {
        return new PerformanceCollector(metricsStorage, properties.getSlowEndpointThresholdMs());
    }

    @Bean
    @ConditionalOnMissingBean
    public DependencyDiscoveryPostProcessor dependencyDiscoveryPostProcessor(DependencyRegistry dependencyRegistry) {
        return new DependencyDiscoveryPostProcessor(dependencyRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public PerformanceInterceptor performanceInterceptor(PerformanceCollector performanceCollector) {
        return new PerformanceInterceptor(
            performanceCollector,
            properties.isMonitorAllEndpoints(),
            properties.getSamplingRate()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TopologyBuilder topologyBuilder(DependencyRegistry dependencyRegistry) {
        return new TopologyBuilder(dependencyRegistry);
    }

    // Analyzers

    @Bean
    @ConditionalOnMissingBean
    public HealthAnalyzer healthAnalyzer(DependencyRegistry dependencyRegistry,
                                        PerformanceCollector performanceCollector,
                                        TopologyBuilder topologyBuilder) {
        return new HealthAnalyzer(
            dependencyRegistry,
            performanceCollector,
            topologyBuilder,
            properties.getSlowEndpointThresholdMs(),
            properties.getHighErrorRateThreshold()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TrendAnalyzer trendAnalyzer(MetricsStorage metricsStorage) {
        return new TrendAnalyzer(metricsStorage);
    }

    // Report generators

    @Bean
    @ConditionalOnMissingBean
    public ReportGenerator reportGenerator(TopologyBuilder topologyBuilder,
                                          HealthAnalyzer healthAnalyzer,
                                          PerformanceCollector performanceCollector,
                                          TrendAnalyzer trendAnalyzer) {
        return new ReportGenerator(topologyBuilder, healthAnalyzer, performanceCollector, trendAnalyzer);
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonReportExporter jsonReportExporter() {
        return new JsonReportExporter();
    }

    @Bean
    @ConditionalOnMissingBean
    public HtmlReportExporter htmlReportExporter() {
        return new HtmlReportExporter();
    }

    // REST API

    @Bean
    @ConditionalOnProperty(name = "diagnostic.api.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public DiagnosticApiController diagnosticApiController(ReportGenerator reportGenerator,
                                                          TopologyBuilder topologyBuilder,
                                                          PerformanceCollector performanceCollector,
                                                          HealthAnalyzer healthAnalyzer) {
        logger.info("Diagnostic REST API enabled");
        return new DiagnosticApiController(reportGenerator, topologyBuilder, performanceCollector, healthAnalyzer);
    }

    @Bean
    @ConditionalOnProperty(name = "diagnostic.api.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public com.diagnostic.api.DiagnosticViewController diagnosticViewController(ReportGenerator reportGenerator,
                                                                                HtmlReportExporter htmlExporter) {
        logger.info("Diagnostic Web UI enabled");
        return new com.diagnostic.api.DiagnosticViewController(reportGenerator, htmlExporter);
    }

    // Register interceptor

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        PerformanceInterceptor interceptor = performanceInterceptor(performanceCollector(metricsStorage()));
        registry.addInterceptor(interceptor);
        logger.info("Performance interceptor registered");
    }
}
