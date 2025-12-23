package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.SystemMetrics;
import com.bub6le.systemmonitoring.repository.SystemMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class SystemMetricsService {
    
    @Autowired
    private SystemMetricsRepository systemMetricsRepository;
    
    private final Random random = new Random();
    
    public List<SystemMetrics> getAllMetrics() {
        return systemMetricsRepository.findAll();
    }
    
    public List<SystemMetrics> getRecentMetrics(int minutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        return systemMetricsRepository.findRecentMetrics(startTime);
    }
    
    public List<SystemMetrics> getMetricsByServer(String serverName) {
        return systemMetricsRepository.findByServerName(serverName);
    }
    
    public List<SystemMetrics> getMetricsByRegion(String region) {
        return systemMetricsRepository.findByRegion(region);
    }
    
    public List<SystemMetrics> getMetricsByServiceType(String serviceType) {
        return systemMetricsRepository.findByServiceType(serviceType);
    }
    
    public SystemMetrics saveMetrics(SystemMetrics metrics) {
        return systemMetricsRepository.save(metrics);
    }
    
    // 生成模拟数据用于演示
    public SystemMetrics generateMockMetrics() {
        String[] servers = {"server-01", "server-02", "server-03", "server-04", "server-05"};
        String[] regions = {"北京", "上海", "深圳", "成都", "杭州"};
        String[] serviceTypes = {"Web服务", "数据库", "缓存", "消息队列", "API网关"};
        
        String serverName = servers[random.nextInt(servers.length)];
        String region = regions[random.nextInt(regions.length)];
        String serviceType = serviceTypes[random.nextInt(serviceTypes.length)];
        
        SystemMetrics metrics = new SystemMetrics(
            serverName,
            20.0 + random.nextDouble() * 60.0, // CPU: 20-80%
            30.0 + random.nextDouble() * 50.0, // Memory: 30-80%
            10.0 + random.nextDouble() * 40.0, // Disk: 10-50%
            100.0 + random.nextDouble() * 900.0, // Network In: 100-1000 MB/s
            50.0 + random.nextDouble() * 450.0, // Network Out: 50-500 MB/s
            random.nextDouble() * 8.0, // Load Average: 0-8
            region,
            serviceType
        );
        
        return saveMetrics(metrics);
    }
    
    // 获取系统健康状态
    public SystemHealthStatus getSystemHealthStatus() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(5);
        List<SystemMetrics> recentMetrics = systemMetricsRepository.findLatestMetrics(startTime);
        
        if (recentMetrics.isEmpty()) {
            return new SystemHealthStatus("未知", 0.0, 0.0, 0.0, 0);
        }
        
        double avgCpu = recentMetrics.stream().mapToDouble(SystemMetrics::getCpuUsage).average().orElse(0.0);
        double avgMemory = recentMetrics.stream().mapToDouble(SystemMetrics::getMemoryUsage).average().orElse(0.0);
        double avgLoad = recentMetrics.stream().mapToDouble(SystemMetrics::getLoadAverage).average().orElse(0.0);
        
        String healthStatus = "健康";
        if (avgCpu > 85 || avgMemory > 90 || avgLoad > 5) {
            healthStatus = "不健康";
        } else if (avgCpu > 70 || avgMemory > 75 || avgLoad > 3) {
            healthStatus = "警告";
        }
        
        return new SystemHealthStatus(healthStatus, avgCpu, avgMemory, avgLoad, recentMetrics.size());
    }
    
    public static class SystemHealthStatus {
        private String status;
        private double avgCpu;
        private double avgMemory;
        private double avgLoad;
        private int serverCount;
        
        public SystemHealthStatus(String status, double avgCpu, double avgMemory, double avgLoad, int serverCount) {
            this.status = status;
            this.avgCpu = avgCpu;
            this.avgMemory = avgMemory;
            this.avgLoad = avgLoad;
            this.serverCount = serverCount;
        }
        
        // Getters
        public String getStatus() { return status; }
        public double getAvgCpu() { return avgCpu; }
        public double getAvgMemory() { return avgMemory; }
        public double getAvgLoad() { return avgLoad; }
        public int getServerCount() { return serverCount; }
    }
}