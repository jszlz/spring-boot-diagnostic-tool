package com.diagnostic.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable diagnostic functionality in a Spring Boot application.
 * Apply this annotation to a @Configuration class to activate all diagnostic modules.
 *
 * <p>Example usage:
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableDiagnostics
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DiagnosticAutoConfiguration.class)
public @interface EnableDiagnostics {

    /**
     * Whether to automatically generate a health report on application startup.
     * @return true to auto-generate report, false otherwise
     */
    boolean autoReport() default false;
}
