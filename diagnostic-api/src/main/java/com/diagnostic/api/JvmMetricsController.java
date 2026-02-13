package com.diagnostic.api;

import com.diagnostic.core.annotation.DiagnosticEndpoint;
import com.diagnostic.core.collector.JvmMetricsCollector;
import com.diagnostic.core.model.*;
import com.diagnostic.core.storage.JvmMetricsStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JVM 指标 REST API 控制器
 * REST API controller for JVM metrics
 */
@RestController
@RequestMapping("/diagnostic/jvm")
@CrossOrigin(origins = "*")
public class JvmMetricsController {

    private final JvmMetricsCollector collector;
    private final JvmMetricsStorage storage;

    public JvmMetricsController(JvmMetricsCollector collector, JvmMetricsStorage storage) {
        this.collector = collector;
        this.storage = storage;
    }

    /**
     * 获取当前完整的 JVM 指标
     * Get current complete JVM metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/current")
    public ResponseEntity<JvmMetrics> getCurrentMetrics() {
        JvmMetrics metrics = storage.getLatest();
        if (metrics == null) {
            // If no metrics available, collect now
            metrics = collector.collectMetrics();
        }
        return ResponseEntity.ok(metrics);
    }

    /**
     * 获取当前堆内存指标
     * Get current heap memory metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/current/heap")
    public ResponseEntity<HeapMemoryMetrics> getCurrentHeapMetrics() {
        JvmMetrics metrics = storage.getLatest();
        if (metrics == null) {
            metrics = collector.collectMetrics();
        }
        return ResponseEntity.ok(metrics.getHeapMemory());
    }

    /**
     * 获取当前 GC 指标
     * Get current GC metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/current/gc")
    public ResponseEntity<GarbageCollectionMetrics> getCurrentGcMetrics() {
        JvmMetrics metrics = storage.getLatest();
        if (metrics == null) {
            metrics = collector.collectMetrics();
        }
        return ResponseEntity.ok(metrics.getGc());
    }

    /**
     * 获取当前线程指标
     * Get current thread metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/current/threads")
    public ResponseEntity<ThreadMetrics> getCurrentThreadMetrics() {
        JvmMetrics metrics = storage.getLatest();
        if (metrics == null) {
            metrics = collector.collectMetrics();
        }
        return ResponseEntity.ok(metrics.getThreads());
    }

    /**
     * 获取当前 CPU 指标
     * Get current CPU metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/current/cpu")
    public ResponseEntity<CpuMetrics> getCurrentCpuMetrics() {
        JvmMetrics metrics = storage.getLatest();
        if (metrics == null) {
            metrics = collector.collectMetrics();
        }
        return ResponseEntity.ok(metrics.getCpu());
    }

    /**
     * 获取当前系统资源指标
     * Get current system resource metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/current/system")
    public ResponseEntity<SystemResourceMetrics> getCurrentSystemMetrics() {
        JvmMetrics metrics = storage.getLatest();
        if (metrics == null) {
            metrics = collector.collectMetrics();
        }
        return ResponseEntity.ok(metrics.getSystem());
    }

    /**
     * 获取历史指标
     * Get historical metrics
     */
    @DiagnosticEndpoint
    @GetMapping("/historical")
    public ResponseEntity<List<JvmMetrics>> getHistoricalMetrics(
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        
        // Default to last 1 hour if no time range specified
        if (startTime == null || endTime == null) {
            endTime = System.currentTimeMillis();
            startTime = endTime - (60 * 60 * 1000); // 1 hour ago
        }

        List<JvmMetrics> metrics = storage.getMetrics(startTime, endTime);
        return ResponseEntity.ok(metrics);
    }

    /**
     * 获取 JVM 监控状态
     * Get JVM monitoring status
     */
    @DiagnosticEndpoint
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", collector.isStarted());
        status.put("collectionIntervalSeconds", collector.getCollectionIntervalSeconds());
        status.put("cachedDataPoints", storage.size());
        status.put("latestTimestamp", storage.getLatest() != null ? storage.getLatest().getTimestamp() : null);
        return ResponseEntity.ok(status);
    }
}
