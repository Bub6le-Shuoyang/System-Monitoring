package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.Alert;
import com.bub6le.systemmonitoring.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AlertService {
    
    @Autowired
    private AlertRepository alertRepository;
    
    private final Random random = new Random();
    
    public List<Alert> getAllAlerts() {
        return alertRepository.findAllOrderByTimestamp();
    }
    
    public List<Alert> getUnresolvedAlerts() {
        return alertRepository.findUnresolvedAlerts();
    }
    
    public List<Alert> getAlertsBySource(String source) {
        return alertRepository.findBySource(source);
    }
    
    public List<Alert> getAlertsBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }
    
    public Alert saveAlert(Alert alert) {
        return alertRepository.save(alert);
    }
    
    public Alert createAlert(String source, Alert.AlertSeverity severity, String message) {
        Alert alert = new Alert(source, severity, message);
        return saveAlert(alert);
    }
    
    public void resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.setResolved(true);
            saveAlert(alert);
        }
    }
    
    // 生成模拟告警数据
    public Alert generateMockAlert() {
        String[] sources = {
            "server-01", "server-02", "server-03", "server-04", "server-05",
            "数据库集群", "缓存服务", "API网关", "负载均衡器", "监控系统"
        };
        
        String[] lowSeverityMessages = {
            "CPU使用率略高", "内存使用率上升", "磁盘空间不足", "网络延迟增加",
            "响应时间变慢", "连接数接近上限", "缓存命中率下降"
        };
        
        String[] mediumSeverityMessages = {
            "CPU使用率过高", "内存使用率过高", "磁盘空间严重不足", "网络连接异常",
            "服务响应超时", "数据库连接池满", "错误率增加"
        };
        
        String[] highSeverityMessages = {
            "CPU使用率严重过高", "内存溢出风险", "磁盘空间即将耗尽", "网络连接中断",
            "服务不可用", "数据库连接失败", "系统崩溃风险"
        };
        
        String[] criticalSeverityMessages = {
            "系统崩溃", "服务完全不可用", "数据丢失风险", "安全漏洞被利用",
            "大规模故障", "数据中心故障", "网络完全中断"
        };
        
        String source = sources[random.nextInt(sources.length)];
        Alert.AlertSeverity severity = getRandomSeverity();
        String message = getRandomMessage(severity, lowSeverityMessages, 
                                       mediumSeverityMessages, highSeverityMessages, 
                                       criticalSeverityMessages);
        
        return createAlert(source, severity, message);
    }
    
    private Alert.AlertSeverity getRandomSeverity() {
        Alert.AlertSeverity[] severities = Alert.AlertSeverity.values();
        // 增加低严重性告警的概率
        int[] weights = {40, 30, 20, 10}; // LOW, MEDIUM, HIGH, CRITICAL
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (int i = 0; i < severities.length; i++) {
            currentWeight += weights[i];
            if (randomValue < currentWeight) {
                return severities[i];
            }
        }
        
        return Alert.AlertSeverity.LOW;
    }
    
    private String getRandomMessage(Alert.AlertSeverity severity, 
                                  String[] lowMessages, String[] mediumMessages,
                                  String[] highMessages, String[] criticalMessages) {
        String[] messages;
        switch (severity) {
            case LOW:
                messages = lowMessages;
                break;
            case MEDIUM:
                messages = mediumMessages;
                break;
            case HIGH:
                messages = highMessages;
                break;
            case CRITICAL:
                messages = criticalMessages;
                break;
            default:
                messages = lowMessages;
        }
        
        return messages[random.nextInt(messages.length)];
    }
    
    public AlertSummary getAlertSummary() {
        long unresolvedCount = alertRepository.countUnresolvedAlerts();
        List<Alert> recentAlerts = getUnresolvedAlerts();
        
        long lowCount = recentAlerts.stream()
            .filter(a -> a.getSeverity() == Alert.AlertSeverity.LOW)
            .count();
        long mediumCount = recentAlerts.stream()
            .filter(a -> a.getSeverity() == Alert.AlertSeverity.MEDIUM)
            .count();
        long highCount = recentAlerts.stream()
            .filter(a -> a.getSeverity() == Alert.AlertSeverity.HIGH)
            .count();
        long criticalCount = recentAlerts.stream()
            .filter(a -> a.getSeverity() == Alert.AlertSeverity.CRITICAL)
            .count();
        
        return new AlertSummary(unresolvedCount, lowCount, mediumCount, highCount, criticalCount);
    }
    
    public static class AlertSummary {
        private long unresolvedCount;
        private long lowCount;
        private long mediumCount;
        private long highCount;
        private long criticalCount;
        
        public AlertSummary(long unresolvedCount, long lowCount, long mediumCount, 
                          long highCount, long criticalCount) {
            this.unresolvedCount = unresolvedCount;
            this.lowCount = lowCount;
            this.mediumCount = mediumCount;
            this.highCount = highCount;
            this.criticalCount = criticalCount;
        }
        
        // Getters
        public long getUnresolvedCount() { return unresolvedCount; }
        public long getLowCount() { return lowCount; }
        public long getMediumCount() { return mediumCount; }
        public long getHighCount() { return highCount; }
        public long getCriticalCount() { return criticalCount; }
    }
}