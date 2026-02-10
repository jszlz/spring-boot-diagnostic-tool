package com.diagnostic.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the diagnostic tool.
 * All properties can be configured via application.properties or application.yml
 * with the prefix "diagnostic".
 */
@ConfigurationProperties(prefix = "diagnostic")
public class DiagnosticProperties {

    /**
     * Whether the diagnostic tool is enabled.
     */
    private boolean enabled = true;

    /**
     * Threshold in milliseconds for marking an endpoint as slow.
     */
    private int slowEndpointThresholdMs = 1000;

    /**
     * Threshold for marking an endpoint as having high error rate (0.0 to 1.0).
     */
    private double highErrorRateThreshold = 0.05;

    /**
     * Sampling rate for performance data collection (0.0 to 1.0).
     */
    private double samplingRate = 1.0;

    /**
     * Number of days to retain performance data.
     */
    private int dataRetentionDays = 7;

    /**
     * Whether the REST API is enabled.
     */
    private boolean apiEnabled = true;

    /**
     * Output path for generated reports.
     */
    private String reportOutputPath = "./diagnostic-reports";

    /**
     * Whether to automatically generate a report on application startup.
     */
    private boolean autoGenerateReportOnStartup = false;

    /**
     * Whether to monitor all endpoints (if false, only @DiagnosticEndpoint annotated endpoints are monitored).
     */
    private boolean monitorAllEndpoints = false;

    /**
     * Maximum number of metrics to keep in memory per endpoint.
     */
    private int maxCachedMetricsPerEndpoint = 10000;

    /**
     * Maximum memory usage percentage before triggering degradation (0.0 to 1.0).
     */
    private double maxMemoryUsageThreshold = 0.9;

    /**
     * Thread pool size for async tasks.
     */
    private int asyncThreadPoolSize = 4;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSlowEndpointThresholdMs() {
        return slowEndpointThresholdMs;
    }

    public void setSlowEndpointThresholdMs(int slowEndpointThresholdMs) {
        this.slowEndpointThresholdMs = slowEndpointThresholdMs;
    }

    public double getHighErrorRateThreshold() {
        return highErrorRateThreshold;
    }

    public void setHighErrorRateThreshold(double highErrorRateThreshold) {
        this.highErrorRateThreshold = highErrorRateThreshold;
    }

    public double getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
    }

    public int getDataRetentionDays() {
        return dataRetentionDays;
    }

    public void setDataRetentionDays(int dataRetentionDays) {
        this.dataRetentionDays = dataRetentionDays;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }

    public String getReportOutputPath() {
        return reportOutputPath;
    }

    public void setReportOutputPath(String reportOutputPath) {
        this.reportOutputPath = reportOutputPath;
    }

    public boolean isAutoGenerateReportOnStartup() {
        return autoGenerateReportOnStartup;
    }

    public void setAutoGenerateReportOnStartup(boolean autoGenerateReportOnStartup) {
        this.autoGenerateReportOnStartup = autoGenerateReportOnStartup;
    }

    public boolean isMonitorAllEndpoints() {
        return monitorAllEndpoints;
    }

    public void setMonitorAllEndpoints(boolean monitorAllEndpoints) {
        this.monitorAllEndpoints = monitorAllEndpoints;
    }

    public int getMaxCachedMetricsPerEndpoint() {
        return maxCachedMetricsPerEndpoint;
    }

    public void setMaxCachedMetricsPerEndpoint(int maxCachedMetricsPerEndpoint) {
        this.maxCachedMetricsPerEndpoint = maxCachedMetricsPerEndpoint;
    }

    public double getMaxMemoryUsageThreshold() {
        return maxMemoryUsageThreshold;
    }

    public void setMaxMemoryUsageThreshold(double maxMemoryUsageThreshold) {
        this.maxMemoryUsageThreshold = maxMemoryUsageThreshold;
    }

    public int getAsyncThreadPoolSize() {
        return asyncThreadPoolSize;
    }

    public void setAsyncThreadPoolSize(int asyncThreadPoolSize) {
        this.asyncThreadPoolSize = asyncThreadPoolSize;
    }
}
