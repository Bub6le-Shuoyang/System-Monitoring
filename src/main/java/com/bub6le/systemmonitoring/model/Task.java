package com.bub6le.systemmonitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "target_cluster")
    private String targetCluster;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status;
    
    @Column(name = "progress")
    private Integer progress;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // Constructors
    public Task() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.status = TaskStatus.QUEUED;
        this.progress = 0;
    }
    
    public Task(String taskName, String targetCluster) {
        this();
        this.taskName = taskName;
        this.targetCluster = targetCluster;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getTargetCluster() {
        return targetCluster;
    }
    
    public void setTargetCluster(String targetCluster) {
        this.targetCluster = targetCluster;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedTime = LocalDateTime.now();
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
        this.updatedTime = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    // Task Status Enum
    public enum TaskStatus {
        QUEUED("排队中"),
        RUNNING("运行中"),
        FAILED("失败"),
        COMPLETED("完成");
        
        private final String description;
        
        TaskStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}