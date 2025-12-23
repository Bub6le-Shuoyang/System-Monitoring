package com.bub6le.systemmonitoring.repository;

import com.bub6le.systemmonitoring.model.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlertRepositoryTest {

    @Autowired
    private AlertRepository alertRepository;

    private Alert testAlert1;
    private Alert testAlert2;
    private Alert testAlert3;
    private Alert testAlert4;
    private Alert testAlert5;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2023, 12, 23, 10, 0, 0);
        
        // 创建测试数据
        testAlert1 = new Alert("server-01", Alert.AlertSeverity.LOW, "CPU使用率略高");
        testAlert1.setId(1L);
        testAlert1.setTimestamp(baseTime);
        testAlert1.setResolved(false);

        testAlert2 = new Alert("server-02", Alert.AlertSeverity.MEDIUM, "内存使用率过高");
        testAlert2.setId(2L);
        testAlert2.setTimestamp(baseTime.plusMinutes(1));
        testAlert2.setResolved(false);

        testAlert3 = new Alert("server-03", Alert.AlertSeverity.HIGH, "磁盘空间即将耗尽");
        testAlert3.setId(3L);
        testAlert3.setTimestamp(baseTime.plusMinutes(2));
        testAlert3.setResolved(false);

        testAlert4 = new Alert("server-04", Alert.AlertSeverity.CRITICAL, "系统崩溃");
        testAlert4.setId(4L);
        testAlert4.setTimestamp(baseTime.plusMinutes(3));
        testAlert4.setResolved(false);

        testAlert5 = new Alert("server-05", Alert.AlertSeverity.LOW, "网络延迟增加");
        testAlert5.setId(5L);
        testAlert5.setTimestamp(baseTime.plusMinutes(4));
        testAlert5.setResolved(true);
    }

    @Test
    @DisplayName("测试根据来源查找告警")
    void testFindBySource() {
        // Given
        alertRepository.save(testAlert1);
        alertRepository.save(testAlert2);
        alertRepository.save(testAlert3);

        // 创建另一个相同来源的告警
        Alert anotherServer01Alert = new Alert("server-01", Alert.AlertSeverity.MEDIUM, "另一个告警");
        anotherServer01Alert.setTimestamp(baseTime.plusMinutes(5));
        alertRepository.save(anotherServer01Alert);

        // When
        List<Alert> server01Alerts = alertRepository.findBySource("server-01");
        List<Alert> server02Alerts = alertRepository.findBySource("server-02");

        // Then
        assertEquals(2, server01Alerts.size());
        server01Alerts.forEach(alert -> assertEquals("server-01", alert.getSource()));

        assertEquals(1, server02Alerts.size());
        assertEquals("server-02", server02Alerts.get(0).getSource());
    }

    @Test
    @DisplayName("测试根据严重程度查找告警")
    void testFindBySeverity() {
        // Given
        alertRepository.save(testAlert1); // LOW
        alertRepository.save(testAlert2); // MEDIUM
        alertRepository.save(testAlert3); // HIGH
        alertRepository.save(testAlert4); // CRITICAL

        // 创建另一个低严重性告警
        Alert anotherLowAlert = new Alert("server-06", Alert.AlertSeverity.LOW, "另一个低严重性告警");
        anotherLowAlert.setTimestamp(baseTime.plusMinutes(5));
        alertRepository.save(anotherLowAlert);

        // When
        List<Alert> lowAlerts = alertRepository.findBySeverity(Alert.AlertSeverity.LOW);
        List<Alert> mediumAlerts = alertRepository.findBySeverity(Alert.AlertSeverity.MEDIUM);
        List<Alert> highAlerts = alertRepository.findBySeverity(Alert.AlertSeverity.HIGH);
        List<Alert> criticalAlerts = alertRepository.findBySeverity(Alert.AlertSeverity.CRITICAL);

        // Then
        assertEquals(2, lowAlerts.size());
        lowAlerts.forEach(alert -> assertEquals(Alert.AlertSeverity.LOW, alert.getSeverity()));

        assertEquals(1, mediumAlerts.size());
        assertEquals(Alert.AlertSeverity.MEDIUM, mediumAlerts.get(0).getSeverity());

        assertEquals(1, highAlerts.size());
        assertEquals(Alert.AlertSeverity.HIGH, highAlerts.get(0).getSeverity());

        assertEquals(1, criticalAlerts.size());
        assertEquals(Alert.AlertSeverity.CRITICAL, criticalAlerts.get(0).getSeverity());
    }

    @Test
    @DisplayName("测试根据解决状态查找告警")
    void testFindByResolved() {
        // Given
        alertRepository.save(testAlert1); // unresolved
        alertRepository.save(testAlert2); // unresolved
        alertRepository.save(testAlert5); // resolved

        // 创建另一个已解决的告警
        Alert anotherResolvedAlert = new Alert("server-06", Alert.AlertSeverity.MEDIUM, "另一个已解决告警");
        anotherResolvedAlert.setResolved(true);
        anotherResolvedAlert.setTimestamp(baseTime.plusMinutes(5));
        alertRepository.save(anotherResolvedAlert);

        // When
        List<Alert> unresolvedAlerts = alertRepository.findByResolved(false);
        List<Alert> resolvedAlerts = alertRepository.findByResolved(true);

        // Then
        assertEquals(2, unresolvedAlerts.size());
        unresolvedAlerts.forEach(alert -> assertFalse(alert.getResolved()));

        assertEquals(2, resolvedAlerts.size());
        resolvedAlerts.forEach(alert -> assertTrue(alert.getResolved()));
    }

    @Test
    @DisplayName("测试查找所有告警按时间戳排序")
    void testFindAllOrderByTimestamp() {
        // Given
        alertRepository.save(testAlert1);
        alertRepository.save(testAlert2);
        alertRepository.save(testAlert3);
        alertRepository.save(testAlert4);
        alertRepository.save(testAlert5);

        // When
        List<Alert> result = alertRepository.findAllOrderByTimestamp();

        // Then
        assertEquals(5, result.size());
        // 验证按时间戳降序排列
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
        assertTrue(result.get(1).getTimestamp().isAfter(result.get(2).getTimestamp()));
        assertTrue(result.get(2).getTimestamp().isAfter(result.get(3).getTimestamp()));
        assertTrue(result.get(3).getTimestamp().isAfter(result.get(4).getTimestamp()));
    }

    @Test
    @DisplayName("测试查找未解决的告警")
    void testFindUnresolvedAlerts() {
        // Given
        alertRepository.save(testAlert1); // unresolved
        alertRepository.save(testAlert2); // unresolved
        alertRepository.save(testAlert3); // unresolved
        alertRepository.save(testAlert4); // unresolved
        alertRepository.save(testAlert5); // resolved

        // When
        List<Alert> result = alertRepository.findUnresolvedAlerts();

        // Then
        assertEquals(4, result.size());
        result.forEach(alert -> assertFalse(alert.getResolved()));
        // 验证按时间戳降序排列
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
    }

    @Test
    @DisplayName("测试根据严重程度查找未解决的告警")
    void testFindUnresolvedAlertsBySeverity() {
        // Given
        alertRepository.save(testAlert1); // LOW, unresolved
        alertRepository.save(testAlert2); // MEDIUM, unresolved
        alertRepository.save(testAlert3); // HIGH, unresolved
        alertRepository.save(testAlert4); // CRITICAL, unresolved
        alertRepository.save(testAlert5); // LOW, resolved

        // 创建另一个未解决的中等严重性告警
        Alert anotherMediumAlert = new Alert("server-06", Alert.AlertSeverity.MEDIUM, "另一个中等告警");
        anotherMediumAlert.setResolved(false);
        anotherMediumAlert.setTimestamp(baseTime.plusMinutes(5));
        alertRepository.save(anotherMediumAlert);

        // When
        List<Alert> lowUnresolved = alertRepository.findUnresolvedAlertsBySeverity(Alert.AlertSeverity.LOW);
        List<Alert> mediumUnresolved = alertRepository.findUnresolvedAlertsBySeverity(Alert.AlertSeverity.MEDIUM);
        List<Alert> highUnresolved = alertRepository.findUnresolvedAlertsBySeverity(Alert.AlertSeverity.HIGH);
        List<Alert> criticalUnresolved = alertRepository.findUnresolvedAlertsBySeverity(Alert.AlertSeverity.CRITICAL);

        // Then
        assertEquals(1, lowUnresolved.size());
        lowUnresolved.forEach(alert -> {
            assertEquals(Alert.AlertSeverity.LOW, alert.getSeverity());
            assertFalse(alert.getResolved());
        });

        assertEquals(2, mediumUnresolved.size());
        mediumUnresolved.forEach(alert -> {
            assertEquals(Alert.AlertSeverity.MEDIUM, alert.getSeverity());
            assertFalse(alert.getResolved());
        });

        assertEquals(1, highUnresolved.size());
        highUnresolved.forEach(alert -> {
            assertEquals(Alert.AlertSeverity.HIGH, alert.getSeverity());
            assertFalse(alert.getResolved());
        });

        assertEquals(1, criticalUnresolved.size());
        criticalUnresolved.forEach(alert -> {
            assertEquals(Alert.AlertSeverity.CRITICAL, alert.getSeverity());
            assertFalse(alert.getResolved());
        });
    }

    @Test
    @DisplayName("测试统计未解决告警数量")
    void testCountUnresolvedAlerts() {
        // Given
        alertRepository.save(testAlert1); // unresolved
        alertRepository.save(testAlert2); // unresolved
        alertRepository.save(testAlert3); // unresolved
        alertRepository.save(testAlert4); // unresolved
        alertRepository.save(testAlert5); // resolved

        // When
        Long result = alertRepository.countUnresolvedAlerts();

        // Then
        assertEquals(Long.valueOf(4), result);
    }

    @Test
    @DisplayName("测试查找不存在的来源")
    void testFindByNonExistentSource() {
        // Given
        alertRepository.save(testAlert1);
        alertRepository.save(testAlert2);

        // When
        List<Alert> result = alertRepository.findBySource("non-existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试查找不存在的严重程度")
    void testFindByNonExistentSeverity() {
        // Given
        alertRepository.save(testAlert1);
        alertRepository.save(testAlert2);

        // When
        List<Alert> result = alertRepository.findBySeverity(Alert.AlertSeverity.CRITICAL);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试统计未解决告警数量 - 无未解决告警")
    void testCountUnresolvedAlertsWithNoUnresolved() {
        // Given
        testAlert1.setResolved(true);
        testAlert2.setResolved(true);
        alertRepository.save(testAlert1);
        alertRepository.save(testAlert2);

        // When
        Long result = alertRepository.countUnresolvedAlerts();

        // Then
        assertEquals(Long.valueOf(0), result);
    }

    @Test
    @DisplayName("测试保存和检索告警")
    void testSaveAndRetrieveAlert() {
        // When
        Alert savedAlert = alertRepository.save(testAlert1);
        Alert retrievedAlert = alertRepository.findById(savedAlert.getId()).orElse(null);

        // Then
        assertNotNull(retrievedAlert);
        assertEquals(testAlert1.getSource(), retrievedAlert.getSource());
        assertEquals(testAlert1.getSeverity(), retrievedAlert.getSeverity());
        assertEquals(testAlert1.getMessage(), retrievedAlert.getMessage());
        assertEquals(testAlert1.getTimestamp(), retrievedAlert.getTimestamp());
        assertEquals(testAlert1.getResolved(), retrievedAlert.getResolved());
    }

    @Test
    @DisplayName("测试删除告警")
    void testDeleteAlert() {
        // Given
        Alert savedAlert = alertRepository.save(testAlert1);
        Long id = savedAlert.getId();

        // When
        alertRepository.deleteById(id);
        Alert deletedAlert = alertRepository.findById(id).orElse(null);

        // Then
        assertNull(deletedAlert);
    }

    @Test
    @DisplayName("测试更新告警")
    void testUpdateAlert() {
        // Given
        Alert savedAlert = alertRepository.save(testAlert1);
        savedAlert.setResolved(true);
        savedAlert.setMessage("更新的消息");

        // When
        Alert updatedAlert = alertRepository.save(savedAlert);

        // Then
        assertTrue(updatedAlert.getResolved());
        assertEquals("更新的消息", updatedAlert.getMessage());
    }

    @Test
    @DisplayName("测试查询结果排序 - 相同时间戳的告警")
    void testQueryResultOrderingWithSameTimestamp() {
        // Given
        Alert alert1 = new Alert("source1", Alert.AlertSeverity.LOW, "消息1");
        alert1.setTimestamp(baseTime);
        
        Alert alert2 = new Alert("source2", Alert.AlertSeverity.MEDIUM, "消息2");
        alert2.setTimestamp(baseTime.plusMinutes(1));
        
        Alert alert3 = new Alert("source3", Alert.AlertSeverity.HIGH, "消息3");
        alert3.setTimestamp(baseTime.plusMinutes(2));
        
        alertRepository.save(alert1);
        alertRepository.save(alert2);
        alertRepository.save(alert3);

        // When
        List<Alert> result = alertRepository.findAllOrderByTimestamp();

        // Then
        assertEquals(3, result.size());
        assertEquals("消息3", result.get(0).getMessage());
        assertEquals("消息2", result.get(1).getMessage());
        assertEquals("消息1", result.get(2).getMessage());
    }

    @Test
    @DisplayName("测试所有告警严重程度")
    void testAllAlertSeverities() {
        // Given
        alertRepository.save(testAlert1); // LOW
        alertRepository.save(testAlert2); // MEDIUM
        alertRepository.save(testAlert3); // HIGH
        alertRepository.save(testAlert4); // CRITICAL

        // When & Then
        assertEquals(1, alertRepository.findBySeverity(Alert.AlertSeverity.LOW).size());
        assertEquals(1, alertRepository.findBySeverity(Alert.AlertSeverity.MEDIUM).size());
        assertEquals(1, alertRepository.findBySeverity(Alert.AlertSeverity.HIGH).size());
        assertEquals(1, alertRepository.findBySeverity(Alert.AlertSeverity.CRITICAL).size());
    }

    @Test
    @DisplayName("测试混合状态的告警查询")
    void testMixedStatusAlertQueries() {
        // Given
        alertRepository.save(testAlert1); // LOW, unresolved
        alertRepository.save(testAlert2); // MEDIUM, unresolved
        alertRepository.save(testAlert3); // HIGH, unresolved
        alertRepository.save(testAlert4); // CRITICAL, unresolved
        alertRepository.save(testAlert5); // LOW, resolved

        // 创建已解决的中等严重性告警
        Alert resolvedMediumAlert = new Alert("server-06", Alert.AlertSeverity.MEDIUM, "已解决的中等告警");
        resolvedMediumAlert.setResolved(true);
        resolvedMediumAlert.setTimestamp(baseTime.plusMinutes(5));
        alertRepository.save(resolvedMediumAlert);

        // When
        List<Alert> allLow = alertRepository.findBySeverity(Alert.AlertSeverity.LOW);
        List<Alert> allMedium = alertRepository.findBySeverity(Alert.AlertSeverity.MEDIUM);
        List<Alert> unresolvedLow = alertRepository.findUnresolvedAlertsBySeverity(Alert.AlertSeverity.LOW);
        List<Alert> unresolvedMedium = alertRepository.findUnresolvedAlertsBySeverity(Alert.AlertSeverity.MEDIUM);

        // Then
        assertEquals(2, allLow.size()); // 1 unresolved + 1 resolved
        assertEquals(2, allMedium.size()); // 1 unresolved + 1 resolved
        assertEquals(1, unresolvedLow.size()); // only unresolved
        assertEquals(1, unresolvedMedium.size()); // only unresolved
    }

    @Test
    @DisplayName("测试空结果集")
    void testEmptyResultSets() {
        // When
        List<Alert> allAlerts = alertRepository.findAllOrderByTimestamp();
        List<Alert> unresolvedAlerts = alertRepository.findUnresolvedAlerts();
        Long unresolvedCount = alertRepository.countUnresolvedAlerts();

        // Then
        assertTrue(allAlerts.isEmpty());
        assertTrue(unresolvedAlerts.isEmpty());
        assertEquals(Long.valueOf(0), unresolvedCount);
    }
}