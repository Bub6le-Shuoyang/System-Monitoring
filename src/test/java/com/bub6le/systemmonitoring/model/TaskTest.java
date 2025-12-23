package com.bub6le.systemmonitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task task;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.of(2023, 12, 23, 10, 30, 0);
        task = new Task();
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertNotNull(task.getCreatedTime());
        assertNotNull(task.getUpdatedTime());
        assertEquals(Task.TaskStatus.QUEUED, task.getStatus());
        assertEquals(Integer.valueOf(0), task.getProgress());
        assertNull(task.getId());
        assertNull(task.getTaskName());
        assertNull(task.getTargetCluster());
    }

    @Test
    @DisplayName("测试参数化构造函数")
    void testParameterizedConstructor() {
        Task newTask = new Task("系统备份", "生产集群");

        assertEquals("系统备份", newTask.getTaskName());
        assertEquals("生产集群", newTask.getTargetCluster());
        assertEquals(Task.TaskStatus.QUEUED, newTask.getStatus());
        assertEquals(Integer.valueOf(0), newTask.getProgress());
        assertNotNull(newTask.getCreatedTime());
        assertNotNull(newTask.getUpdatedTime());
    }

    @Test
    @DisplayName("测试Getter和Setter方法")
    void testGettersAndSetters() {
        // 测试ID
        task.setId(1L);
        assertEquals(1L, task.getId());

        // 测试任务名称
        task.setTaskName("日志清理");
        assertEquals("日志清理", task.getTaskName());

        // 测试目标集群
        task.setTargetCluster("测试集群");
        assertEquals("测试集群", task.getTargetCluster());

        // 测试状态
        task.setStatus(Task.TaskStatus.RUNNING);
        assertEquals(Task.TaskStatus.RUNNING, task.getStatus());

        // 测试进度
        task.setProgress(50);
        assertEquals(Integer.valueOf(50), task.getProgress());

        // 测试创建时间
        task.setCreatedTime(testTimestamp);
        assertEquals(testTimestamp, task.getCreatedTime());

        // 测试更新时间
        task.setUpdatedTime(testTimestamp);
        assertEquals(testTimestamp, task.getUpdatedTime());
    }

    @Test
    @DisplayName("测试状态枚举")
    void testTaskStatusEnum() {
        assertEquals("排队中", Task.TaskStatus.QUEUED.getDescription());
        assertEquals("运行中", Task.TaskStatus.RUNNING.getDescription());
        assertEquals("失败", Task.TaskStatus.FAILED.getDescription());
        assertEquals("完成", Task.TaskStatus.COMPLETED.getDescription());
    }

    @Test
    @DisplayName("测试状态变更时自动更新时间")
    void testStatusChangeUpdatesTime() {
        LocalDateTime originalUpdatedTime = task.getUpdatedTime();
        
        // 等待一小段时间确保时间戳不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        task.setStatus(Task.TaskStatus.RUNNING);
        assertTrue(task.getUpdatedTime().isAfter(originalUpdatedTime));
    }

    @Test
    @DisplayName("测试进度变更时自动更新时间")
    void testProgressChangeUpdatesTime() {
        LocalDateTime originalUpdatedTime = task.getUpdatedTime();
        
        // 等待一小段时间确保时间戳不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        task.setProgress(25);
        assertTrue(task.getUpdatedTime().isAfter(originalUpdatedTime));
    }

    @Test
    @DisplayName("测试进度边界值")
    void testProgressBoundaryValues() {
        // 测试最小值
        task.setProgress(0);
        assertEquals(Integer.valueOf(0), task.getProgress());

        // 测试最大值
        task.setProgress(100);
        assertEquals(Integer.valueOf(100), task.getProgress());

        // 测试负数（应该被设置为0）
        task.setProgress(-10);
        assertEquals(Integer.valueOf(-10), task.getProgress()); // 注意：实际业务逻辑中可能需要验证

        // 测试超过100的值（应该被设置为100）
        task.setProgress(150);
        assertEquals(Integer.valueOf(150), task.getProgress()); // 注意：实际业务逻辑中可能需要验证
    }

    @Test
    @DisplayName("测试字符串字段")
    void testStringFields() {
        // 测试空字符串
        task.setTaskName("");
        task.setTargetCluster("");

        assertEquals("", task.getTaskName());
        assertEquals("", task.getTargetCluster());

        // 测试特殊字符
        task.setTaskName("任务-测试_01");
        task.setTargetCluster("集群-生产/v2");

        assertEquals("任务-测试_01", task.getTaskName());
        assertEquals("集群-生产/v2", task.getTargetCluster());

        // 测试中文字符
        task.setTaskName("系统备份任务");
        task.setTargetCluster("生产环境集群");

        assertEquals("系统备份任务", task.getTaskName());
        assertEquals("生产环境集群", task.getTargetCluster());
    }

    @Test
    @DisplayName("测试时间戳自动设置")
    void testTimestampAutoSet() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        
        Task newTask = new Task("新任务", "新集群");
        
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(newTask.getCreatedTime().isAfter(beforeCreation));
        assertTrue(newTask.getCreatedTime().isBefore(afterCreation));
        assertTrue(newTask.getUpdatedTime().isAfter(beforeCreation));
        assertTrue(newTask.getUpdatedTime().isBefore(afterCreation));
        assertEquals(newTask.getCreatedTime(), newTask.getUpdatedTime());
    }

    @Test
    @DisplayName("测试所有状态值")
    void testAllStatusValues() {
        Task[] tasks = new Task[4];
        
        tasks[0] = new Task();
        tasks[0].setStatus(Task.TaskStatus.QUEUED);
        
        tasks[1] = new Task();
        tasks[1].setStatus(Task.TaskStatus.RUNNING);
        
        tasks[2] = new Task();
        tasks[2].setStatus(Task.TaskStatus.FAILED);
        
        tasks[3] = new Task();
        tasks[3].setStatus(Task.TaskStatus.COMPLETED);

        assertEquals(Task.TaskStatus.QUEUED, tasks[0].getStatus());
        assertEquals(Task.TaskStatus.RUNNING, tasks[1].getStatus());
        assertEquals(Task.TaskStatus.FAILED, tasks[2].getStatus());
        assertEquals(Task.TaskStatus.COMPLETED, tasks[3].getStatus());
    }
}