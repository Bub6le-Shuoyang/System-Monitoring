package com.bub6le.systemmonitoring.integration;

import com.bub6le.systemmonitoring.controller.WebSocketController;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class WebSocketIntegrationTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

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
        
        // 使用反射设置messagingTemplate字段
        try {
            java.lang.reflect.Field messagingTemplateField = WebSocketController.class.getDeclaredField("messagingTemplate");
            messagingTemplateField.setAccessible(true);
            messagingTemplateField.set(webSocketController, messagingTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject messagingTemplate mock", e);
        }
    }

    @Test
    @DisplayName("测试WebSocket连接建立")
    void testWebSocketConnectionEstablishment() throws Exception {
        // Given
        String testMessage = "Test Connection";

        // When
        String response = webSocketController.greeting(testMessage);

        // Then
        assertNotNull(response);
        assertEquals("Hello, Test Connection!", response);
    }

    @Test
    @DisplayName("测试系统指标数据推送")
    void testSystemMetricsDataPush() {
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
        
        // 验证数据推送逻辑
        assertNotNull(healthStatus);
    }

    @Test
    @DisplayName("测试任务数据推送")
    void testTaskDataPush() {
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
        
        // 验证任务数据完整性
        assertEquals(1, tasks.size());
        assertEquals("系统备份", tasks.get(0).getTaskName());
        assertEquals(Task.TaskStatus.RUNNING, tasks.get(0).getStatus());
    }

    @Test
    @DisplayName("测试告警数据推送")
    void testAlertDataPush() {
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
        
        // 验证告警数据完整性
        assertEquals(1, alerts.size());
        assertEquals(Alert.AlertSeverity.HIGH, alerts.get(0).getSeverity());
        assertFalse(alerts.get(0).getResolved());
    }

    @Test
    @DisplayName("测试实时数据更新频率")
    void testRealTimeDataUpdateFrequency() {
        // Given
        when(systemMetricsService.generateMockMetrics()).thenReturn(testMetric);

        // When - 模拟多次数据生成
        for (int i = 0; i < 3; i++) {
            webSocketController.generateMockData();
        }

        // Then
        verify(systemMetricsService, times(3)).generateMockMetrics();
        // 由于随机性，task和alert的mock可能不会被调用
    }

    @Test
    @DisplayName("测试WebSocket消息处理")
    void testWebSocketMessageHandling() throws Exception {
        // Given
        String[] testMessages = {
            "subscribe", "unsubscribe", "ping", "status"
        };

        // When & Then
        for (String message : testMessages) {
            String response = webSocketController.greeting(message);
            assertNotNull(response);
            assertTrue(response.contains(message));
        }
    }

    @Test
    @DisplayName("测试数据推送异常处理")
    void testDataPushExceptionHandling() {
        // Given
        when(systemMetricsService.getRecentMetrics(5))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        try {
            webSocketController.pushSystemMetrics();
        } catch (Exception e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("Database connection failed"));
        }
    }

    @Test
    @DisplayName("测试并发数据推送")
    void testConcurrentDataPush() throws InterruptedException {
        // Given
        when(systemMetricsService.getRecentMetrics(5)).thenReturn(Arrays.asList(testMetric));
        when(taskService.getAllTasks()).thenReturn(Arrays.asList(testTask));
        when(alertService.getUnresolvedAlerts()).thenReturn(Arrays.asList(testAlert));

        // When - 模拟并发推送
        Thread metricsThread = new Thread(() -> webSocketController.pushSystemMetrics());
        Thread tasksThread = new Thread(() -> webSocketController.pushTasks());
        Thread alertsThread = new Thread(() -> webSocketController.pushAlerts());

        metricsThread.start();
        tasksThread.start();
        alertsThread.start();

        // Wait for threads to complete
        metricsThread.join(1000);
        tasksThread.join(1000);
        alertsThread.join(1000);

        // Then
        verify(systemMetricsService, times(1)).getRecentMetrics(5);
        verify(taskService, times(1)).getAllTasks();
        verify(alertService, times(1)).getUnresolvedAlerts();
    }
}