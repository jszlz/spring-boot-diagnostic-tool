package com.diagnostic.autoconfigure;

import com.diagnostic.core.collector.JvmMetricsCollector;
import com.diagnostic.core.storage.JvmMetricsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * JVM 监控自动配置
 * Auto-configuration for JVM monitoring
 */
@Configuration
@ConditionalOnProperty(prefix = "diagnostic.jvm", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JvmMonitoringProperties.class)
public class JvmMonitoringAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JvmMonitoringAutoConfiguration.class);

    private JvmMetricsCollector collector;

    @Bean
    public JvmMetricsStorage jvmMetricsStorage(JvmMonitoringProperties properties) {
        logger.info("Creating JVM metrics storage with max {} data points", properties.getMaxMemoryDataPoints());
        return new JvmMetricsStorage(
                properties.getMaxMemoryDataPoints(),
                properties.getRetentionDays()
        );
    }

    @Bean
    public JvmMetricsCollector jvmMetricsCollector(
            JvmMetricsStorage storage,
            JvmMonitoringProperties properties) {
        
        logger.info("Creating JVM metrics collector with interval {} seconds", 
                   properties.getCollectionIntervalSeconds());
        
        this.collector = new JvmMetricsCollector(
                storage,
                properties.getCollectionIntervalSeconds()
        );
        
        // Start collection automatically
        collector.start();
        logger.info("JVM monitoring started successfully");
        
        return collector;
    }

    @PreDestroy
    public void shutdown() {
        if (collector != null) {
            logger.info("Shutting down JVM metrics collector");
            collector.stop();
        }
    }
}
