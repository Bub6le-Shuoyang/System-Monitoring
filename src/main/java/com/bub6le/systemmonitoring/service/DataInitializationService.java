package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.Alert;
import com.bub6le.systemmonitoring.model.SystemMetrics;
import com.bub6le.systemmonitoring.model.Task;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class DataInitializationService {
    
    @Autowired
    private SystemMetricsService systemMetricsService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AlertService alertService;
    
    private final Random random = new Random();
    
    @PostConstruct
    public void initializeData() {
        // 生成初始系统指标数据
        generateInitialMetrics();
        
        // 生成初始任务数据
        generateInitialTasks();
        
        // 生成初始告警数据
        generateInitialAlerts();
    }
    
    private void generateInitialMetrics() {
        // 生成最近5分钟的模拟数据
        for (int i = 0; i < 20; i++) {
            SystemMetrics metrics = systemMetricsService.generateMockMetrics();
            // 设置为过去的时间，确保数据分布在最近5分钟内
            metrics.setTimestamp(LocalDateTime.now().minusMinutes(5).plusSeconds(i * 15));
            systemMetricsService.saveMetrics(metrics);
        }
    }
    
    private void generateInitialTasks() {
        // 生成10个初始任务
        for (int i = 0; i < 10; i++) {
            Task task = taskService.generateMockTask();
            // 设置为过去的时间
            task.setCreatedTime(LocalDateTime.now().minusMinutes(random.nextInt(60)));
            task.setUpdatedTime(task.getCreatedTime().plusMinutes(random.nextInt(30)));
            taskService.saveTask(task);
        }
    }
    
    private void generateInitialAlerts() {
        // 生成5个初始告警
        for (int i = 0; i < 5; i++) {
            Alert alert = alertService.generateMockAlert();
            // 设置为过去的时间
            alert.setTimestamp(LocalDateTime.now().minusMinutes(random.nextInt(120)));
            // 随机设置一些告警为已解决
            if (random.nextBoolean()) {
                alert.setResolved(true);
            }
            alertService.saveAlert(alert);
        }
    }
}