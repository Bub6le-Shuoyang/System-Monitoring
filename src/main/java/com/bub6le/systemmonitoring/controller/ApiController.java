package com.bub6le.systemmonitoring.controller;

import com.bub6le.systemmonitoring.model.Alert;
import com.bub6le.systemmonitoring.model.SystemMetrics;
import com.bub6le.systemmonitoring.model.Task;
import com.bub6le.systemmonitoring.service.AlertService;
import com.bub6le.systemmonitoring.service.SystemMetricsService;
import com.bub6le.systemmonitoring.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    
    @Autowired
    private SystemMetricsService systemMetricsService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AlertService alertService;
    
    // 系统指标相关API
    @GetMapping("/metrics")
    public List<SystemMetrics> getAllMetrics() {
        return systemMetricsService.getAllMetrics();
    }
    
    @GetMapping("/metrics/recent")
    public List<SystemMetrics> getRecentMetrics(@RequestParam(defaultValue = "5") int minutes) {
        return systemMetricsService.getRecentMetrics(minutes);
    }
    
    @GetMapping("/metrics/server/{serverName}")
    public List<SystemMetrics> getMetricsByServer(@PathVariable String serverName) {
        return systemMetricsService.getMetricsByServer(serverName);
    }
    
    @GetMapping("/metrics/region/{region}")
    public List<SystemMetrics> getMetricsByRegion(@PathVariable String region) {
        return systemMetricsService.getMetricsByRegion(region);
    }
    
    @GetMapping("/metrics/service/{serviceType}")
    public List<SystemMetrics> getMetricsByServiceType(@PathVariable String serviceType) {
        return systemMetricsService.getMetricsByServiceType(serviceType);
    }
    
    @GetMapping("/metrics/health")
    public SystemMetricsService.SystemHealthStatus getSystemHealth() {
        return systemMetricsService.getSystemHealthStatus();
    }
    
    @PostMapping("/metrics/generate")
    public SystemMetrics generateMockMetrics() {
        return systemMetricsService.generateMockMetrics();
    }
    
    // 任务相关API
    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    
    @GetMapping("/tasks/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        return taskService.getTasksByStatus(status);
    }
    
    @GetMapping("/tasks/cluster/{cluster}")
    public List<Task> getTasksByCluster(@PathVariable String cluster) {
        return taskService.getTasksByCluster(cluster);
    }
    
    @PostMapping("/tasks")
    public Task createTask(@RequestParam String taskName, @RequestParam String targetCluster) {
        return taskService.createTask(taskName, targetCluster);
    }
    
    @PutMapping("/tasks/{taskId}/progress")
    public void updateTaskProgress(@PathVariable Long taskId, @RequestParam int progress) {
        taskService.updateTaskProgress(taskId, progress);
    }
    
    @PutMapping("/tasks/{taskId}/fail")
    public void failTask(@PathVariable Long taskId) {
        taskService.failTask(taskId);
    }
    
    @PostMapping("/tasks/generate")
    public Task generateMockTask() {
        return taskService.generateMockTask();
    }
    
    @GetMapping("/tasks/summary")
    public TaskService.TaskStatusSummary getTaskSummary() {
        return taskService.getTaskStatusSummary();
    }
    
    // 告警相关API
    @GetMapping("/alerts")
    public List<Alert> getAllAlerts() {
        return alertService.getAllAlerts();
    }
    
    @GetMapping("/alerts/unresolved")
    public List<Alert> getUnresolvedAlerts() {
        return alertService.getUnresolvedAlerts();
    }
    
    @GetMapping("/alerts/source/{source}")
    public List<Alert> getAlertsBySource(@PathVariable String source) {
        return alertService.getAlertsBySource(source);
    }
    
    @GetMapping("/alerts/severity/{severity}")
    public List<Alert> getAlertsBySeverity(@PathVariable Alert.AlertSeverity severity) {
        return alertService.getAlertsBySeverity(severity);
    }
    
    @PostMapping("/alerts")
    public Alert createAlert(@RequestParam String source, 
                           @RequestParam Alert.AlertSeverity severity, 
                           @RequestParam String message) {
        return alertService.createAlert(source, severity, message);
    }
    
    @PutMapping("/alerts/{alertId}/resolve")
    public void resolveAlert(@PathVariable Long alertId) {
        alertService.resolveAlert(alertId);
    }
    
    @PostMapping("/alerts/generate")
    public Alert generateMockAlert() {
        return alertService.generateMockAlert();
    }
    
    @GetMapping("/alerts/summary")
    public AlertService.AlertSummary getAlertSummary() {
        return alertService.getAlertSummary();
    }
}