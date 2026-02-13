package com.diagnostic.core.collector;

import com.diagnostic.core.model.ThreadMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * 线程收集器
 * Collector for thread pool metrics using MXBeans
 */
public class ThreadCollector {

    private static final Logger logger = LoggerFactory.getLogger(ThreadCollector.class);

    private final ThreadMXBean threadMXBean;

    public ThreadCollector() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }

    /**
     * 收集线程指标
     * Collect thread metrics including thread count, peak, daemon, and total started
     */
    public ThreadMetrics collectThreadMetrics() {
        try {
            return ThreadMetrics.builder()
                    .threadCount(threadMXBean.getThreadCount())
                    .peakThreadCount(threadMXBean.getPeakThreadCount())
                    .daemonThreadCount(threadMXBean.getDaemonThreadCount())
                    .totalStartedThreadCount(threadMXBean.getTotalStartedThreadCount())
                    .build();
        } catch (Exception e) {
            logger.error("Error collecting thread metrics: {}", e.getMessage());
            return new ThreadMetrics();
        }
    }
}
