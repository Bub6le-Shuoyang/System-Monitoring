package com.bub6le.systemmonitoring.repository;

import com.bub6le.systemmonitoring.model.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {
    
    List<SystemMetrics> findByServerName(String serverName);
    
    List<SystemMetrics> findByRegion(String region);
    
    List<SystemMetrics> findByServiceType(String serviceType);
    
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.timestamp >= :startTime ORDER BY sm.timestamp DESC")
    List<SystemMetrics> findRecentMetrics(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.serverName = :serverName AND sm.timestamp >= :startTime ORDER BY sm.timestamp DESC")
    List<SystemMetrics> findRecentMetricsByServer(@Param("serverName") String serverName, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(sm.cpuUsage) FROM SystemMetrics sm WHERE sm.timestamp >= :startTime")
    Double getAverageCpuUsage(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(sm.memoryUsage) FROM SystemMetrics sm WHERE sm.timestamp >= :startTime")
    Double getAverageMemoryUsage(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.timestamp >= :startTime ORDER BY sm.timestamp DESC")
    List<SystemMetrics> findLatestMetrics(@Param("startTime") LocalDateTime startTime);
}