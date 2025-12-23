package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.Alert;
import com.bub6le.systemmonitoring.repository.AlertRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private List<Alert> mockAlertsList;
    private Alert testAlert;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testAlert = new Alert("server-01", Alert.AlertSeverity.HIGH, "CPU使用率过高");
        testAlert.setId(1L);
        testAlert.setResolved(false);

        mockAlertsList = Arrays.asList(
            testAlert,
            new Alert("server-02", Alert.AlertSeverity.MEDIUM, "内存使用率过高"),
            new Alert("server-03", Alert.AlertSeverity.LOW, "磁盘空间不足")
        );
        
        // 设置第二个告警的属性
        mockAlertsList.get(1).setId(2L);
        mockAlertsList.get(1).setResolved(false);
        
        // 设置第三个告警的属性
        mockAlertsList.get(2).setId(3L);
        mockAlertsList.get(2).setResolved(true);
    }

    @Test
    @DisplayName("测试获取所有告警")
    void testGetAllAlerts() {
        // Given
        when(alertRepository.findAllOrderByTimestamp()).thenReturn(mockAlertsList);

        // When
        List<Alert> result = alertService.getAllAlerts();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("server-01", result.get(0).getSource());
        assertEquals("server-02", result.get(1).getSource());
        assertEquals("server-03", result.get(2).getSource());
        verify(alertRepository, times(1)).findAllOrderByTimestamp();
    }

    @Test
    @DisplayName("测试获取未解决的告警")
    void testGetUnresolvedAlerts() {
        // Given
        List<Alert> unresolvedAlerts = Arrays.asList(testAlert, mockAlertsList.get(1));
        when(alertRepository.findUnresolvedAlerts()).thenReturn(unresolvedAlerts);

        // When
        List<Alert> result = alertService.getUnresolvedAlerts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(alert -> assertFalse(alert.getResolved()));
        verify(alertRepository, times(1)).findUnresolvedAlerts();
    }

    @Test
    @DisplayName("测试根据来源获取告警")
    void testGetAlertsBySource() {
        // Given
        String source = "server-01";
        List<Alert> sourceAlerts = Arrays.asList(testAlert);
        when(alertRepository.findBySource(source)).thenReturn(sourceAlerts);

        // When
        List<Alert> result = alertService.getAlertsBySource(source);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(source, result.get(0).getSource());
        verify(alertRepository, times(1)).findBySource(source);
    }

    @Test
    @DisplayName("测试根据严重程度获取告警")
    void testGetAlertsBySeverity() {
        // Given
        Alert.AlertSeverity severity = Alert.AlertSeverity.HIGH;
        List<Alert> severityAlerts = Arrays.asList(testAlert);
        when(alertRepository.findBySeverity(severity)).thenReturn(severityAlerts);

        // When
        List<Alert> result = alertService.getAlertsBySeverity(severity);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(severity, result.get(0).getSeverity());
        verify(alertRepository, times(1)).findBySeverity(severity);
    }

    @Test
    @DisplayName("测试保存告警")
    void testSaveAlert() {
        // Given
        Alert newAlert = new Alert("database-01", Alert.AlertSeverity.CRITICAL, "数据库连接失败");
        when(alertRepository.save(newAlert)).thenReturn(newAlert);

        // When
        Alert result = alertService.saveAlert(newAlert);

        // Then
        assertNotNull(result);
        assertEquals("database-01", result.getSource());
        assertEquals(Alert.AlertSeverity.CRITICAL, result.getSeverity());
        assertEquals("数据库连接失败", result.getMessage());
        verify(alertRepository, times(1)).save(newAlert);
    }

    @Test
    @DisplayName("测试创建告警")
    void testCreateAlert() {
        // Given
        String source = "cache-cluster";
        Alert.AlertSeverity severity = Alert.AlertSeverity.MEDIUM;
        String message = "缓存命中率下降";
        Alert createdAlert = new Alert(source, severity, message);
        when(alertRepository.save(any(Alert.class))).thenReturn(createdAlert);

        // When
        Alert result = alertService.createAlert(source, severity, message);

        // Then
        assertNotNull(result);
        assertEquals(source, result.getSource());
        assertEquals(severity, result.getSeverity());
        assertEquals(message, result.getMessage());
        assertFalse(result.getResolved());
        assertNotNull(result.getTimestamp());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("测试解决告警")
    void testResolveAlert() {
        // Given
        Long alertId = 1L;
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // When
        alertService.resolveAlert(alertId);

        // Then
        assertTrue(testAlert.getResolved());
        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, times(1)).save(testAlert);
    }

    @Test
    @DisplayName("测试解决告警 - 告警不存在")
    void testResolveAlertNotFound() {
        // Given
        Long alertId = 999L;
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        // When
        alertService.resolveAlert(alertId);

        // Then
        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    @DisplayName("测试生成模拟告警")
    void testGenerateMockAlert() {
        // Given
        Alert savedAlert = new Alert();
        when(alertRepository.save(any(Alert.class))).thenReturn(savedAlert);

        // When
        Alert result = alertService.generateMockAlert();

        // Then
        assertNotNull(result);
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("测试获取告警摘要")
    void testGetAlertSummary() {
        // Given
        List<Alert> unresolvedAlerts = Arrays.asList(
            new Alert("source1", Alert.AlertSeverity.LOW, "低严重性告警"),
            new Alert("source2", Alert.AlertSeverity.MEDIUM, "中严重性告警"),
            new Alert("source3", Alert.AlertSeverity.HIGH, "高严重性告警"),
            new Alert("source4", Alert.AlertSeverity.CRITICAL, "严重告警"),
            new Alert("source5", Alert.AlertSeverity.LOW, "另一个低严重性告警")
        );
        
        when(alertRepository.countUnresolvedAlerts()).thenReturn(5L);
        when(alertRepository.findUnresolvedAlerts()).thenReturn(unresolvedAlerts);

        // When
        AlertService.AlertSummary result = alertService.getAlertSummary();

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getUnresolvedCount());
        assertEquals(2L, result.getLowCount());
        assertEquals(1L, result.getMediumCount());
        assertEquals(1L, result.getHighCount());
        assertEquals(1L, result.getCriticalCount());

        verify(alertRepository, times(1)).countUnresolvedAlerts();
        verify(alertRepository, times(1)).findUnresolvedAlerts();
    }

    @Test
    @DisplayName("测试AlertSummary类")
    void testAlertSummaryClass() {
        // Given
        long unresolvedCount = 10L;
        long lowCount = 4L;
        long mediumCount = 3L;
        long highCount = 2L;
        long criticalCount = 1L;

        // When
        AlertService.AlertSummary summary = 
            new AlertService.AlertSummary(unresolvedCount, lowCount, mediumCount, highCount, criticalCount);

        // Then
        assertEquals(unresolvedCount, summary.getUnresolvedCount());
        assertEquals(lowCount, summary.getLowCount());
        assertEquals(mediumCount, summary.getMediumCount());
        assertEquals(highCount, summary.getHighCount());
        assertEquals(criticalCount, summary.getCriticalCount());
    }

    @Test
    @DisplayName("测试边界情况 - 空列表处理")
    void testEmptyListHandling() {
        // Given
        when(alertRepository.findAllOrderByTimestamp()).thenReturn(Collections.emptyList());
        when(alertRepository.findUnresolvedAlerts()).thenReturn(Collections.emptyList());
        when(alertRepository.findBySource(anyString())).thenReturn(Collections.emptyList());
        when(alertRepository.findBySeverity(any())).thenReturn(Collections.emptyList());

        // When & Then
        assertTrue(alertService.getAllAlerts().isEmpty());
        assertTrue(alertService.getUnresolvedAlerts().isEmpty());
        assertTrue(alertService.getAlertsBySource("non-existent").isEmpty());
        assertTrue(alertService.getAlertsBySeverity(Alert.AlertSeverity.LOW).isEmpty());

        verify(alertRepository, times(1)).findAllOrderByTimestamp();
        verify(alertRepository, times(1)).findUnresolvedAlerts();
        verify(alertRepository, times(1)).findBySource("non-existent");
        verify(alertRepository, times(1)).findBySeverity(Alert.AlertSeverity.LOW);
    }

    @Test
    @DisplayName("测试所有告警严重程度")
    void testAllAlertSeverities() {
        // Given
        when(alertRepository.findBySeverity(Alert.AlertSeverity.LOW)).thenReturn(Collections.emptyList());
        when(alertRepository.findBySeverity(Alert.AlertSeverity.MEDIUM)).thenReturn(Collections.emptyList());
        when(alertRepository.findBySeverity(Alert.AlertSeverity.HIGH)).thenReturn(Collections.emptyList());
        when(alertRepository.findBySeverity(Alert.AlertSeverity.CRITICAL)).thenReturn(Collections.emptyList());

        // When & Then
        assertTrue(alertService.getAlertsBySeverity(Alert.AlertSeverity.LOW).isEmpty());
        assertTrue(alertService.getAlertsBySeverity(Alert.AlertSeverity.MEDIUM).isEmpty());
        assertTrue(alertService.getAlertsBySeverity(Alert.AlertSeverity.HIGH).isEmpty());
        assertTrue(alertService.getAlertsBySeverity(Alert.AlertSeverity.CRITICAL).isEmpty());

        verify(alertRepository, times(1)).findBySeverity(Alert.AlertSeverity.LOW);
        verify(alertRepository, times(1)).findBySeverity(Alert.AlertSeverity.MEDIUM);
        verify(alertRepository, times(1)).findBySeverity(Alert.AlertSeverity.HIGH);
        verify(alertRepository, times(1)).findBySeverity(Alert.AlertSeverity.CRITICAL);
    }

    @Test
    @DisplayName("测试告警摘要 - 无未解决告警")
    void testAlertSummaryNoUnresolved() {
        // Given
        when(alertRepository.countUnresolvedAlerts()).thenReturn(0L);
        when(alertRepository.findUnresolvedAlerts()).thenReturn(Collections.emptyList());

        // When
        AlertService.AlertSummary result = alertService.getAlertSummary();

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getUnresolvedCount());
        assertEquals(0L, result.getLowCount());
        assertEquals(0L, result.getMediumCount());
        assertEquals(0L, result.getHighCount());
        assertEquals(0L, result.getCriticalCount());

        verify(alertRepository, times(1)).countUnresolvedAlerts();
        verify(alertRepository, times(1)).findUnresolvedAlerts();
    }

    @Test
    @DisplayName("测试不同来源的告警")
    void testDifferentAlertSources() {
        // Given
        String[] sources = {
            "server-01", "server-02", "database-01", "cache-cluster", 
            "api-gateway", "load-balancer", "monitoring-system"
        };

        for (String source : sources) {
            when(alertRepository.findBySource(source)).thenReturn(Collections.emptyList());
        }

        // When & Then
        for (String source : sources) {
            assertTrue(alertService.getAlertsBySource(source).isEmpty());
            verify(alertRepository, times(1)).findBySource(source);
        }
    }

    @Test
    @DisplayName("测试告警消息内容")
    void testAlertMessageContent() {
        // Given
        String source = "test-server";
        Alert.AlertSeverity severity = Alert.AlertSeverity.HIGH;
        String message = "CPU使用率达到95%，需要立即处理";
        Alert createdAlert = new Alert(source, severity, message);
        when(alertRepository.save(any(Alert.class))).thenReturn(createdAlert);

        // When
        Alert result = alertService.createAlert(source, severity, message);

        // Then
        assertEquals(message, result.getMessage());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }
}