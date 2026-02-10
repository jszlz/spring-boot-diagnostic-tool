package com.diagnostic.dashboard.controller;

import com.diagnostic.dashboard.config.MonitoredAppsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多应用监控仪表板控制器
 */
@RestController
@RequestMapping("/api/dashboard")
public class MultiAppDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(MultiAppDashboardController.class);

    private final MonitoredAppsProperties appsProperties;
    private final RestTemplate restTemplate;

    public MultiAppDashboardController(MonitoredAppsProperties appsProperties, RestTemplate restTemplate) {
        this.appsProperties = appsProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * 获取所有被监控应用列表
     */
    @GetMapping("/apps")
    public List<Map<String, String>> getApps() {
        return appsProperties.getApps().stream()
                .map(app -> {
                    Map<String, String> appInfo = new HashMap<>();
                    appInfo.put("name", app.getName());
                    appInfo.put("url", app.getUrl());
                    return appInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取指定应用的概览数据
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview(@RequestParam(required = false) String appName) {
        MonitoredAppsProperties.AppConfig app = getApp(appName);
        if (app == null) {
            return createEmptyOverview();
        }

        try {
            String url = app.getUrl() + app.getDiagnosticApiPath() + "/health/summary";
            logger.info("Fetching overview from: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            logger.info("Response status: {}", response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, Object> summary = response.getBody();

            if (summary == null) {
                logger.warn("Summary data is null");
                return createEmptyOverview();
            }

            Map<String, Object> overview = new HashMap<>();
            overview.put("appName", app.getName());
            overview.put("totalEndpoints", summary.getOrDefault("totalEndpoints", 0));
            overview.put("totalRisks", summary.getOrDefault("totalRisks", 0));
            overview.put("highRisks", summary.getOrDefault("highSeverityRisks", 0));
            overview.put("dependencies", summary.getOrDefault("totalDependencies", 0));
            overview.put("timestamp", summary.getOrDefault("timestamp", System.currentTimeMillis()));

            return overview;
        } catch (Exception e) {
            logger.error("Error fetching overview: {}", e.getMessage(), e);
            return createEmptyOverview();
        }
    }

    /**
     * 获取指定应用的性能数据
     */
    @GetMapping("/performance")
    public Map<String, Object> getPerformance(@RequestParam(required = false) String appName) {
        MonitoredAppsProperties.AppConfig app = getApp(appName);
        if (app == null) {
            return createEmptyPerformance();
        }

        try {
            String url = app.getUrl() + app.getDiagnosticApiPath() + "/endpoints";
            logger.info("Fetching performance from: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> stats = restTemplate.getForObject(url, Map.class);

            if (stats == null || stats.isEmpty()) {
                return createEmptyPerformance();
            }

            List<Map<String, Object>> endpoints = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : stats.entrySet()) {
                Map<String, Object> ep = new HashMap<>();
                Map<String, Object> s = entry.getValue();

                ep.put("name", entry.getKey());
                ep.put("qps", s.getOrDefault("qps", 0.0));
                ep.put("avgResponseTime", s.getOrDefault("averageResponseTime", 0.0));
                ep.put("p95", s.getOrDefault("p95ResponseTime", 0.0));
                ep.put("p99", s.getOrDefault("p99ResponseTime", 0.0));
                ep.put("errorRate", s.getOrDefault("errorRate", 0.0));
                ep.put("totalRequests", s.getOrDefault("totalRequests", 0));

                endpoints.add(ep);
            }

            endpoints.sort((a, b) -> Double.compare(
                    ((Number) b.get("qps")).doubleValue(),
                    ((Number) a.get("qps")).doubleValue()
            ));

            Map<String, Object> result = new HashMap<>();
            result.put("appName", app.getName());
            result.put("endpoints", endpoints);
            result.put("timestamp", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            logger.error("Error fetching performance: {}", e.getMessage());
            return createEmptyPerformance();
        }
    }

    /**
     * 获取指定应用的拓扑数据
     */
    @GetMapping("/topology")
    public Map<String, Object> getTopology(@RequestParam(required = false) String appName) {
        MonitoredAppsProperties.AppConfig app = getApp(appName);
        if (app == null) {
            return createEmptyTopology();
        }

        try {
            String url = app.getUrl() + app.getDiagnosticApiPath() + "/topology/json";
            logger.info("Fetching topology from: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> topologyData = restTemplate.getForObject(url, Map.class);

            if (topologyData == null) {
                return createEmptyTopology();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) topologyData.get("nodes");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edges = (List<Map<String, Object>>) topologyData.get("edges");

            Map<String, Object> result = new HashMap<>();
            result.put("appName", app.getName());
            result.put("nodes", nodes != null ? nodes : Collections.emptyList());
            result.put("edges", edges != null ? edges : Collections.emptyList());
            result.put("hasCycles", topologyData.getOrDefault("cyclicNodeCount", 0));

            return result;
        } catch (Exception e) {
            logger.error("Error fetching topology: {}", e.getMessage());
            return createEmptyTopology();
        }
    }

    /**
     * 获取指定应用的风险数据
     */
    @GetMapping("/risks")
    public Map<String, Object> getRisks(@RequestParam(required = false) String appName) {
        MonitoredAppsProperties.AppConfig app = getApp(appName);
        if (app == null) {
            return createEmptyRisks();
        }

        try {
            String url = app.getUrl() + app.getDiagnosticApiPath() + "/risks";
            logger.info("Fetching risks from: {}", url);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> risks = restTemplate.getForObject(url, List.class);

            if (risks == null) {
                return createEmptyRisks();
            }

            List<Map<String, Object>> riskList = new ArrayList<>();
            for (Map<String, Object> risk : risks) {
                Map<String, Object> r = new HashMap<>();
                r.put("type", risk.get("type"));
                r.put("severity", risk.get("severity"));
                r.put("description", risk.get("description"));
                r.put("affectedComponents", Collections.singletonList(risk.get("component")));

                @SuppressWarnings("unchecked")
                List<String> recommendations = (List<String>) risk.get("recommendations");
                r.put("recommendation", recommendations != null && !recommendations.isEmpty() ?
                        String.join("; ", recommendations) : "请检查相关组件");

                riskList.add(r);
            }

            riskList.sort((a, b) -> {
                String sevA = (String) a.get("severity");
                String sevB = (String) b.get("severity");
                return sevB.compareTo(sevA);
            });

            Map<String, Object> result = new HashMap<>();
            result.put("appName", app.getName());
            result.put("risks", riskList);
            result.put("total", riskList.size());
            result.put("high", riskList.stream().filter(r -> "HIGH".equals(r.get("severity"))).count());
            result.put("medium", riskList.stream().filter(r -> "MEDIUM".equals(r.get("severity"))).count());
            result.put("low", riskList.stream().filter(r -> "LOW".equals(r.get("severity"))).count());

            return result;
        } catch (Exception e) {
            logger.error("Error fetching risks: {}", e.getMessage());
            return createEmptyRisks();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health(@RequestParam(required = false) String appName) {
        MonitoredAppsProperties.AppConfig app = getApp(appName);
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("totalApps", appsProperties.getApps().size());
        
        if (app != null) {
            health.put("currentApp", app.getName());
            health.put("monitoredApp", app.getUrl());
            
            try {
                String url = app.getUrl() + app.getDiagnosticApiPath() + "/status";
                restTemplate.getForObject(url, Map.class);
                health.put("monitoredAppStatus", "UP");
            } catch (Exception e) {
                health.put("monitoredAppStatus", "DOWN");
                health.put("error", e.getMessage());
            }
        }
        
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    private MonitoredAppsProperties.AppConfig getApp(String appName) {
        if (appName == null || appName.isEmpty()) {
            // 使用默认应用或第一个应用
            String defaultApp = appsProperties.getDefaultApp();
            if (defaultApp != null) {
                return appsProperties.getApps().stream()
                        .filter(app -> app.getName().equals(defaultApp))
                        .findFirst()
                        .orElse(appsProperties.getApps().isEmpty() ? null : appsProperties.getApps().get(0));
            }
            return appsProperties.getApps().isEmpty() ? null : appsProperties.getApps().get(0);
        }

        return appsProperties.getApps().stream()
                .filter(app -> app.getName().equals(appName))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> createEmptyOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalEndpoints", 0);
        overview.put("totalRisks", 0);
        overview.put("highRisks", 0);
        overview.put("dependencies", 0);
        overview.put("timestamp", System.currentTimeMillis());
        return overview;
    }

    private Map<String, Object> createEmptyPerformance() {
        Map<String, Object> result = new HashMap<>();
        result.put("endpoints", Collections.emptyList());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    private Map<String, Object> createEmptyTopology() {
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", Collections.emptyList());
        result.put("edges", Collections.emptyList());
        result.put("hasCycles", false);
        return result;
    }

    private Map<String, Object> createEmptyRisks() {
        Map<String, Object> result = new HashMap<>();
        result.put("risks", Collections.emptyList());
        result.put("total", 0);
        result.put("high", 0);
        result.put("medium", 0);
        result.put("low", 0);
        return result;
    }
}
