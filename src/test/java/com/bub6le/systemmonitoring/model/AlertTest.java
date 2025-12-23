package com.bub6le.systemmonitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    private Alert alert;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.of(2023, 12, 23, 10, 30, 0);
        alert = new Alert();
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertNotNull(alert.getTimestamp());
        assertFalse(alert.getResolved());
        assertNull(alert.getId());
        assertNull(alert.getSource());
        assertNull(alert.getSeverity());
        assertNull(alert.getMessage());
    }

    @Test
    @DisplayName("测试参数化构造函数")
    void testParameterizedConstructor() {
        Alert newAlert = new Alert("server-01", Alert.AlertSeverity.HIGH, "CPU使用率过高");

        assertEquals("server-01", newAlert.getSource());
        assertEquals(Alert.AlertSeverity.HIGH, newAlert.getSeverity());
        assertEquals("CPU使用率过高", newAlert.getMessage());
        assertNotNull(newAlert.getTimestamp());
        assertFalse(newAlert.getResolved());
    }

    @Test
    @DisplayName("测试Getter和Setter方法")
    void testGettersAndSetters() {
        // 测试ID
        alert.setId(1L);
        assertEquals(1L, alert.getId());

        // 测试来源
        alert.setSource("database-01");
        assertEquals("database-01", alert.getSource());

        // 测试严重程度
        alert.setSeverity(Alert.AlertSeverity.CRITICAL);
        assertEquals(Alert.AlertSeverity.CRITICAL, alert.getSeverity());

        // 测试消息
        alert.setMessage("内存溢出风险");
        assertEquals("内存溢出风险", alert.getMessage());

        // 测试时间戳
        alert.setTimestamp(testTimestamp);
        assertEquals(testTimestamp, alert.getTimestamp());

        // 测试解决状态
        alert.setResolved(true);
        assertTrue(alert.getResolved());
    }

    @Test
    @DisplayName("测试严重程度枚举")
    void testAlertSeverityEnum() {
        assertEquals("低", Alert.AlertSeverity.LOW.getDescription());
        assertEquals("中", Alert.AlertSeverity.MEDIUM.getDescription());
        assertEquals("高", Alert.AlertSeverity.HIGH.getDescription());
        assertEquals("严重", Alert.AlertSeverity.CRITICAL.getDescription());
    }

    @Test
    @DisplayName("测试所有严重程度值")
    void testAllSeverityValues() {
        Alert[] alerts = new Alert[4];
        
        alerts[0] = new Alert();
        alerts[0].setSeverity(Alert.AlertSeverity.LOW);
        
        alerts[1] = new Alert();
        alerts[1].setSeverity(Alert.AlertSeverity.MEDIUM);
        
        alerts[2] = new Alert();
        alerts[2].setSeverity(Alert.AlertSeverity.HIGH);
        
        alerts[3] = new Alert();
        alerts[3].setSeverity(Alert.AlertSeverity.CRITICAL);

        assertEquals(Alert.AlertSeverity.LOW, alerts[0].getSeverity());
        assertEquals(Alert.AlertSeverity.MEDIUM, alerts[1].getSeverity());
        assertEquals(Alert.AlertSeverity.HIGH, alerts[2].getSeverity());
        assertEquals(Alert.AlertSeverity.CRITICAL, alerts[3].getSeverity());
    }

    @Test
    @DisplayName("测试字符串字段")
    void testStringFields() {
        // 测试空字符串
        alert.setSource("");
        alert.setMessage("");

        assertEquals("", alert.getSource());
        assertEquals("", alert.getMessage());

        // 测试特殊字符
        alert.setSource("server-测试_01");
        alert.setMessage("CPU使用率超过90%！@#$%");

        assertEquals("server-测试_01", alert.getSource());
        assertEquals("CPU使用率超过90%！@#$%", alert.getMessage());

        // 测试中文字符
        alert.setSource("数据库服务器");
        alert.setMessage("内存使用率过高，需要立即处理");

        assertEquals("数据库服务器", alert.getSource());
        assertEquals("内存使用率过高，需要立即处理", alert.getMessage());

        // 测试长消息
        String longMessage = "这是一个非常长的告警消息，用于测试系统是否能够正确处理长文本内容。" +
                           "在实际的生产环境中，告警消息可能包含详细的错误信息、堆栈跟踪、" +
                           "系统状态描述等内容，因此需要确保系统能够正确处理和显示这些长消息。";
        alert.setMessage(longMessage);
        assertEquals(longMessage, alert.getMessage());
    }

    @Test
    @DisplayName("测试时间戳自动设置")
    void testTimestampAutoSet() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        
        Alert newAlert = new Alert("test-source", Alert.AlertSeverity.MEDIUM, "test message");
        
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(newAlert.getTimestamp().isAfter(beforeCreation));
        assertTrue(newAlert.getTimestamp().isBefore(afterCreation));
    }

    @Test
    @DisplayName("测试解决状态")
    void testResolvedStatus() {
        // 默认应该是未解决
        assertFalse(alert.getResolved());

        // 设置为已解决
        alert.setResolved(true);
        assertTrue(alert.getResolved());

        // 设置为未解决
        alert.setResolved(false);
        assertFalse(alert.getResolved());

        // 测试null值处理
        alert.setResolved(null);
        assertNull(alert.getResolved());
    }

    @Test
    @DisplayName("测试不同严重程度的告警创建")
    void testCreateAlertsWithDifferentSeverities() {
        Alert lowAlert = new Alert("server-01", Alert.AlertSeverity.LOW, "CPU使用率略高");
        Alert mediumAlert = new Alert("server-02", Alert.AlertSeverity.MEDIUM, "内存使用率过高");
        Alert highAlert = new Alert("server-03", Alert.AlertSeverity.HIGH, "磁盘空间即将耗尽");
        Alert criticalAlert = new Alert("server-04", Alert.AlertSeverity.CRITICAL, "系统崩溃");

        assertEquals(Alert.AlertSeverity.LOW, lowAlert.getSeverity());
        assertEquals("CPU使用率略高", lowAlert.getMessage());

        assertEquals(Alert.AlertSeverity.MEDIUM, mediumAlert.getSeverity());
        assertEquals("内存使用率过高", mediumAlert.getMessage());

        assertEquals(Alert.AlertSeverity.HIGH, highAlert.getSeverity());
        assertEquals("磁盘空间即将耗尽", highAlert.getMessage());

        assertEquals(Alert.AlertSeverity.CRITICAL, criticalAlert.getSeverity());
        assertEquals("系统崩溃", criticalAlert.getMessage());
    }

    @Test
    @DisplayName("测试告警来源")
    void testAlertSources() {
        String[] sources = {
            "server-01", "server-02", "database-01", "cache-cluster", 
            "api-gateway", "load-balancer", "monitoring-system"
        };

        for (String source : sources) {
            Alert testAlert = new Alert();
            testAlert.setSource(source);
            assertEquals(source, testAlert.getSource());
        }
    }

    @Test
    @DisplayName("测试完整的告警生命周期")
    void testAlertLifecycle() {
        // 创建告警
        Alert alert = new Alert("server-01", Alert.AlertSeverity.HIGH, "CPU使用率过高");
        
        // 验证初始状态
        assertEquals("server-01", alert.getSource());
        assertEquals(Alert.AlertSeverity.HIGH, alert.getSeverity());
        assertEquals("CPU使用率过高", alert.getMessage());
        assertFalse(alert.getResolved());
        assertNotNull(alert.getTimestamp());

        // 模拟告警被解决
        alert.setResolved(true);
        assertTrue(alert.getResolved());

        // 验证其他字段未改变
        assertEquals("server-01", alert.getSource());
        assertEquals(Alert.AlertSeverity.HIGH, alert.getSeverity());
        assertEquals("CPU使用率过高", alert.getMessage());
        assertNotNull(alert.getTimestamp());
    }
}