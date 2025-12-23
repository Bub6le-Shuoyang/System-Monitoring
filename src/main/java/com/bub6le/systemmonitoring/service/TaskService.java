package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.Task;
import com.bub6le.systemmonitoring.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    private final Random random = new Random();
    
    public List<Task> getAllTasks() {
        return taskRepository.findAllOrderByUpdatedTime();
    }
    
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
    
    public List<Task> getTasksByCluster(String cluster) {
        return taskRepository.findByTargetCluster(cluster);
    }
    
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }
    
    public Task createTask(String taskName, String targetCluster) {
        Task task = new Task(taskName, targetCluster);
        return saveTask(task);
    }
    
    public void updateTaskProgress(Long taskId, int progress) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setProgress(Math.min(100, Math.max(0, progress)));
            
            // 自动更新状态
            if (progress >= 100) {
                task.setStatus(Task.TaskStatus.COMPLETED);
            } else if (progress > 0 && task.getStatus() == Task.TaskStatus.QUEUED) {
                task.setStatus(Task.TaskStatus.RUNNING);
            }
            
            saveTask(task);
        }
    }
    
    public void failTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setStatus(Task.TaskStatus.FAILED);
            saveTask(task);
        }
    }
    
    // 生成模拟任务数据
    public Task generateMockTask() {
        String[] taskNames = {
            "系统备份", "日志清理", "性能监控", "安全扫描", "数据同步",
            "缓存更新", "数据库优化", "服务重启", "配置更新", "健康检查"
        };
        String[] clusters = {
            "生产集群", "测试集群", "开发集群", "预发布集群", "灾备集群"
        };
        
        String taskName = taskNames[random.nextInt(taskNames.length)];
        String cluster = clusters[random.nextInt(clusters.length)];
        
        Task task = createTask(taskName, cluster);
        
        // 随机设置状态和进度
        Task.TaskStatus[] statuses = {
            Task.TaskStatus.QUEUED, Task.TaskStatus.RUNNING, 
            Task.TaskStatus.COMPLETED, Task.TaskStatus.FAILED
        };
        Task.TaskStatus status = statuses[random.nextInt(statuses.length)];
        task.setStatus(status);
        
        if (status == Task.TaskStatus.RUNNING) {
            task.setProgress(20 + random.nextInt(60)); // 20-80%
        } else if (status == Task.TaskStatus.COMPLETED) {
            task.setProgress(100);
        } else {
            task.setProgress(0);
        }
        
        return saveTask(task);
    }
    
    public TaskStatusSummary getTaskStatusSummary() {
        long queuedCount = taskRepository.countByStatus(Task.TaskStatus.QUEUED);
        long runningCount = taskRepository.countByStatus(Task.TaskStatus.RUNNING);
        long failedCount = taskRepository.countByStatus(Task.TaskStatus.FAILED);
        long completedCount = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
        
        return new TaskStatusSummary(queuedCount, runningCount, failedCount, completedCount);
    }
    
    public static class TaskStatusSummary {
        private long queuedCount;
        private long runningCount;
        private long failedCount;
        private long completedCount;
        
        public TaskStatusSummary(long queuedCount, long runningCount, long failedCount, long completedCount) {
            this.queuedCount = queuedCount;
            this.runningCount = runningCount;
            this.failedCount = failedCount;
            this.completedCount = completedCount;
        }
        
        // Getters
        public long getQueuedCount() { return queuedCount; }
        public long getRunningCount() { return runningCount; }
        public long getFailedCount() { return failedCount; }
        public long getCompletedCount() { return completedCount; }
        public long getTotalCount() { return queuedCount + runningCount + failedCount + completedCount; }
    }
}