package com.diagnostic.core.discovery;

import com.diagnostic.core.model.DependencyType;
import com.diagnostic.core.model.ExternalDependency;
import com.diagnostic.core.registry.DependencyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * Bean post processor that automatically discovers external dependencies in the Spring context.
 * This processor scans all beans and identifies:
 * - DataSource beans (databases)
 * - Redis connections (RedisTemplate, RedisConnectionFactory)
 * - HTTP clients (Feign clients, RestTemplate)
 * - Message queues (RabbitMQ, Kafka)
 */
@Component
public class DependencyDiscoveryPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DependencyDiscoveryPostProcessor.class);

    private ApplicationContext applicationContext;
    private final DependencyRegistry dependencyRegistry;

    public DependencyDiscoveryPostProcessor(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            // Detect DataSource
            if (bean instanceof DataSource) {
                registerDatabaseDependency((DataSource) bean, beanName);
            }

            // Detect Redis
            if (isRedisConnectionFactory(bean)) {
                registerRedisDependency(bean, beanName);
            } else if (isRedisTemplate(bean)) {
                registerRedisTemplateDependency(bean, beanName);
            }

            // Detect Feign clients
            if (isFeignClient(bean)) {
                registerHttpClientDependency(bean, beanName);
            }

            // Detect RestTemplate
            if (isRestTemplate(bean)) {
                registerRestTemplateDependency(bean, beanName);
            }

            // Detect RabbitMQ
            if (isRabbitConnectionFactory(bean)) {
                registerRabbitMQDependency(bean, beanName);
            }

            // Detect Kafka
            if (isKafkaTemplate(bean)) {
                registerKafkaDependency(bean, beanName);
            }

        } catch (Exception e) {
            // Silent failure - don't break application startup
            logger.warn("Error discovering dependency for bean {}: {}", beanName, e.getMessage());
        }

        return bean;
    }

    private void registerDatabaseDependency(DataSource dataSource, String beanName) {
        try {
            String connectionUrl = extractConnectionUrl(dataSource);
            String id = "database-" + beanName;

            ExternalDependency dependency = new ExternalDependency(id, DependencyType.DATABASE);
            dependency.setConnectionString(connectionUrl);
            dependency.setCritical(true);
            dependency.getMetadata().put("beanName", beanName);
            dependency.getMetadata().put("type", "DataSource");

            dependencyRegistry.register(dependency);
            logger.info("Discovered database dependency: {}", id);
        } catch (Exception e) {
            logger.warn("Failed to extract database connection info: {}", e.getMessage());
        }
    }

    private String extractConnectionUrl(DataSource dataSource) {
        try {
            // Try to get JDBC URL using reflection
            Method getUrlMethod = dataSource.getClass().getMethod("getJdbcUrl");
            return (String) getUrlMethod.invoke(dataSource);
        } catch (Exception e1) {
            try {
                Method getUrlMethod = dataSource.getClass().getMethod("getUrl");
                return (String) getUrlMethod.invoke(dataSource);
            } catch (Exception e2) {
                return "unknown";
            }
        }
    }

    private void registerRedisDependency(Object bean, String beanName) {
        String id = "redis-" + beanName;
        ExternalDependency dependency = new ExternalDependency(id, DependencyType.REDIS);
        dependency.setCritical(true);
        dependency.getMetadata().put("beanName", beanName);
        dependency.getMetadata().put("type", "RedisConnectionFactory");

        dependencyRegistry.register(dependency);
        logger.info("Discovered Redis dependency: {}", id);
    }

    private void registerRedisTemplateDependency(Object bean, String beanName) {
        String id = "redis-template-" + beanName;
        ExternalDependency dependency = new ExternalDependency(id, DependencyType.REDIS);
        dependency.setCritical(true);
        dependency.getMetadata().put("beanName", beanName);
        dependency.getMetadata().put("type", "RedisTemplate");

        dependencyRegistry.register(dependency);
        logger.info("Discovered Redis template dependency: {}", id);
    }

    private void registerHttpClientDependency(Object bean, String beanName) {
        String id = "http-client-" + beanName;
        ExternalDependency dependency = new ExternalDependency(id, DependencyType.HTTP_SERVICE);
        dependency.getMetadata().put("beanName", beanName);
        dependency.getMetadata().put("type", "FeignClient");

        // Try to extract service name from @FeignClient annotation
        try {
            Class<?> beanClass = bean.getClass();
            for (Class<?> iface : beanClass.getInterfaces()) {
                if (iface.isAnnotationPresent(getFeignClientAnnotation())) {
                    Object annotation = iface.getAnnotation(getFeignClientAnnotation());
                    String serviceName = extractFeignServiceName(annotation);
                    if (serviceName != null && !serviceName.isEmpty()) {
                        dependency.getMetadata().put("serviceName", serviceName);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract Feign service name: {}", e.getMessage());
        }

        dependencyRegistry.register(dependency);
        logger.info("Discovered HTTP client dependency: {}", id);
    }

    private void registerRestTemplateDependency(Object bean, String beanName) {
        String id = "rest-template-" + beanName;
        ExternalDependency dependency = new ExternalDependency(id, DependencyType.HTTP_SERVICE);
        dependency.getMetadata().put("beanName", beanName);
        dependency.getMetadata().put("type", "RestTemplate");

        dependencyRegistry.register(dependency);
        logger.info("Discovered RestTemplate dependency: {}", id);
    }

    private void registerRabbitMQDependency(Object bean, String beanName) {
        String id = "rabbitmq-" + beanName;
        ExternalDependency dependency = new ExternalDependency(id, DependencyType.MESSAGE_QUEUE);
        dependency.setCritical(true);
        dependency.getMetadata().put("beanName", beanName);
        dependency.getMetadata().put("type", "RabbitMQ");

        dependencyRegistry.register(dependency);
        logger.info("Discovered RabbitMQ dependency: {}", id);
    }

    private void registerKafkaDependency(Object bean, String beanName) {
        String id = "kafka-" + beanName;
        ExternalDependency dependency = new ExternalDependency(id, DependencyType.MESSAGE_QUEUE);
        dependency.setCritical(true);
        dependency.getMetadata().put("beanName", beanName);
        dependency.getMetadata().put("type", "Kafka");

        dependencyRegistry.register(dependency);
        logger.info("Discovered Kafka dependency: {}", id);
    }

    // Type checking methods using class name to avoid hard dependencies

    private boolean isRedisConnectionFactory(Object bean) {
        return bean.getClass().getName().contains("RedisConnectionFactory");
    }

    private boolean isRedisTemplate(Object bean) {
        return bean.getClass().getName().contains("RedisTemplate");
    }

    private boolean isFeignClient(Object bean) {
        Class<?> beanClass = bean.getClass();
        for (Class<?> iface : beanClass.getInterfaces()) {
            try {
                if (iface.isAnnotationPresent(getFeignClientAnnotation())) {
                    return true;
                }
            } catch (Exception e) {
                // Annotation class not available
            }
        }
        return false;
    }

    private boolean isRestTemplate(Object bean) {
        return bean.getClass().getName().contains("RestTemplate");
    }

    private boolean isRabbitConnectionFactory(Object bean) {
        return bean.getClass().getName().contains("RabbitConnectionFactory") ||
               bean.getClass().getName().contains("ConnectionFactory") && 
               bean.getClass().getName().contains("rabbit");
    }

    private boolean isKafkaTemplate(Object bean) {
        return bean.getClass().getName().contains("KafkaTemplate");
    }

    @SuppressWarnings("unchecked")
    private Class<? extends java.lang.annotation.Annotation> getFeignClientAnnotation() throws ClassNotFoundException {
        return (Class<? extends java.lang.annotation.Annotation>) 
            Class.forName("org.springframework.cloud.openfeign.FeignClient");
    }

    private String extractFeignServiceName(Object annotation) {
        try {
            Method nameMethod = annotation.getClass().getMethod("name");
            String name = (String) nameMethod.invoke(annotation);
            if (name != null && !name.isEmpty()) {
                return name;
            }

            Method valueMethod = annotation.getClass().getMethod("value");
            return (String) valueMethod.invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }
}
