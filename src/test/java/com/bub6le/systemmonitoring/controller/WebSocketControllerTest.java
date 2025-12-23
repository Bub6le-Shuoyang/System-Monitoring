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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {

    @Mock
    private SystemMetricsService systemMetricsService;

    @Mock
    private TaskService taskService;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private WebSocketController webSocketController;

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
    @DisplayName("测试生成模拟数据")
    void testGenerateMockData() {
        // Given
        when(systemMetricsService.generateMockMetrics()).thenReturn(testMetric);
        when(taskService.generateMockTask()).thenReturn(testTask);
        when(alertService.generateMockAlert()).thenReturn(testAlert);

        // When
        webSocketController.generateMockData();

        // Then
        verify(systemMetricsService, times(1)).generateMockMetrics();
        verify(taskService, atMostOnce()).generateMockTask();
        verify(alertService, atMostOnce()).generateMockAlert();
    }

    @Test
    @DisplayName("测试定时数据生成")
    void testScheduledDataGeneration() {
        // Given
        when(systemMetricsService.generateMockMetrics()).thenReturn(testMetric);
        when(taskService.generateMockTask()).thenReturn(testTask);
        when(alertService.generateMockAlert()).thenReturn(testAlert);

        // When
        webSocketController.scheduledDataGeneration();

        // Then
        // 由于使用了随机数，我们只能验证方法被调用了
        verify(systemMetricsService, atLeastOnce()).generateMockMetrics();
    }

    @Test
    @DisplayName("测试WebSocket消息处理")
    void testGreeting() throws Exception {
        // Given
        String message = "World";

        // When
        String result = webSocketController.greeting(message);

        // Then
        assertEquals("Hello, World!", result);
    }

    @Test
    @DisplayName("测试推送系统指标数据")
    void testPushSystemMetrics() {
        // Given
        List<SystemMetrics> metrics = Arrays.asList(testMetric);
        SystemMetricsService.SystemHealthStatus healthStatus = 
            new SystemMetricsService.SystemHealthStatus("健康", 65.5, 55.2, 2.1, 3);
        
        when(systemMetricsService.getRecentMetrics(5)).thenReturn(metrics);
        when(systemMetricsService.getSystemHealthStatus()).thenReturn(healthStatus);

        // When
        webSocketController.pushSystemMetrics();

        // Then
        verify(systemMetricsService, times(1)).getRecentMetrics(5);
        verify(systemMetricsService, times(1)).getSystemHealthStatus();
    }

    @Test
    @DisplayName("测试推送任务数据")
    void testPushTasks() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        TaskService.TaskStatusSummary summary = 
            new TaskService.TaskStatusSummary(5L, 3L, 2L, 10L);
        
        when(taskService.getAllTasks()).thenReturn(tasks);
        when(taskService.getTaskStatusSummary()).thenReturn(summary);

        // When
        webSocketController.pushTasks();

        // Then
        verify(taskService, times(1)).getAllTasks();
        verify(taskService, times(1)).getTaskStatusSummary();
    }

    @Test
    @DisplayName("测试推送告警数据")
    void testPushAlerts() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        AlertService.AlertSummary summary = 
            new AlertService.AlertSummary(10L, 4L, 3L, 2L, 1L);
        
        when(alertService.getUnresolvedAlerts()).thenReturn(alerts);
        when(alertService.getAlertSummary()).thenReturn(summary);

        // When
        webSocketController.pushAlerts();

        // Then
        verify(alertService, times(1)).getUnresolvedAlerts();
        verify(alertService, times(1)).getAlertSummary();
    }

    @Test
    @DisplayName("测试推送空数据")
    void testPushEmptyData() {
        // Given
        when(systemMetricsService.getRecentMetrics(5)).thenReturn(Collections.emptyList());
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());
        when(alertService.getUnresolvedAlerts()).thenReturn(Collections.emptyList());

        // When
        webSocketController.pushSystemMetrics();
        webSocketController.pushTasks();
        webSocketController.pushAlerts();

        // Then
        verify(systemMetricsService, times(1)).getRecentMetrics(5);
        verify(taskService, times(1)).getAllTasks();
        verify(alertService, times(1)).getUnresolvedAlerts();
    }

    @Test
    @DisplayName("测试WebSocket控制器初始化")
    void testWebSocketControllerInitialization() {
        // Given & When
        WebSocketController controller = new WebSocketController();

        // Then
        assertNotNull(controller);
    }

    @Test
    @DisplayName("测试系统健康状态推送")
    void testSystemHealthStatusPush() {
        // Given
        SystemMetricsService.SystemHealthStatus healthStatus = 
            new SystemMetricsService.SystemHealthStatus("警告", 75.0, 80.0, 4.5, 5);
        
        when(systemMetricsService.getRecentMetrics(5)).thenReturn(Arrays.asList(testMetric));
        when(systemMetricsService.getSystemHealthStatus()).thenReturn(healthStatus);

        // When
        webSocketController.pushSystemMetrics();

        // Then
        verify(systemMetricsService, times(1)).getSystemHealthStatus();
    }

    @Test
    @DisplayName("测试任务状态摘要推送")
    void testTaskStatusSummaryPush() {
        // Given
        TaskService.TaskStatusSummary summary = 
            new TaskService.TaskStatusSummary(0L, 1L, 0L, 5L);
        
        when(taskService.getAllTasks()).thenReturn(Arrays.asList(testTask));
        when(taskService.getTaskStatusSummary()).thenReturn(summary);

        // When
        webSocketController.pushTasks();

        // Then
        verify(taskService, times(1)).getTaskStatusSummary();
    }

    @Test
    @DisplayName("测试告警摘要推送")
    void testAlertSummaryPush() {
        // Given
        AlertService.AlertSummary summary = 
            new AlertService.AlertSummary(0L, 0L, 0L, 0L, 0L);
        
        when(alertService.getUnresolvedAlerts()).thenReturn(Collections.emptyList());
        when(alertService.getAlertSummary()).thenReturn(summary);

        // When
        webSocketController.pushAlerts();

        // Then
        verify(alertService, times(1)).getAlertSummary();
    }
}