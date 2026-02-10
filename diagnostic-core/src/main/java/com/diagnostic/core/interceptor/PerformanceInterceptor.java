package com.diagnostic.core.interceptor;

import com.diagnostic.core.annotation.DiagnosticEndpoint;
import com.diagnostic.core.collector.PerformanceCollector;
import com.diagnostic.core.model.EndpointMetrics;
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
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                controllerClass = handlerMethod.getBeanType().getName();
                controllerMethod = handlerMethod.getMethod().getName();
            }

            // Build metrics
            EndpointMetrics metrics = EndpointMetrics.builder()
                    .endpoint(endpointName)
                    .duration(duration)
                    .statusCode(response.getStatus())
                    .timestamp(System.currentTimeMillis())
                    .method(request.getMethod())
                    .controllerClass(controllerClass)
                    .controllerMethod(controllerMethod)
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
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        return request.getMethod() + " " + uri;
    }
}
