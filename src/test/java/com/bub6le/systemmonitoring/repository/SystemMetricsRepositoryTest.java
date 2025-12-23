package com.bub6le.systemmonitoring.repository;

import com.bub6le.systemmonitoring.model.SystemMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SystemMetricsRepositoryTest {

    @Autowired
    private SystemMetricsRepository systemMetricsRepository;

    private SystemMetrics testMetric1;
    private SystemMetrics testMetric2;
    private SystemMetrics testMetric3;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        // 清理数据库
        systemMetricsRepository.deleteAll();
        
        baseTime = LocalDateTime.of(2023, 12, 23, 10, 0, 0);
        
        // 创建测试数据
        testMetric1 = new SystemMetrics(
            "server-01", 75.5, 60.2, 45.8, 500.0, 250.0, 2.5, "北京", "Web服务"
        );
        testMetric1.setTimestamp(baseTime);

        testMetric2 = new SystemMetrics(
            "server-02", 80.0, 70.0, 50.0, 600.0, 300.0, 3.0, "上海", "数据库"
        );
        testMetric2.setTimestamp(baseTime.plusMinutes(1));

        testMetric3 = new SystemMetrics(
            "server-01", 85.0, 75.0, 55.0, 700.0, 350.0, 3.5, "北京", "Web服务"
        );
        testMetric3.setTimestamp(baseTime.plusMinutes(2));
    }

    @Test
    @DisplayName("测试根据服务器名称查找系统指标")
    void testFindByServerName() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findByServerName("server-01");

        // Then
        assertEquals(2, result.size());
        result.forEach(metric -> assertEquals("server-01", metric.getServerName()));
    }

    @Test
    @DisplayName("测试根据区域查找系统指标")
    void testFindByRegion() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findByRegion("北京");

        // Then
        assertEquals(2, result.size());
        result.forEach(metric -> assertEquals("北京", metric.getRegion()));
    }

    @Test
    @DisplayName("测试根据服务类型查找系统指标")
    void testFindByServiceType() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findByServiceType("Web服务");

        // Then
        assertEquals(2, result.size());
        result.forEach(metric -> assertEquals("Web服务", metric.getServiceType()));
    }

    @Test
    @DisplayName("测试查找最近的系统指标")
    void testFindRecentMetrics() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findRecentMetrics(baseTime.plusMinutes(1));

        // Then
        assertEquals(2, result.size());
        // 验证结果按时间戳降序排列
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
    }

    @Test
    @DisplayName("测试根据服务器名称和时间查找最近的系统指标")
    void testFindRecentMetricsByServer() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findRecentMetricsByServer(
            "server-01", baseTime.plusMinutes(1));

        // Then
        assertEquals(1, result.size());
        assertEquals("server-01", result.get(0).getServerName());
        assertTrue(result.get(0).getTimestamp().isAfter(baseTime.plusMinutes(1)));
    }

    @Test
    @DisplayName("测试计算平均CPU使用率")
    void testGetAverageCpuUsage() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        Double result = systemMetricsRepository.getAverageCpuUsage(baseTime);

        // Then
        assertNotNull(result);
        assertEquals((75.5 + 80.0 + 85.0) / 3, result, 0.01);
    }

    @Test
    @DisplayName("测试计算平均内存使用率")
    void testGetAverageMemoryUsage() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        Double result = systemMetricsRepository.getAverageMemoryUsage(baseTime);

        // Then
        assertNotNull(result);
        assertEquals((60.2 + 70.0 + 75.0) / 3, result, 0.01);
    }

    @Test
    @DisplayName("测试查找最新的系统指标")
    void testFindLatestMetrics() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findLatestMetrics(baseTime);

        // Then
        assertEquals(3, result.size());
        // 验证结果按时间戳降序排列
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
        assertTrue(result.get(1).getTimestamp().isAfter(result.get(2).getTimestamp()));
    }

    @Test
    @DisplayName("测试查找不存在的服务器名称")
    void testFindByNonExistentServerName() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findByServerName("non-existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试查找不存在的区域")
    void testFindByNonExistentRegion() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findByRegion("non-existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试查找不存在的服务类型")
    void testFindByNonExistentServiceType() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findByServiceType("non-existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试查找时间范围内无数据")
    void testFindRecentMetricsNoData() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findRecentMetrics(baseTime.plusHours(1));

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试计算平均值时无数据")
    void testGetAverageUsageNoData() {
        // Given
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric2);

        // When
        Double cpuResult = systemMetricsRepository.getAverageCpuUsage(baseTime.plusHours(1));
        Double memoryResult = systemMetricsRepository.getAverageMemoryUsage(baseTime.plusHours(1));

        // Then
        assertNull(cpuResult);
        assertNull(memoryResult);
    }

    @Test
    @DisplayName("测试保存和检索系统指标")
    void testSaveAndRetrieveSystemMetrics() {
        // When
        SystemMetrics savedMetric = systemMetricsRepository.save(testMetric1);
        SystemMetrics retrievedMetric = systemMetricsRepository.findById(savedMetric.getId()).orElse(null);

        // Then
        assertNotNull(retrievedMetric);
        assertEquals(testMetric1.getServerName(), retrievedMetric.getServerName());
        assertEquals(testMetric1.getCpuUsage(), retrievedMetric.getCpuUsage());
        assertEquals(testMetric1.getMemoryUsage(), retrievedMetric.getMemoryUsage());
        assertEquals(testMetric1.getDiskUsage(), retrievedMetric.getDiskUsage());
        assertEquals(testMetric1.getNetworkIn(), retrievedMetric.getNetworkIn());
        assertEquals(testMetric1.getNetworkOut(), retrievedMetric.getNetworkOut());
        assertEquals(testMetric1.getLoadAverage(), retrievedMetric.getLoadAverage());
        assertEquals(testMetric1.getRegion(), retrievedMetric.getRegion());
        assertEquals(testMetric1.getServiceType(), retrievedMetric.getServiceType());
    }

    @Test
    @DisplayName("测试删除系统指标")
    void testDeleteSystemMetrics() {
        // Given
        SystemMetrics savedMetric = systemMetricsRepository.save(testMetric1);
        Long id = savedMetric.getId();

        // When
        systemMetricsRepository.deleteById(id);
        SystemMetrics deletedMetric = systemMetricsRepository.findById(id).orElse(null);

        // Then
        assertNull(deletedMetric);
    }

    @Test
    @DisplayName("测试更新系统指标")
    void testUpdateSystemMetrics() {
        // Given
        SystemMetrics savedMetric = systemMetricsRepository.save(testMetric1);
        savedMetric.setCpuUsage(90.0);
        savedMetric.setMemoryUsage(80.0);

        // When
        SystemMetrics updatedMetric = systemMetricsRepository.save(savedMetric);

        // Then
        assertEquals(90.0, updatedMetric.getCpuUsage());
        assertEquals(80.0, updatedMetric.getMemoryUsage());
    }

    @Test
    @DisplayName("测试查询结果排序")
    void testQueryResultOrdering() {
        // Given
        SystemMetrics oldMetric = new SystemMetrics(
            "server-01", 70.0, 60.0, 40.0, 400.0, 200.0, 2.0, "北京", "Web服务"
        );
        oldMetric.setTimestamp(baseTime.minusMinutes(1));
        
        systemMetricsRepository.save(oldMetric);
        systemMetricsRepository.save(testMetric1);
        systemMetricsRepository.save(testMetric3);

        // When
        List<SystemMetrics> result = systemMetricsRepository.findLatestMetrics(baseTime.minusMinutes(2));

        // Then
        assertEquals(3, result.size());
        // 验证按时间戳降序排列
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
        assertTrue(result.get(1).getTimestamp().isAfter(result.get(2).getTimestamp()));
    }
}