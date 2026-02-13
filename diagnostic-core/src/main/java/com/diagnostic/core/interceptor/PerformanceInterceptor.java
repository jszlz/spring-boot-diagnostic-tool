package com.diagnostic.core.interceptor;

import com.diagnostic.core.annotation.DiagnosticEndpoint;
import com.diagnostic.core.collector.PerformanceCollector;
import com.diagnostic.core.model.EndpointMetrics;
import com.diagnostic.core.model.EndpointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor for monitoring HTTP endpoint performance.
 * Captures request timing, status codes, and other metrics for endpoints
 * annotated with @DiagnosticEndpoint or when monitoring all endpoints is enabled.
 */
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "diagnostic.startTime";
    private static final String ENDPOINT_NAME_ATTRIBUTE = "diagnostic.endpointName";

    private final PerformanceCollector performanceCollector;
    private final boolean monitorAllEndpoints;
    private final double samplingRate;

    public PerformanceInterceptor(PerformanceCollector performanceCollector, 
                                 boolean monitorAllEndpoints,
                                 double samplingRate) {
        this.performanceCollector = performanceCollector;
        this.monitorAllEndpoints = monitorAllEndpoints;
        this.samplingRate = samplingRate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Skip Spring Boot's internal error handler
        String uri = request.getRequestURI();
        if (uri != null && uri.endsWith("/error")) {
            return true;
        }
        
        if (!shouldMonitor(handler)) {
            return true;
        }

        // Apply sampling rate
        if (!shouldSample(handler)) {
            return true;
        }

        // Record start time
        long startTime = System.nanoTime();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        // Store endpoint name for later use
        String endpointName = extractEndpointName(request, handler);
        request.setAttribute(ENDPOINT_NAME_ATTRIBUTE, endpointName);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime == null) {
            return; // Not monitored
        }

        try {
            long duration = System.nanoTime() - startTime;
            String endpointName = (String) request.getAttribute(ENDPOINT_NAME_ATTRIBUTE);
            
            if (endpointName == null) {
                endpointName = extractEndpointName(request, handler);
            }

            // Extract controller information
            String controllerClass = null;
            String controllerMethod = null;
            EndpointType endpointType = EndpointType.BUSINESS; // Default to business
            
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                controllerClass = handlerMethod.getBeanType().getName();
                controllerMethod = handlerMethod.getMethod().getName();
                
                // Determine endpoint type based on controller package or path
                endpointType = determineEndpointType(controllerClass, endpointName);
            }

            // Extract client IP and user agent
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            // Build metrics
            EndpointMetrics metrics = EndpointMetrics.builder()
                    .endpoint(endpointName)
                    .duration(duration)
                    .statusCode(response.getStatus())
                    .timestamp(System.currentTimeMillis())
                    .method(request.getMethod())
                    .controllerClass(controllerClass)
                    .controllerMethod(controllerMethod)
                    .endpointType(endpointType)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .build();

            // Collect metrics asynchronously
            performanceCollector.collect(metrics);

        } catch (Exception e) {
            // Silent failure - don't break the request
            logger.warn("Error collecting performance metrics: {}", e.getMessage());
        }
    }

    /**
     * Determine if the handler should be monitored.
     *
     * @param handler the handler object
     * @return true if should monitor, false otherwise
     */
    private boolean shouldMonitor(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check if @DiagnosticEndpoint annotation is present
        DiagnosticEndpoint annotation = handlerMethod.getMethodAnnotation(DiagnosticEndpoint.class);
        if (annotation != null) {
            return true;
        }

        // Check if monitoring all endpoints
        return monitorAllEndpoints;
    }

    /**
     * Determine if this request should be sampled based on sampling rate.
     *
     * @param handler the handler object
     * @return true if should sample, false otherwise
     */
    private boolean shouldSample(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        DiagnosticEndpoint annotation = handlerMethod.getMethodAnnotation(DiagnosticEndpoint.class);

        // Use annotation-specific sampling rate if available
        double effectiveSamplingRate = samplingRate;
        if (annotation != null && annotation.samplingRate() >= 0) {
            effectiveSamplingRate = annotation.samplingRate();
        }

        // Always sample if rate is 1.0
        if (effectiveSamplingRate >= 1.0) {
            return true;
        }

        // Never sample if rate is 0.0
        if (effectiveSamplingRate <= 0.0) {
            return false;
        }

        // Probabilistic sampling
        return Math.random() < effectiveSamplingRate;
    }

    /**
     * Extract endpoint name from request and handler.
     *
     * @param request the HTTP request
     * @param handler the handler object
     * @return endpoint name
     */
    private String extractEndpointName(HttpServletRequest request, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            DiagnosticEndpoint annotation = handlerMethod.getMethodAnnotation(DiagnosticEndpoint.class);
            
            // Use custom name from annotation if provided
            if (annotation != null && annotation.name() != null && !annotation.name().isEmpty()) {
                return annotation.name();
            }
        }

        // Use request URI as default
        String uri = request.getRequestURI();
        if (uri == null) {
            uri = "/unknown";
        }
        
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        String method = request.getMethod();
        if (method == null) {
            method = "UNKNOWN";
        }

        return method + " " + uri;
    }

    /**
     * Determine endpoint type based on controller class and endpoint path.
     * 
     * @param controllerClass the controller class name
     * @param endpointName the endpoint name (format: "METHOD /path")
     * @return endpoint type (BUSINESS or DIAGNOSTIC)
     */
    private EndpointType determineEndpointType(String controllerClass, String endpointName) {
        // Priority 1: Check if controller is in diagnostic package
        if (controllerClass != null && controllerClass.contains("com.diagnostic")) {
            logger.debug("Endpoint classified as DIAGNOSTIC based on controller package: {}", controllerClass);
            return EndpointType.DIAGNOSTIC;
        }
        
        // Priority 2: Check if endpoint path contains diagnostic keywords
        if (endpointName != null) {
            String lowerPath = endpointName.toLowerCase();
            if (lowerPath.contains("/diagnostic") || 
                lowerPath.contains("/actuator") ||
                lowerPath.contains("/metrics") ||
                lowerPath.contains("/health") ||
                lowerPath.contains("/monitor") ||
                lowerPath.contains("/jvm")) {
                logger.debug("Endpoint classified as DIAGNOSTIC based on path: {}", endpointName);
                return EndpointType.DIAGNOSTIC;
            }
        }
        
        logger.debug("Endpoint classified as BUSINESS: controller={}, path={}", controllerClass, endpointName);
        return EndpointType.BUSINESS;
    }

    /**
     * 获取客户端真实IP地址
     * 考虑了Nginx、Apache等反向代理的情况
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // X-Forwarded-For可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
