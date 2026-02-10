package com.diagnostic.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 被监控应用配置
 */
@Component
@ConfigurationProperties(prefix = "monitored")
public class MonitoredAppsProperties {

    private List<AppConfig> apps = new ArrayList<>();
    private String defaultApp;

    public List<AppConfig> getApps() {
        return apps;
    }

    public void setApps(List<AppConfig> apps) {
        this.apps = apps;
    }

    public String getDefaultApp() {
        return defaultApp;
    }

    public void setDefaultApp(String defaultApp) {
        this.defaultApp = defaultApp;
    }

    public static class AppConfig {
        private String name;
        private String url;
        private String diagnosticApiPath = "/diagnostic";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDiagnosticApiPath() {
            return diagnosticApiPath;
        }

        public void setDiagnosticApiPath(String diagnosticApiPath) {
            this.diagnosticApiPath = diagnosticApiPath;
        }
    }
}
