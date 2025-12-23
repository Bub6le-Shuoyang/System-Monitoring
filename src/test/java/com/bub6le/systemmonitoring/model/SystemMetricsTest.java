package com.bub6le.systemmonitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SystemMetricsTest {

    private SystemMetrics systemMetrics;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.of(2023, 12, 23, 10, 30, 0);
        systemMetrics = new SystemMetrics();
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertNotNull(systemMetrics.getTimestamp());
        assertNull(systemMetrics.getId());
        assertNull(systemMetrics.getServerName());
        assertNull(systemMetrics.getCpuUsage());
        assertNull(systemMetrics.getMemoryUsage());
        assertNull(systemMetrics.getDiskUsage());
        assertNull(systemMetrics.getNetworkIn());
        assertNull(systemMetrics.getNetworkOut());
        assertNull(systemMetrics.getLoadAverage());
        assertNull(systemMetrics.getRegion());
        assertNull(systemMetrics.getServiceType());
    }

    @Test
    @DisplayName("测试参数化构造函数")
    void testParameterizedConstructor() {
        SystemMetrics metrics = new SystemMetrics(
            "server-01", 75.5, 60.2, 45.8, 500.0, 250.0, 2.5, "北京", "Web服务"
        );

        assertEquals("server-01", metrics.getServerName());
        assertEquals(75.5, metrics.getCpuUsage());
        assertEquals(60.2, metrics.getMemoryUsage());
        assertEquals(45.8, metrics.getDiskUsage());
        assertEquals(500.0, metrics.getNetworkIn());
        assertEquals(250.0, metrics.getNetworkOut());
        assertEquals(2.5, metrics.getLoadAverage());
        assertEquals("北京", metrics.getRegion());
        assertEquals("Web服务", metrics.getServiceType());
        assertNotNull(metrics.getTimestamp());
    }

    @Test
    @DisplayName("测试Getter和Setter方法")
    void testGettersAndSetters() {
        // 测试ID
        systemMetrics.setId(1L);
        assertEquals(1L, systemMetrics.getId());

        // 测试服务器名称
        systemMetrics.setServerName("server-02");
        assertEquals("server-02", systemMetrics.getServerName());

        // 测试CPU使用率
        systemMetrics.setCpuUsage(80.0);
        assertEquals(80.0, systemMetrics.getCpuUsage());

        // 测试内存使用率
        systemMetrics.setMemoryUsage(70.5);
        assertEquals(70.5, systemMetrics.getMemoryUsage());

        // 测试磁盘使用率
        systemMetrics.setDiskUsage(55.2);
        assertEquals(55.2, systemMetrics.getDiskUsage());

        // 测试网络入流量
        systemMetrics.setNetworkIn(1000.0);
        assertEquals(1000.0, systemMetrics.getNetworkIn());

        // 测试网络出流量
        systemMetrics.setNetworkOut(750.0);
        assertEquals(750.0, systemMetrics.getNetworkOut());

        // 测试负载平均值
        systemMetrics.setLoadAverage(3.5);
        assertEquals(3.5, systemMetrics.getLoadAverage());

        // 测试时间戳
        systemMetrics.setTimestamp(testTimestamp);
        assertEquals(testTimestamp, systemMetrics.getTimestamp());

        // 测试区域
        systemMetrics.setRegion("上海");
        assertEquals("上海", systemMetrics.getRegion());

        // 测试服务类型
        systemMetrics.setServiceType("数据库");
        assertEquals("数据库", systemMetrics.getServiceType());
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最小值
        systemMetrics.setCpuUsage(0.0);
        systemMetrics.setMemoryUsage(0.0);
        systemMetrics.setDiskUsage(0.0);
        systemMetrics.setNetworkIn(0.0);
        systemMetrics.setNetworkOut(0.0);
        systemMetrics.setLoadAverage(0.0);

        assertEquals(0.0, systemMetrics.getCpuUsage());
        assertEquals(0.0, systemMetrics.getMemoryUsage());
        assertEquals(0.0, systemMetrics.getDiskUsage());
        assertEquals(0.0, systemMetrics.getNetworkIn());
        assertEquals(0.0, systemMetrics.getNetworkOut());
        assertEquals(0.0, systemMetrics.getLoadAverage());

        // 测试最大值
        systemMetrics.setCpuUsage(100.0);
        systemMetrics.setMemoryUsage(100.0);
        systemMetrics.setDiskUsage(100.0);
        systemMetrics.setNetworkIn(Double.MAX_VALUE);
        systemMetrics.setNetworkOut(Double.MAX_VALUE);
        systemMetrics.setLoadAverage(Double.MAX_VALUE);

        assertEquals(100.0, systemMetrics.getCpuUsage());
        assertEquals(100.0, systemMetrics.getMemoryUsage());
        assertEquals(100.0, systemMetrics.getDiskUsage());
        assertEquals(Double.MAX_VALUE, systemMetrics.getNetworkIn());
        assertEquals(Double.MAX_VALUE, systemMetrics.getNetworkOut());
        assertEquals(Double.MAX_VALUE, systemMetrics.getLoadAverage());
    }

    @Test
    @DisplayName("测试字符串字段")
    void testStringFields() {
        // 测试空字符串
        systemMetrics.setServerName("");
        systemMetrics.setRegion("");
        systemMetrics.setServiceType("");

        assertEquals("", systemMetrics.getServerName());
        assertEquals("", systemMetrics.getRegion());
        assertEquals("", systemMetrics.getServiceType());

        // 测试特殊字符
        systemMetrics.setServerName("server-测试_01");
        systemMetrics.setRegion("北京-朝阳");
        systemMetrics.setServiceType("API网关/v2");

        assertEquals("server-测试_01", systemMetrics.getServerName());
        assertEquals("北京-朝阳", systemMetrics.getRegion());
        assertEquals("API网关/v2", systemMetrics.getServiceType());
    }

    @Test
    @DisplayName("测试时间戳自动设置")
    void testTimestampAutoSet() {
        SystemMetrics newMetrics = new SystemMetrics();
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        
        SystemMetrics createdMetrics = new SystemMetrics(
            "server-03", 50.0, 40.0, 30.0, 200.0, 100.0, 1.5, "深圳", "缓存"
        );
        
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(createdMetrics.getTimestamp().isAfter(beforeCreation));
        assertTrue(createdMetrics.getTimestamp().isBefore(afterCreation));
    }
}