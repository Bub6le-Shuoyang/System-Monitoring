package com.bub6le.systemmonitoring.service;

import com.bub6le.systemmonitoring.model.Task;
import com.bub6le.systemmonitoring.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private List<Task> mockTasksList;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testTask = new Task("系统备份", "生产集群");
        testTask.setId(1L);
        testTask.setStatus(Task.TaskStatus.RUNNING);
        testTask.setProgress(50);

        mockTasksList = Arrays.asList(
            testTask,
            new Task("日志清理", "测试集群"),
            new Task("性能监控", "开发集群")
        );
        
        // 设置第二个任务的状态
        mockTasksList.get(1).setStatus(Task.TaskStatus.COMPLETED);
        mockTasksList.get(1).setProgress(100);
        mockTasksList.get(1).setId(2L);
        
        // 设置第三个任务的状态
        mockTasksList.get(2).setStatus(Task.TaskStatus.FAILED);
        mockTasksList.get(2).setProgress(0);
        mockTasksList.get(2).setId(3L);
    }

    @Test
    @DisplayName("测试获取所有任务")
    void testGetAllTasks() {
        // Given
        when(taskRepository.findAllOrderByUpdatedTime()).thenReturn(mockTasksList);

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("系统备份", result.get(0).getTaskName());
        assertEquals("日志清理", result.get(1).getTaskName());
        assertEquals("性能监控", result.get(2).getTaskName());
        verify(taskRepository, times(1)).findAllOrderByUpdatedTime();
    }

    @Test
    @DisplayName("测试根据状态获取任务")
    void testGetTasksByStatus() {
        // Given
        Task.TaskStatus status = Task.TaskStatus.RUNNING;
        List<Task> runningTasks = Arrays.asList(testTask);
        when(taskRepository.findByStatus(status)).thenReturn(runningTasks);

        // When
        List<Task> result = taskService.getTasksByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        verify(taskRepository, times(1)).findByStatus(status);
    }

    @Test
    @DisplayName("测试根据集群获取任务")
    void testGetTasksByCluster() {
        // Given
        String cluster = "生产集群";
        List<Task> clusterTasks = Arrays.asList(testTask);
        when(taskRepository.findByTargetCluster(cluster)).thenReturn(clusterTasks);

        // When
        List<Task> result = taskService.getTasksByCluster(cluster);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cluster, result.get(0).getTargetCluster());
        verify(taskRepository, times(1)).findByTargetCluster(cluster);
    }

    @Test
    @DisplayName("测试保存任务")
    void testSaveTask() {
        // Given
        Task newTask = new Task("数据同步", "预发布集群");
        when(taskRepository.save(newTask)).thenReturn(newTask);

        // When
        Task result = taskService.saveTask(newTask);

        // Then
        assertNotNull(result);
        assertEquals("数据同步", result.getTaskName());
        assertEquals("预发布集群", result.getTargetCluster());
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    @DisplayName("测试创建任务")
    void testCreateTask() {
        // Given
        String taskName = "安全扫描";
        String targetCluster = "灾备集群";
        Task createdTask = new Task(taskName, targetCluster);
        when(taskRepository.save(any(Task.class))).thenReturn(createdTask);

        // When
        Task result = taskService.createTask(taskName, targetCluster);

        // Then
        assertNotNull(result);
        assertEquals(taskName, result.getTaskName());
        assertEquals(targetCluster, result.getTargetCluster());
        assertEquals(Task.TaskStatus.QUEUED, result.getStatus());
        assertEquals(Integer.valueOf(0), result.getProgress());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("测试更新任务进度 - 正常进度")
    void testUpdateTaskProgressNormal() {
        // Given
        Long taskId = 1L;
        int progress = 75;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        taskService.updateTaskProgress(taskId, progress);

        // Then
        assertEquals(Integer.valueOf(75), testTask.getProgress());
        assertEquals(Task.TaskStatus.RUNNING, testTask.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    @DisplayName("测试更新任务进度 - 完成状态")
    void testUpdateTaskProgressCompleted() {
        // Given
        Long taskId = 1L;
        int progress = 100;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        taskService.updateTaskProgress(taskId, progress);

        // Then
        assertEquals(Integer.valueOf(100), testTask.getProgress());
        assertEquals(Task.TaskStatus.COMPLETED, testTask.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    @DisplayName("测试更新任务进度 - 从排队到运行")
    void testUpdateTaskProgressQueuedToRunning() {
        // Given
        Task queuedTask = new Task("新任务", "测试集群");
        queuedTask.setStatus(Task.TaskStatus.QUEUED);
        Long taskId = 2L;
        int progress = 25;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(queuedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(queuedTask);

        // When
        taskService.updateTaskProgress(taskId, progress);

        // Then
        assertEquals(Integer.valueOf(25), queuedTask.getProgress());
        assertEquals(Task.TaskStatus.RUNNING, queuedTask.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(queuedTask);
    }

    @Test
    @DisplayName("测试更新任务进度 - 边界值")
    void testUpdateTaskProgressBoundaryValues() {
        // Given
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When & Then - 测试负数
        taskService.updateTaskProgress(taskId, -10);
        assertEquals(Integer.valueOf(0), testTask.getProgress());

        // When & Then - 测试超过100
        taskService.updateTaskProgress(taskId, 150);
        assertEquals(Integer.valueOf(100), testTask.getProgress());

        verify(taskRepository, times(2)).findById(taskId);
        verify(taskRepository, times(2)).save(testTask);
    }

    @Test
    @DisplayName("测试更新任务进度 - 任务不存在")
    void testUpdateTaskProgressTaskNotFound() {
        // Given
        Long taskId = 999L;
        int progress = 50;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        taskService.updateTaskProgress(taskId, progress);

        // Then
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("测试任务失败")
    void testFailTask() {
        // Given
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        taskService.failTask(taskId);

        // Then
        assertEquals(Task.TaskStatus.FAILED, testTask.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    @DisplayName("测试任务失败 - 任务不存在")
    void testFailTaskNotFound() {
        // Given
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        taskService.failTask(taskId);

        // Then
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("测试生成模拟任务")
    void testGenerateMockTask() {
        // Given
        Task savedTask = new Task();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskService.generateMockTask();

        // Then
        assertNotNull(result);
        assertNotNull(result.getTaskName());
        assertNotNull(result.getTargetCluster());
        assertNotNull(result.getStatus());
        assertNotNull(result.getProgress());
        assertNotNull(result.getCreatedTime());
        assertNotNull(result.getUpdatedTime());

        // 验证任务名称在预期范围内
        String[] expectedTaskNames = {
            "系统备份", "日志清理", "性能监控", "安全扫描", "数据同步",
            "缓存更新", "数据库优化", "服务重启", "配置更新", "健康检查"
        };
        assertTrue(Arrays.asList(expectedTaskNames).contains(result.getTaskName()));

        // 验证集群名称在预期范围内
        String[] expectedClusters = {
            "生产集群", "测试集群", "开发集群", "预发布集群", "灾备集群"
        };
        assertTrue(Arrays.asList(expectedClusters).contains(result.getTargetCluster()));

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("测试获取任务状态摘要")
    void testGetTaskStatusSummary() {
        // Given
        when(taskRepository.countByStatus(Task.TaskStatus.QUEUED)).thenReturn(5L);
        when(taskRepository.countByStatus(Task.TaskStatus.RUNNING)).thenReturn(3L);
        when(taskRepository.countByStatus(Task.TaskStatus.FAILED)).thenReturn(2L);
        when(taskRepository.countByStatus(Task.TaskStatus.COMPLETED)).thenReturn(10L);

        // When
        TaskService.TaskStatusSummary result = taskService.getTaskStatusSummary();

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getQueuedCount());
        assertEquals(3L, result.getRunningCount());
        assertEquals(2L, result.getFailedCount());
        assertEquals(10L, result.getCompletedCount());
        assertEquals(20L, result.getTotalCount());

        verify(taskRepository, times(1)).countByStatus(Task.TaskStatus.QUEUED);
        verify(taskRepository, times(1)).countByStatus(Task.TaskStatus.RUNNING);
        verify(taskRepository, times(1)).countByStatus(Task.TaskStatus.FAILED);
        verify(taskRepository, times(1)).countByStatus(Task.TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("测试TaskStatusSummary类")
    void testTaskStatusSummaryClass() {
        // Given
        long queuedCount = 5L;
        long runningCount = 3L;
        long failedCount = 2L;
        long completedCount = 10L;

        // When
        TaskService.TaskStatusSummary summary = 
            new TaskService.TaskStatusSummary(queuedCount, runningCount, failedCount, completedCount);

        // Then
        assertEquals(queuedCount, summary.getQueuedCount());
        assertEquals(runningCount, summary.getRunningCount());
        assertEquals(failedCount, summary.getFailedCount());
        assertEquals(completedCount, summary.getCompletedCount());
        assertEquals(20L, summary.getTotalCount());
    }

    @Test
    @DisplayName("测试边界情况 - 空列表处理")
    void testEmptyListHandling() {
        // Given
        when(taskRepository.findAllOrderByUpdatedTime()).thenReturn(Collections.emptyList());
        when(taskRepository.findByStatus(any())).thenReturn(Collections.emptyList());
        when(taskRepository.findByTargetCluster(anyString())).thenReturn(Collections.emptyList());

        // When & Then
        assertTrue(taskService.getAllTasks().isEmpty());
        assertTrue(taskService.getTasksByStatus(Task.TaskStatus.QUEUED).isEmpty());
        assertTrue(taskService.getTasksByCluster("non-existent").isEmpty());

        verify(taskRepository, times(1)).findAllOrderByUpdatedTime();
        verify(taskRepository, times(1)).findByStatus(Task.TaskStatus.QUEUED);
        verify(taskRepository, times(1)).findByTargetCluster("non-existent");
    }

    @Test
    @DisplayName("测试所有任务状态")
    void testAllTaskStatuses() {
        // Given
        when(taskRepository.findByStatus(Task.TaskStatus.QUEUED)).thenReturn(Collections.emptyList());
        when(taskRepository.findByStatus(Task.TaskStatus.RUNNING)).thenReturn(Collections.emptyList());
        when(taskRepository.findByStatus(Task.TaskStatus.FAILED)).thenReturn(Collections.emptyList());
        when(taskRepository.findByStatus(Task.TaskStatus.COMPLETED)).thenReturn(Collections.emptyList());

        // When & Then
        assertTrue(taskService.getTasksByStatus(Task.TaskStatus.QUEUED).isEmpty());
        assertTrue(taskService.getTasksByStatus(Task.TaskStatus.RUNNING).isEmpty());
        assertTrue(taskService.getTasksByStatus(Task.TaskStatus.FAILED).isEmpty());
        assertTrue(taskService.getTasksByStatus(Task.TaskStatus.COMPLETED).isEmpty());

        verify(taskRepository, times(1)).findByStatus(Task.TaskStatus.QUEUED);
        verify(taskRepository, times(1)).findByStatus(Task.TaskStatus.RUNNING);
        verify(taskRepository, times(1)).findByStatus(Task.TaskStatus.FAILED);
        verify(taskRepository, times(1)).findByStatus(Task.TaskStatus.COMPLETED);
    }
}