package com.diagnostic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark HTTP endpoints for performance monitoring.
 * Apply this annotation to controller methods to enable automatic collection
 * of performance metrics (response time, QPS, error rate, etc.).
 *
 * <p>Example usage:
 * <pre>
 * &#64;RestController
 * public class UserController {
 *
 *     &#64;GetMapping("/users/{id}")
 *     &#64;DiagnosticEndpoint(name = "getUser", slowThresholdMs = 500)
 *     public User getUser(@PathVariable Long id) {
 *         return userService.findById(id);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiagnosticEndpoint {

    /**
     * Custom name for the endpoint. If not specified, the endpoint path will be used.
     * @return the endpoint name
     */
    String name() default "";

    /**
     * Threshold in milliseconds for marking this endpoint as slow.
     * If set to -1, the global configuration value will be used.
     * @return the slow threshold in milliseconds
     */
    int slowThresholdMs() default -1;

    /**
     * Sampling rate for this endpoint (0.0 to 1.0).
     * If set to 1.0, all requests are monitored.
     * If set to 0.5, approximately 50% of requests are monitored.
     * @return the sampling rate
     */
    double samplingRate() default 1.0;

    /**
     * Whether this endpoint is critical for the application.
     * Critical endpoints are highlighted in health reports.
     * @return true if critical, false otherwise
     */
    boolean critical() default false;
}
