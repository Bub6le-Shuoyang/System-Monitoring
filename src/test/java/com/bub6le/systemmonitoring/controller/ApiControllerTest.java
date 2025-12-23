package com.bub6le.systemmonitoring.controller;

import com.bub6le.systemmonitoring.model.Alert;
import com.bub6le.systemmonitoring.model.SystemMetrics;
import com.bub6le.systemmonitoring.model.Task;
import com.bub6le.systemmonitoring.service.AlertService;
import com.bub6le.systemmonitoring.service.SystemMetricsService;
import com.bub6le.systemmonitoring.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    @Mock
    private SystemMetricsService systemMetricsService;

    @Mock
    private TaskService taskService;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private ApiController apiController;

    private SystemMetrics testMetric;
    private Task testTask;
    private Alert testAlert;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testMetric = new SystemMetrics(
            "server-01", 75.5, 60.2, 45.8, 500.0, 250.0, 2.5, "北京", "Web服务"
        );
        testMetric.setId(1L);

        testTask = new Task("系统备份", "生产集群");
        testTask.setId(1L);
        testTask.setStatus(Task.TaskStatus.RUNNING);
        testTask.setProgress(50);

        testAlert = new Alert("server-01", Alert.AlertSeverity.HIGH, "CPU使用率过高");
        testAlert.setId(1L);
        testAlert.setResolved(false);
    }

    @Test
    @DisplayName("测试获取所有系统指标")
    void testGetAllMetrics() {
        // Given
        List<SystemMetrics> metrics = Arrays.asList(testMetric);
        when(systemMetricsService.getAllMetrics()).thenReturn(metrics);

        // When
        List<SystemMetrics> result = apiController.getAllMetrics();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("server-01", result.get(0).getServerName());
        assertEquals(75.5, result.get(0).getCpuUsage());
        verify(systemMetricsService, times(1)).getAllMetrics();
    }

    @Test
    @DisplayName("测试获取最近的系统指标")
    void testGetRecentMetrics() {
        // Given
        List<SystemMetrics> metrics = Arrays.asList(testMetric);
        when(systemMetricsService.getRecentMetrics(5)).thenReturn(metrics);

        // When
        List<SystemMetrics> result = apiController.getRecentMetrics(5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("server-01", result.get(0).getServerName());
        verify(systemMetricsService, times(1)).getRecentMetrics(5);
    }

    @Test
    @DisplayName("测试根据服务器名称获取系统指标")
    void testGetMetricsByServer() {
        // Given
        List<SystemMetrics> metrics = Arrays.asList(testMetric);
        when(systemMetricsService.getMetricsByServer("server-01")).thenReturn(metrics);

        // When
        List<SystemMetrics> result = apiController.getMetricsByServer("server-01");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("server-01", result.get(0).getServerName());
        verify(systemMetricsService, times(1)).getMetricsByServer("server-01");
    }

    @Test
    @DisplayName("测试根据区域获取系统指标")
    void testGetMetricsByRegion() {
        // Given
        List<SystemMetrics> metrics = Arrays.asList(testMetric);
        when(systemMetricsService.getMetricsByRegion("北京")).thenReturn(metrics);

        // When
        List<SystemMetrics> result = apiController.getMetricsByRegion("北京");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("北京", result.get(0).getRegion());
        verify(systemMetricsService, times(1)).getMetricsByRegion("北京");
    }

    @Test
    @DisplayName("测试根据服务类型获取系统指标")
    void testGetMetricsByServiceType() {
        // Given
        List<SystemMetrics> metrics = Arrays.asList(testMetric);
        when(systemMetricsService.getMetricsByServiceType("Web服务")).thenReturn(metrics);

        // When
        List<SystemMetrics> result = apiController.getMetricsByServiceType("Web服务");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Web服务", result.get(0).getServiceType());
        verify(systemMetricsService, times(1)).getMetricsByServiceType("Web服务");
    }

    @Test
    @DisplayName("测试获取系统健康状态")
    void testGetSystemHealth() {
        // Given
        SystemMetricsService.SystemHealthStatus healthStatus = 
            new SystemMetricsService.SystemHealthStatus("健康", 65.5, 55.2, 2.1, 3);
        when(systemMetricsService.getSystemHealthStatus()).thenReturn(healthStatus);

        // When
        SystemMetricsService.SystemHealthStatus result = apiController.getSystemHealth();

        // Then
        assertNotNull(result);
        assertEquals("健康", result.getStatus());
        assertEquals(65.5, result.getAvgCpu());
        assertEquals(55.2, result.getAvgMemory());
        assertEquals(2.1, result.getAvgLoad());
        assertEquals(3, result.getServerCount());
        verify(systemMetricsService, times(1)).getSystemHealthStatus();
    }

    @Test
    @DisplayName("测试生成模拟系统指标")
    void testGenerateMockMetrics() {
        // Given
        when(systemMetricsService.generateMockMetrics()).thenReturn(testMetric);

        // When
        SystemMetrics result = apiController.generateMockMetrics();

        // Then
        assertNotNull(result);
        assertEquals("server-01", result.getServerName());
        assertEquals(75.5, result.getCpuUsage());
        verify(systemMetricsService, times(1)).generateMockMetrics();
    }

    @Test
    @DisplayName("测试获取所有任务")
    void testGetAllTasks() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When
        List<Task> result = apiController.getAllTasks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("系统备份", result.get(0).getTaskName());
        assertEquals("生产集群", result.get(0).getTargetCluster());
        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @DisplayName("测试根据状态获取任务")
    void testGetTasksByStatus() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByStatus(Task.TaskStatus.RUNNING)).thenReturn(tasks);

        // When
        List<Task> result = apiController.getTasksByStatus(Task.TaskStatus.RUNNING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Task.TaskStatus.RUNNING, result.get(0).getStatus());
        verify(taskService, times(1)).getTasksByStatus(Task.TaskStatus.RUNNING);
    }

    @Test
    @DisplayName("测试根据集群获取任务")
    void testGetTasksByCluster() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByCluster("生产集群")).thenReturn(tasks);

        // When
        List<Task> result = apiController.getTasksByCluster("生产集群");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("生产集群", result.get(0).getTargetCluster());
        verify(taskService, times(1)).getTasksByCluster("生产集群");
    }

    @Test
    @DisplayName("测试创建任务")
    void testCreateTask() {
        // Given
        Task newTask = new Task("新任务", "新集群");
        when(taskService.createTask("新任务", "新集群")).thenReturn(newTask);

        // When
        Task result = apiController.createTask("新任务", "新集群");

        // Then
        assertNotNull(result);
        assertEquals("新任务", result.getTaskName());
        assertEquals("新集群", result.getTargetCluster());
        verify(taskService, times(1)).createTask("新任务", "新集群");
    }

    @Test
    @DisplayName("测试更新任务进度")
    void testUpdateTaskProgress() {
        // Given
        doNothing().when(taskService).updateTaskProgress(1L, 75);

        // When
        apiController.updateTaskProgress(1L, 75);

        // Then
        verify(taskService, times(1)).updateTaskProgress(1L, 75);
    }

    @Test
    @DisplayName("测试任务失败")
    void testFailTask() {
        // Given
        doNothing().when(taskService).failTask(1L);

        // When
        apiController.failTask(1L);

        // Then
        verify(taskService, times(1)).failTask(1L);
    }

    @Test
    @DisplayName("测试生成模拟任务")
    void testGenerateMockTask() {
        // Given
        when(taskService.generateMockTask()).thenReturn(testTask);

        // When
        Task result = apiController.generateMockTask();

        // Then
        assertNotNull(result);
        assertEquals("系统备份", result.getTaskName());
        assertEquals("生产集群", result.getTargetCluster());
        verify(taskService, times(1)).generateMockTask();
    }

    @Test
    @DisplayName("测试获取任务摘要")
    void testGetTaskSummary() {
        // Given
        TaskService.TaskStatusSummary summary = 
            new TaskService.TaskStatusSummary(5L, 3L, 2L, 10L);
        when(taskService.getTaskStatusSummary()).thenReturn(summary);

        // When
        TaskService.TaskStatusSummary result = apiController.getTaskSummary();

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getQueuedCount());
        assertEquals(3L, result.getRunningCount());
        assertEquals(2L, result.getFailedCount());
        assertEquals(10L, result.getCompletedCount());
        assertEquals(20L, result.getTotalCount());
        verify(taskService, times(1)).getTaskStatusSummary();
    }

    @Test
    @DisplayName("测试获取所有告警")
    void testGetAllAlerts() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertService.getAllAlerts()).thenReturn(alerts);

        // When
        List<Alert> result = apiController.getAllAlerts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("server-01", result.get(0).getSource());
        assertEquals(Alert.AlertSeverity.HIGH, result.get(0).getSeverity());
        verify(alertService, times(1)).getAllAlerts();
    }

    @Test
    @DisplayName("测试获取未解决的告警")
    void testGetUnresolvedAlerts() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertService.getUnresolvedAlerts()).thenReturn(alerts);

        // When
        List<Alert> result = apiController.getUnresolvedAlerts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getResolved());
        verify(alertService, times(1)).getUnresolvedAlerts();
    }

    @Test
    @DisplayName("测试根据来源获取告警")
    void testGetAlertsBySource() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertService.getAlertsBySource("server-01")).thenReturn(alerts);

        // When
        List<Alert> result = apiController.getAlertsBySource("server-01");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("server-01", result.get(0).getSource());
        verify(alertService, times(1)).getAlertsBySource("server-01");
    }

    @Test
    @DisplayName("测试根据严重程度获取告警")
    void testGetAlertsBySeverity() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertService.getAlertsBySeverity(Alert.AlertSeverity.HIGH)).thenReturn(alerts);

        // When
        List<Alert> result = apiController.getAlertsBySeverity(Alert.AlertSeverity.HIGH);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Alert.AlertSeverity.HIGH, result.get(0).getSeverity());
        verify(alertService, times(1)).getAlertsBySeverity(Alert.AlertSeverity.HIGH);
    }

    @Test
    @DisplayName("测试创建告警")
    void testCreateAlert() {
        // Given
        Alert newAlert = new Alert("test-source", Alert.AlertSeverity.MEDIUM, "测试告警");
        when(alertService.createAlert("test-source", Alert.AlertSeverity.MEDIUM, "测试告警")).thenReturn(newAlert);

        // When
        Alert result = apiController.createAlert("test-source", Alert.AlertSeverity.MEDIUM, "测试告警");

        // Then
        assertNotNull(result);
        assertEquals("test-source", result.getSource());
        assertEquals(Alert.AlertSeverity.MEDIUM, result.getSeverity());
        assertEquals("测试告警", result.getMessage());
        verify(alertService, times(1)).createAlert("test-source", Alert.AlertSeverity.MEDIUM, "测试告警");
    }

    @Test
    @DisplayName("测试解决告警")
    void testResolveAlert() {
        // Given
        doNothing().when(alertService).resolveAlert(1L);

        // When
        apiController.resolveAlert(1L);

        // Then
        verify(alertService, times(1)).resolveAlert(1L);
    }

    @Test
    @DisplayName("测试生成模拟告警")
    void testGenerateMockAlert() {
        // Given
        when(alertService.generateMockAlert()).thenReturn(testAlert);

        // When
        Alert result = apiController.generateMockAlert();

        // Then
        assertNotNull(result);
        assertEquals("server-01", result.getSource());
        assertEquals(Alert.AlertSeverity.HIGH, result.getSeverity());
        assertEquals("CPU使用率过高", result.getMessage());
        verify(alertService, times(1)).generateMockAlert();
    }

    @Test
    @DisplayName("测试获取告警摘要")
    void testGetAlertSummary() {
        // Given
        AlertService.AlertSummary summary = 
            new AlertService.AlertSummary(10L, 4L, 3L, 2L, 1L);
        when(alertService.getAlertSummary()).thenReturn(summary);

        // When
        AlertService.AlertSummary result = apiController.getAlertSummary();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getUnresolvedCount());
        assertEquals(4L, result.getLowCount());
        assertEquals(3L, result.getMediumCount());
        assertEquals(2L, result.getHighCount());
        assertEquals(1L, result.getCriticalCount());
        verify(alertService, times(1)).getAlertSummary();
    }

    @Test
    @DisplayName("测试空结果集")
    void testEmptyResults() {
        // Given
        when(systemMetricsService.getAllMetrics()).thenReturn(Collections.emptyList());
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());
        when(alertService.getAllAlerts()).thenReturn(Collections.emptyList());

        // When & Then
        assertTrue(apiController.getAllMetrics().isEmpty());
        assertTrue(apiController.getAllTasks().isEmpty());
        assertTrue(apiController.getAllAlerts().isEmpty());
    }
}