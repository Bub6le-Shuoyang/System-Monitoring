package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.SystemMetrics;
import com.bub6le.systemmonitoring.repository.SystemMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemMetricsServiceTest {

    @Mock
    private SystemMetricsRepository systemMetricsRepository;

    @InjectMocks
    private SystemMetricsService systemMetricsService;

    private List<SystemMetrics> mockMetricsList;
    private SystemMetrics testMetric;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testMetric = new SystemMetrics(
            "server-01", 75.5, 60.2, 45.8, 500.0, 250.0, 2.5, "北京", "Web服务"
        );
        testMetric.setId(1L);

        mockMetricsList = Arrays.asList(
            testMetric,
            new SystemMetrics("server-02", 80.0, 70.0, 50.0, 600.0, 300.0, 3.0, "上海", "数据库"),
            new SystemMetrics("server-03", 65.5, 55.0, 40.0, 400.0, 200.0, 2.0, "深圳", "缓存")
        );
    }

    @Test
    @DisplayName("测试获取所有系统指标")
    void testGetAllMetrics() {
        // Given
        when(systemMetricsRepository.findAll()).thenReturn(mockMetricsList);

        // When
        List<SystemMetrics> result = systemMetricsService.getAllMetrics();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("server-01", result.get(0).getServerName());
        assertEquals("server-02", result.get(1).getServerName());
        assertEquals("server-03", result.get(2).getServerName());
        verify(systemMetricsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试获取最近的系统指标")
    void testGetRecentMetrics() {
        // Given
        int minutes = 5;
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        when(systemMetricsRepository.findRecentMetrics(any(LocalDateTime.class))).thenReturn(mockMetricsList);

        // When
        List<SystemMetrics> result = systemMetricsService.getRecentMetrics(minutes);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(systemMetricsRepository, times(1)).findRecentMetrics(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试根据服务器名称获取系统指标")
    void testGetMetricsByServer() {
        // Given
        String serverName = "server-01";
        List<SystemMetrics> serverMetrics = Arrays.asList(testMetric);
        when(systemMetricsRepository.findByServerName(serverName)).thenReturn(serverMetrics);

        // When
        List<SystemMetrics> result = systemMetricsService.getMetricsByServer(serverName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(serverName, result.get(0).getServerName());
        verify(systemMetricsRepository, times(1)).findByServerName(serverName);
    }

    @Test
    @DisplayName("测试根据区域获取系统指标")
    void testGetMetricsByRegion() {
        // Given
        String region = "北京";
        List<SystemMetrics> regionMetrics = Arrays.asList(testMetric);
        when(systemMetricsRepository.findByRegion(region)).thenReturn(regionMetrics);

        // When
        List<SystemMetrics> result = systemMetricsService.getMetricsByRegion(region);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(region, result.get(0).getRegion());
        verify(systemMetricsRepository, times(1)).findByRegion(region);
    }

    @Test
    @DisplayName("测试根据服务类型获取系统指标")
    void testGetMetricsByServiceType() {
        // Given
        String serviceType = "Web服务";
        List<SystemMetrics> serviceMetrics = Arrays.asList(testMetric);
        when(systemMetricsRepository.findByServiceType(serviceType)).thenReturn(serviceMetrics);

        // When
        List<SystemMetrics> result = systemMetricsService.getMetricsByServiceType(serviceType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(serviceType, result.get(0).getServiceType());
        verify(systemMetricsRepository, times(1)).findByServiceType(serviceType);
    }

    @Test
    @DisplayName("测试保存系统指标")
    void testSaveMetrics() {
        // Given
        SystemMetrics newMetric = new SystemMetrics(
            "server-04", 85.0, 75.0, 55.0, 700.0, 350.0, 4.0, "成都", "API网关"
        );
        when(systemMetricsRepository.save(newMetric)).thenReturn(newMetric);

        // When
        SystemMetrics result = systemMetricsService.saveMetrics(newMetric);

        // Then
        assertNotNull(result);
        assertEquals("server-04", result.getServerName());
        assertEquals(85.0, result.getCpuUsage());
        verify(systemMetricsRepository, times(1)).save(newMetric);
    }

    @Test
    @DisplayName("测试生成模拟系统指标")
    void testGenerateMockMetrics() {
        // Given
        SystemMetrics savedMetric = new SystemMetrics();
        when(systemMetricsRepository.save(any(SystemMetrics.class))).thenReturn(savedMetric);

        // When
        SystemMetrics result = systemMetricsService.generateMockMetrics();

        // Then
        assertNotNull(result);
        assertNotNull(result.getServerName());
        assertNotNull(result.getRegion());
        assertNotNull(result.getServiceType());
        assertNotNull(result.getCpuUsage());
        assertNotNull(result.getMemoryUsage());
        assertNotNull(result.getDiskUsage());
        assertNotNull(result.getNetworkIn());
        assertNotNull(result.getNetworkOut());
        assertNotNull(result.getLoadAverage());
        assertNotNull(result.getTimestamp());

        // 验证数值范围
        assertTrue(result.getCpuUsage() >= 20.0 && result.getCpuUsage() <= 80.0);
        assertTrue(result.getMemoryUsage() >= 30.0 && result.getMemoryUsage() <= 80.0);
        assertTrue(result.getDiskUsage() >= 10.0 && result.getDiskUsage() <= 50.0);
        assertTrue(result.getNetworkIn() >= 100.0 && result.getNetworkIn() <= 1000.0);
        assertTrue(result.getNetworkOut() >= 50.0 && result.getNetworkOut() <= 500.0);
        assertTrue(result.getLoadAverage() >= 0.0 && result.getLoadAverage() <= 8.0);

        verify(systemMetricsRepository, times(1)).save(any(SystemMetrics.class));
    }

    @Test
    @DisplayName("测试获取系统健康状态 - 无数据")
    void testGetSystemHealthStatusNoData() {
        // Given
        when(systemMetricsRepository.findLatestMetrics(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // When
        SystemMetricsService.SystemHealthStatus result = systemMetricsService.getSystemHealthStatus();

        // Then
        assertNotNull(result);
        assertEquals("未知", result.getStatus());
        assertEquals(0.0, result.getAvgCpu());
        assertEquals(0.0, result.getAvgMemory());
        assertEquals(0.0, result.getAvgLoad());
        assertEquals(0, result.getServerCount());
        verify(systemMetricsRepository, times(1)).findLatestMetrics(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试获取系统健康状态 - 健康状态")
    void testGetSystemHealthStatusHealthy() {
        // Given
        List<SystemMetrics> healthyMetrics = Arrays.asList(
            new SystemMetrics("server-01", 50.0, 40.0, 30.0, 200.0, 100.0, 1.0, "北京", "Web服务"),
            new SystemMetrics("server-02", 60.0, 50.0, 35.0, 250.0, 120.0, 1.5, "上海", "数据库")
        );
        when(systemMetricsRepository.findLatestMetrics(any(LocalDateTime.class))).thenReturn(healthyMetrics);

        // When
        SystemMetricsService.SystemHealthStatus result = systemMetricsService.getSystemHealthStatus();

        // Then
        assertNotNull(result);
        assertEquals("健康", result.getStatus());
        assertEquals(55.0, result.getAvgCpu(), 0.01);
        assertEquals(45.0, result.getAvgMemory(), 0.01);
        assertEquals(1.25, result.getAvgLoad(), 0.01);
        assertEquals(2, result.getServerCount());
        verify(systemMetricsRepository, times(1)).findLatestMetrics(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试获取系统健康状态 - 警告状态")
    void testGetSystemHealthStatusWarning() {
        // Given
        List<SystemMetrics> warningMetrics = Arrays.asList(
            new SystemMetrics("server-01", 75.0, 60.0, 40.0, 300.0, 150.0, 2.5, "北京", "Web服务"),
            new SystemMetrics("server-02", 80.0, 78.0, 45.0, 350.0, 180.0, 3.2, "上海", "数据库")
        );
        when(systemMetricsRepository.findLatestMetrics(any(LocalDateTime.class))).thenReturn(warningMetrics);

        // When
        SystemMetricsService.SystemHealthStatus result = systemMetricsService.getSystemHealthStatus();

        // Then
        assertNotNull(result);
        assertEquals("警告", result.getStatus());
        assertEquals(77.5, result.getAvgCpu(), 0.01);
        assertEquals(69.0, result.getAvgMemory(), 0.01);
        assertEquals(2.85, result.getAvgLoad(), 0.01);
        assertEquals(2, result.getServerCount());
        verify(systemMetricsRepository, times(1)).findLatestMetrics(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试获取系统健康状态 - 不健康状态")
    void testGetSystemHealthStatusUnhealthy() {
        // Given
        List<SystemMetrics> unhealthyMetrics = Arrays.asList(
            new SystemMetrics("server-01", 90.0, 85.0, 50.0, 400.0, 200.0, 4.5, "北京", "Web服务"),
            new SystemMetrics("server-02", 88.0, 92.0, 55.0, 450.0, 220.0, 5.5, "上海", "数据库")
        );
        when(systemMetricsRepository.findLatestMetrics(any(LocalDateTime.class))).thenReturn(unhealthyMetrics);

        // When
        SystemMetricsService.SystemHealthStatus result = systemMetricsService.getSystemHealthStatus();

        // Then
        assertNotNull(result);
        assertEquals("不健康", result.getStatus());
        assertEquals(89.0, result.getAvgCpu(), 0.01);
        assertEquals(88.5, result.getAvgMemory(), 0.01);
        assertEquals(5.0, result.getAvgLoad(), 0.01);
        assertEquals(2, result.getServerCount());
        verify(systemMetricsRepository, times(1)).findLatestMetrics(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试SystemHealthStatus类")
    void testSystemHealthStatusClass() {
        // Given
        String status = "健康";
        double avgCpu = 65.5;
        double avgMemory = 55.2;
        double avgLoad = 2.1;
        int serverCount = 3;

        // When
        SystemMetricsService.SystemHealthStatus healthStatus = 
            new SystemMetricsService.SystemHealthStatus(status, avgCpu, avgMemory, avgLoad, serverCount);

        // Then
        assertEquals(status, healthStatus.getStatus());
        assertEquals(avgCpu, healthStatus.getAvgCpu(), 0.01);
        assertEquals(avgMemory, healthStatus.getAvgMemory(), 0.01);
        assertEquals(avgLoad, healthStatus.getAvgLoad(), 0.01);
        assertEquals(serverCount, healthStatus.getServerCount());
    }

    @Test
    @DisplayName("测试边界情况 - 空列表处理")
    void testEmptyListHandling() {
        // Given
        when(systemMetricsRepository.findAll()).thenReturn(Collections.emptyList());
        when(systemMetricsRepository.findByServerName(anyString())).thenReturn(Collections.emptyList());
        when(systemMetricsRepository.findByRegion(anyString())).thenReturn(Collections.emptyList());
        when(systemMetricsRepository.findByServiceType(anyString())).thenReturn(Collections.emptyList());

        // When & Then
        assertTrue(systemMetricsService.getAllMetrics().isEmpty());
        assertTrue(systemMetricsService.getMetricsByServer("non-existent").isEmpty());
        assertTrue(systemMetricsService.getMetricsByRegion("non-existent").isEmpty());
        assertTrue(systemMetricsService.getMetricsByServiceType("non-existent").isEmpty());

        verify(systemMetricsRepository, times(1)).findAll();
        verify(systemMetricsRepository, times(1)).findByServerName("non-existent");
        verify(systemMetricsRepository, times(1)).findByRegion("non-existent");
        verify(systemMetricsRepository, times(1)).findByServiceType("non-existent");
    }
}