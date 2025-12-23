package com.bub6le.systemmonitoring.repository;

import com.bub6le.systemmonitoring.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByStatus(Task.TaskStatus status);
    
    List<Task> findByTargetCluster(String targetCluster);
    
    @Query("SELECT t FROM Task t ORDER BY t.updatedTime DESC")
    List<Task> findAllOrderByUpdatedTime();
    
    @Query("SELECT t FROM Task t WHERE t.status IN :statuses ORDER BY t.updatedTime DESC")
    List<Task> findByStatusInOrderByUpdatedTime(List<Task.TaskStatus> statuses);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Long countByStatus(Task.TaskStatus status);
}