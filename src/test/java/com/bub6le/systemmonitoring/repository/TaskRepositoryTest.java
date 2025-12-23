package com.bub6le.systemmonitoring.repository;

import com.bub6le.systemmonitoring.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private Task testTask4;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        // 清理数据库
        taskRepository.deleteAll();
        
        baseTime = LocalDateTime.of(2023, 12, 23, 10, 0, 0);
        
        // 创建测试数据 - 不设置ID，让数据库自动生成
        testTask1 = new Task("系统备份", "生产集群");
        testTask1.setStatus(Task.TaskStatus.QUEUED);
        testTask1.setProgress(0);
        testTask1.setCreatedTime(baseTime);
        testTask1.setUpdatedTime(baseTime);

        testTask2 = new Task("日志清理", "测试集群");
        testTask2.setStatus(Task.TaskStatus.RUNNING);
        testTask2.setProgress(50);
        testTask2.setCreatedTime(baseTime.plusMinutes(1));
        testTask2.setUpdatedTime(baseTime.plusMinutes(2));

        testTask3 = new Task("性能监控", "开发集群");
        testTask3.setStatus(Task.TaskStatus.COMPLETED);
        testTask3.setProgress(100);
        testTask3.setCreatedTime(baseTime.plusMinutes(2));
        testTask3.setUpdatedTime(baseTime.plusMinutes(3));

        testTask4 = new Task("安全扫描", "生产集群");
        testTask4.setStatus(Task.TaskStatus.FAILED);
        testTask4.setProgress(25);
        testTask4.setCreatedTime(baseTime.plusMinutes(3));
        testTask4.setUpdatedTime(baseTime.plusMinutes(4));
    }

    @Test
    @DisplayName("测试根据状态查找任务")
    void testFindByStatus() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);
        taskRepository.save(testTask3);
        taskRepository.save(testTask4);

        // When
        List<Task> queuedTasks = taskRepository.findByStatus(Task.TaskStatus.QUEUED);
        List<Task> runningTasks = taskRepository.findByStatus(Task.TaskStatus.RUNNING);
        List<Task> completedTasks = taskRepository.findByStatus(Task.TaskStatus.COMPLETED);
        List<Task> failedTasks = taskRepository.findByStatus(Task.TaskStatus.FAILED);

        // Then
        assertEquals(1, queuedTasks.size());
        assertEquals(Task.TaskStatus.QUEUED, queuedTasks.get(0).getStatus());

        assertEquals(1, runningTasks.size());
        assertEquals(Task.TaskStatus.RUNNING, runningTasks.get(0).getStatus());

        assertEquals(1, completedTasks.size());
        assertEquals(Task.TaskStatus.COMPLETED, completedTasks.get(0).getStatus());

        assertEquals(1, failedTasks.size());
        assertEquals(Task.TaskStatus.FAILED, failedTasks.get(0).getStatus());
    }

    @Test
    @DisplayName("测试根据目标集群查找任务")
    void testFindByTargetCluster() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);
        taskRepository.save(testTask3);
        taskRepository.save(testTask4);

        // When
        List<Task> productionTasks = taskRepository.findByTargetCluster("生产集群");
        List<Task> testTasks = taskRepository.findByTargetCluster("测试集群");
        List<Task> devTasks = taskRepository.findByTargetCluster("开发集群");

        // Then
        assertEquals(2, productionTasks.size());
        productionTasks.forEach(task -> assertEquals("生产集群", task.getTargetCluster()));

        assertEquals(1, testTasks.size());
        assertEquals("测试集群", testTasks.get(0).getTargetCluster());

        assertEquals(1, devTasks.size());
        assertEquals("开发集群", devTasks.get(0).getTargetCluster());
    }

    @Test
    @DisplayName("测试查找所有任务按更新时间排序")
    void testFindAllOrderByUpdatedTime() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);
        taskRepository.save(testTask3);
        taskRepository.save(testTask4);

        // When
        List<Task> result = taskRepository.findAllOrderByUpdatedTime();

        // Then
        assertEquals(4, result.size());
        // 验证按更新时间降序排列
        assertTrue(result.get(0).getUpdatedTime().isAfter(result.get(1).getUpdatedTime()));
        assertTrue(result.get(1).getUpdatedTime().isAfter(result.get(2).getUpdatedTime()));
        assertTrue(result.get(2).getUpdatedTime().isAfter(result.get(3).getUpdatedTime()));
    }

    @Test
    @DisplayName("测试根据多个状态查找任务按更新时间排序")
    void testFindByStatusInOrderByUpdatedTime() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);
        taskRepository.save(testTask3);
        taskRepository.save(testTask4);

        List<Task.TaskStatus> statuses = Arrays.asList(
            Task.TaskStatus.QUEUED, Task.TaskStatus.RUNNING
        );

        // When
        List<Task> result = taskRepository.findByStatusInOrderByUpdatedTime(statuses);

        // Then
        assertEquals(2, result.size());
        // 验证按更新时间降序排列
        assertTrue(result.get(0).getUpdatedTime().isAfter(result.get(1).getUpdatedTime()));
        // 验证只包含指定状态的任务
        result.forEach(task -> {
            assertTrue(task.getStatus() == Task.TaskStatus.QUEUED || 
                      task.getStatus() == Task.TaskStatus.RUNNING);
        });
    }

    @Test
    @DisplayName("测试根据状态统计任务数量")
    void testCountByStatus() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);
        taskRepository.save(testTask3);
        taskRepository.save(testTask4);

        // When
        Long queuedCount = taskRepository.countByStatus(Task.TaskStatus.QUEUED);
        Long runningCount = taskRepository.countByStatus(Task.TaskStatus.RUNNING);
        Long completedCount = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
        Long failedCount = taskRepository.countByStatus(Task.TaskStatus.FAILED);

        // Then
        assertEquals(Long.valueOf(1), queuedCount);
        assertEquals(Long.valueOf(1), runningCount);
        assertEquals(Long.valueOf(1), completedCount);
        assertEquals(Long.valueOf(1), failedCount);
    }

    @Test
    @DisplayName("测试查找不存在的状态")
    void testFindByNonExistentStatus() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);

        // 创建一个新任务但不保存
        Task unsavedTask = new Task("未保存任务", "测试集群");
        unsavedTask.setStatus(Task.TaskStatus.RUNNING);

        // When
        List<Task> result = taskRepository.findByStatus(Task.TaskStatus.RUNNING);

        // Then
        assertEquals(1, result.size());
        assertEquals("日志清理", result.get(0).getTaskName());
    }

    @Test
    @DisplayName("测试查找不存在的集群")
    void testFindByNonExistentCluster() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);

        // When
        List<Task> result = taskRepository.findByTargetCluster("不存在的集群");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试统计不存在的状态")
    void testCountByNonExistentStatus() {
        // Given
        taskRepository.save(testTask1);
        taskRepository.save(testTask2);

        // When
        Long count = taskRepository.countByStatus(Task.TaskStatus.FAILED);

        // Then
        assertEquals(Long.valueOf(0), count);
    }

    @Test
    @DisplayName("测试保存和检索任务")
    void testSaveAndRetrieveTask() {
        // When
        Task savedTask = taskRepository.save(testTask1);
        Task retrievedTask = taskRepository.findById(savedTask.getId()).orElse(null);

        // Then
        assertNotNull(retrievedTask);
        assertEquals(testTask1.getTaskName(), retrievedTask.getTaskName());
        assertEquals(testTask1.getTargetCluster(), retrievedTask.getTargetCluster());
        assertEquals(testTask1.getStatus(), retrievedTask.getStatus());
        assertEquals(testTask1.getProgress(), retrievedTask.getProgress());
        assertEquals(testTask1.getCreatedTime(), retrievedTask.getCreatedTime());
        assertEquals(testTask1.getUpdatedTime(), retrievedTask.getUpdatedTime());
    }

    @Test
    @DisplayName("测试删除任务")
    void testDeleteTask() {
        // Given
        Task savedTask = taskRepository.save(testTask1);
        Long id = savedTask.getId();

        // When
        taskRepository.deleteById(id);
        Task deletedTask = taskRepository.findById(id).orElse(null);

        // Then
        assertNull(deletedTask);
    }

    @Test
    @DisplayName("测试更新任务")
    void testUpdateTask() {
        // Given
        Task savedTask = taskRepository.save(testTask1);
        savedTask.setStatus(Task.TaskStatus.RUNNING);
        savedTask.setProgress(75);
        savedTask.setUpdatedTime(LocalDateTime.now());

        // When
        Task updatedTask = taskRepository.save(savedTask);

        // Then
        assertEquals(Task.TaskStatus.RUNNING, updatedTask.getStatus());
        assertEquals(Integer.valueOf(75), updatedTask.getProgress());
    }

    @Test
    @DisplayName("测试查询结果排序 - 多个相同更新时间的任务")
    void testQueryResultOrderingWithSameUpdateTime() {
        // Given
        Task task1 = new Task("任务1", "集群1");
        task1.setStatus(Task.TaskStatus.QUEUED);
        task1.setUpdatedTime(baseTime);
        
        Task task2 = new Task("任务2", "集群2");
        task2.setStatus(Task.TaskStatus.RUNNING);
        task2.setUpdatedTime(baseTime.plusMinutes(1));
        
        Task task3 = new Task("任务3", "集群3");
        task3.setStatus(Task.TaskStatus.COMPLETED);
        task3.setUpdatedTime(baseTime.plusMinutes(2));
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        // When
        List<Task> result = taskRepository.findAllOrderByUpdatedTime();

        // Then
        assertEquals(3, result.size());
        assertEquals("任务3", result.get(0).getTaskName());
        assertEquals("任务2", result.get(1).getTaskName());
        assertEquals("任务1", result.get(2).getTaskName());
    }

    @Test
    @DisplayName("测试所有任务状态")
    void testAllTaskStatuses() {
        // Given
        taskRepository.save(testTask1); // QUEUED
        taskRepository.save(testTask2); // RUNNING
        taskRepository.save(testTask3); // COMPLETED
        taskRepository.save(testTask4); // FAILED

        // When & Then
        assertEquals(1, taskRepository.findByStatus(Task.TaskStatus.QUEUED).size());
        assertEquals(1, taskRepository.findByStatus(Task.TaskStatus.RUNNING).size());
        assertEquals(1, taskRepository.findByStatus(Task.TaskStatus.COMPLETED).size());
        assertEquals(1, taskRepository.findByStatus(Task.TaskStatus.FAILED).size());
    }

    @Test
    @DisplayName("测试多个状态查询")
    void testMultipleStatusQuery() {
        // Given
        taskRepository.save(testTask1); // QUEUED
        taskRepository.save(testTask2); // RUNNING
        taskRepository.save(testTask3); // COMPLETED
        taskRepository.save(testTask4); // FAILED

        List<Task.TaskStatus> activeStatuses = Arrays.asList(
            Task.TaskStatus.QUEUED, Task.TaskStatus.RUNNING
        );

        List<Task.TaskStatus> finishedStatuses = Arrays.asList(
            Task.TaskStatus.COMPLETED, Task.TaskStatus.FAILED
        );

        // When
        List<Task> activeTasks = taskRepository.findByStatusInOrderByUpdatedTime(activeStatuses);
        List<Task> finishedTasks = taskRepository.findByStatusInOrderByUpdatedTime(finishedStatuses);

        // Then
        assertEquals(2, activeTasks.size());
        assertEquals(2, finishedTasks.size());
        
        // 验证活跃任务
        activeTasks.forEach(task -> {
            assertTrue(task.getStatus() == Task.TaskStatus.QUEUED || 
                      task.getStatus() == Task.TaskStatus.RUNNING);
        });
        
        // 验证已完成任务
        finishedTasks.forEach(task -> {
            assertTrue(task.getStatus() == Task.TaskStatus.COMPLETED || 
                      task.getStatus() == Task.TaskStatus.FAILED);
        });
    }
}