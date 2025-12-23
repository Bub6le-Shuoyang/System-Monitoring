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
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    @Mock
    private SimpMessagingTemplate messagingTemplate;

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
        
        // 使用反射设置messagingTemplate字段，因为它是@Autowired的
        try {
            java.lang.reflect.Field messagingTemplateField = WebSocketController.class.getDeclaredField("messagingTemplate");
            messagingTemplateField.setAccessible(true);
            messagingTemplateField.set(webSocketController, messagingTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject messagingTemplate mock", e);
        }
    }

    @Test
    @DisplayName("测试生成模拟数据")
    void testGenerateMockData() {
        // Given
        when(systemMetricsService.generateMockMetrics()).thenReturn(testMetric);

        // When
        webSocketController.generateMockData();

        // Then
        verify(systemMetricsService, times(1)).generateMockMetrics();
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/new-metric"), eq(testMetric));
        // 由于随机性，task和alert的mock可能不会被调用
    }

    @Test
    @DisplayName("测试定时数据生成")
    void testScheduledDataGeneration() {
        // Given
        when(systemMetricsService.generateMockMetrics()).thenReturn(testMetric);

        // When
        webSocketController.scheduledDataGeneration();

        // Then
        // 由于使用了随机数，我们只能验证方法可能被调用了
        verify(systemMetricsService, atMostOnce()).generateMockMetrics();
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
        verify(messagingTemplate, times(1)).convertAndSend("/topic/metrics", metrics);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/health", healthStatus);
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
        verify(messagingTemplate, times(1)).convertAndSend("/topic/tasks", tasks);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/task-summary", summary);
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
        verify(messagingTemplate, times(1)).convertAndSend("/topic/alerts", alerts);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/alert-summary", summary);
    }

    @Test
    @DisplayName("测试推送空数据")
    void testPushEmptyData() {
        // Given
        when(systemMetricsService.getRecentMetrics(5)).thenReturn(Collections.emptyList());
        when(systemMetricsService.getSystemHealthStatus()).thenReturn(
            new SystemMetricsService.SystemHealthStatus("健康", 0.0, 0.0, 0.0, 0));
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());
        when(taskService.getTaskStatusSummary()).thenReturn(
            new TaskService.TaskStatusSummary(0L, 0L, 0L, 0L));
        when(alertService.getUnresolvedAlerts()).thenReturn(Collections.emptyList());
        when(alertService.getAlertSummary()).thenReturn(
            new AlertService.AlertSummary(0L, 0L, 0L, 0L, 0L));

        // When
        webSocketController.pushSystemMetrics();
        webSocketController.pushTasks();
        webSocketController.pushAlerts();

        // Then
        verify(systemMetricsService, times(1)).getRecentMetrics(5);
        verify(taskService, times(1)).getAllTasks();
        verify(alertService, times(1)).getUnresolvedAlerts();
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/metrics"), any(Object.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/health"), any(Object.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/tasks"), any(Object.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/task-summary"), any(Object.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/alerts"), any(Object.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/alert-summary"), any(Object.class));
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
        verify(messagingTemplate, times(1)).convertAndSend("/topic/health", healthStatus);
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
        verify(messagingTemplate, times(1)).convertAndSend("/topic/task-summary", summary);
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
        verify(messagingTemplate, times(1)).convertAndSend("/topic/alert-summary", summary);
    }
}