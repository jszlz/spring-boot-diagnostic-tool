package com.diagnostic.core.collector;

import com.diagnostic.core.model.CpuMetrics;
import com.diagnostic.core.model.FileDescriptorMetrics;
import com.diagnostic.core.model.SystemResourceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * 系统收集器
 * Collector for CPU and system resource metrics using MXBeans
 */
public class SystemCollector {

    private static final Logger logger = LoggerFactory.getLogger(SystemCollector.class);

    private final OperatingSystemMXBean osMXBean;
    private final com.sun.management.OperatingSystemMXBean sunOsMXBean; // nullable

    public SystemCollector() {
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        
        // Try to cast to extended interface for additional metrics
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            this.sunOsMXBean = (com.sun.management.OperatingSystemMXBean) osMXBean;
        } else {
            this.sunOsMXBean = null;
            logger.warn("Extended OS metrics not available on this JVM");
        }
    }

    /**
     * 收集 CPU 指标
     * Collect CPU metrics including process and system CPU usage
     */
    public CpuMetrics collectCpuMetrics() {
        try {
            CpuMetrics.Builder builder = CpuMetrics.builder()
                    .systemLoadAverage(osMXBean.getSystemLoadAverage());

            if (sunOsMXBean != null) {
                // Convert from 0.0-1.0 to 0.0-100.0
                double processCpu = sunOsMXBean.getProcessCpuLoad() * 100.0;
                double systemCpu = sunOsMXBean.getSystemCpuLoad() * 100.0;
                
                builder.processCpuUsage(processCpu >= 0 ? processCpu : 0.0);
                builder.systemCpuUsage(systemCpu >= 0 ? systemCpu : 0.0);
            } else {
                builder.processCpuUsage(0.0);
                builder.systemCpuUsage(0.0);
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Error collecting CPU metrics: {}", e.getMessage());
            return new CpuMetrics();
        }
    }

    /**
     * 收集系统资源指标
     * Collect system resource metrics including memory and file descriptors
     */
    public SystemResourceMetrics collectSystemMetrics() {
        try {
            SystemResourceMetrics.Builder builder = SystemResourceMetrics.builder()
                    .availableProcessors(osMXBean.getAvailableProcessors());

            if (sunOsMXBean != null) {
                builder.totalPhysicalMemory(sunOsMXBean.getTotalPhysicalMemorySize());
                builder.freePhysicalMemory(sunOsMXBean.getFreePhysicalMemorySize());
                builder.committedVirtualMemory(sunOsMXBean.getCommittedVirtualMemorySize());
                
                // File descriptors only available on Unix-like systems
                FileDescriptorMetrics fdMetrics = collectFileDescriptorMetrics();
                if (fdMetrics != null) {
                    builder.fileDescriptors(fdMetrics);
                }
            } else {
                builder.totalPhysicalMemory(0);
                builder.freePhysicalMemory(0);
                builder.committedVirtualMemory(0);
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Error collecting system metrics: {}", e.getMessage());
            return new SystemResourceMetrics();
        }
    }

    /**
     * 收集文件描述符指标（仅 Unix 系统）
     * Collect file descriptor metrics (Unix-like systems only)
     */
    private FileDescriptorMetrics collectFileDescriptorMetrics() {
        if (sunOsMXBean == null) {
            return null;
        }

        try {
            // Use reflection to access file descriptor methods (not available in all JDK versions)
            java.lang.reflect.Method getOpenFdMethod = sunOsMXBean.getClass().getMethod("getOpenFileDescriptorCount");
            java.lang.reflect.Method getMaxFdMethod = sunOsMXBean.getClass().getMethod("getMaxFileDescriptorCount");
            
            long openFd = (Long) getOpenFdMethod.invoke(sunOsMXBean);
            long maxFd = (Long) getMaxFdMethod.invoke(sunOsMXBean);

            if (openFd >= 0 && maxFd > 0) {
                return FileDescriptorMetrics.builder()
                        .openCount(openFd)
                        .maxCount(maxFd)
                        .build();
            }
        } catch (Exception e) {
            logger.debug("File descriptor metrics not available: {}", e.getMessage());
        }

        return null;
    }
}
