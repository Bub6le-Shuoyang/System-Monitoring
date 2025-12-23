package com.bub6le.systemmonitoring.controller;

import com.bub6le.systemmonitoring.model.Alert;
import com.bub6le.systemmonitoring.model.SystemMetrics;
import com.bub6le.systemmonitoring.model.Task;
import com.bub6le.systemmonitoring.service.AlertService;
import com.bub6le.systemmonitoring.service.SystemMetricsService;
import com.bub6le.systemmonitoring.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@EnableScheduling
public class WebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private SystemMetricsService systemMetricsService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AlertService alertService;
    
    // 处理客户端发送的消息
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String message) throws Exception {
        return "Hello, " + message + "!";
    }
    
    // 定时推送系统指标数据（每2秒）
    @Scheduled(fixedRate = 2000)
    public void pushSystemMetrics() {
        List<SystemMetrics> metrics = systemMetricsService.getRecentMetrics(5);
        
        // 确保有数据才推送
        if (!metrics.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/metrics", metrics);
        }
        
        // 推送系统健康状态
        SystemMetricsService.SystemHealthStatus healthStatus = systemMetricsService.getSystemHealthStatus();
        messagingTemplate.convertAndSend("/topic/health", healthStatus);
    }
    
    // 定时推送任务数据（每3秒）
    @Scheduled(fixedRate = 3000)
    public void pushTasks() {
        List<Task> tasks = taskService.getAllTasks();
        messagingTemplate.convertAndSend("/topic/tasks", tasks);
        
        // 推送任务摘要
        TaskService.TaskStatusSummary taskSummary = taskService.getTaskStatusSummary();
        messagingTemplate.convertAndSend("/topic/task-summary", taskSummary);
    }
    
    // 定时推送告警数据（每5秒）
    @Scheduled(fixedRate = 5000)
    public void pushAlerts() {
        List<Alert> alerts = alertService.getUnresolvedAlerts();
        messagingTemplate.convertAndSend("/topic/alerts", alerts);
        
        // 推送告警摘要
        AlertService.AlertSummary alertSummary = alertService.getAlertSummary();
        messagingTemplate.convertAndSend("/topic/alert-summary", alertSummary);
    }
    
    // 生成模拟数据的方法，可以通过API调用
    public void generateMockData() {
        // 生成模拟系统指标
        SystemMetrics metrics = systemMetricsService.generateMockMetrics();
        messagingTemplate.convertAndSend("/topic/new-metric", metrics);
        
        // 随机生成模拟任务
        if (Math.random() > 0.7) {
            Task task = taskService.generateMockTask();
            messagingTemplate.convertAndSend("/topic/new-task", task);
        }
        
        // 随机生成模拟告警
        if (Math.random() > 0.8) {
            Alert alert = alertService.generateMockAlert();
            messagingTemplate.convertAndSend("/topic/new-alert", alert);
        }
    }
    
    // 定时生成模拟数据（每5秒）
    @Scheduled(fixedRate = 5000)
    public void scheduledDataGeneration() {
        if (Math.random() > 0.5) {
            generateMockData();
        }
    }
}