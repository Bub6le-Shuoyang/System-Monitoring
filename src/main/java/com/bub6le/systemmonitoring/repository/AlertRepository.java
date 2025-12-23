package com.bub6le.systemmonitoring.repository;

import com.bub6le.systemmonitoring.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    List<Alert> findBySource(String source);
    
    List<Alert> findBySeverity(Alert.AlertSeverity severity);
    
    List<Alert> findByResolved(Boolean resolved);
    
    @Query("SELECT a FROM Alert a ORDER BY a.timestamp DESC")
    List<Alert> findAllOrderByTimestamp();
    
    @Query("SELECT a FROM Alert a WHERE a.resolved = false ORDER BY a.timestamp DESC")
    List<Alert> findUnresolvedAlerts();
    
    @Query("SELECT a FROM Alert a WHERE a.severity = :severity AND a.resolved = false ORDER BY a.timestamp DESC")
    List<Alert> findUnresolvedAlertsBySeverity(Alert.AlertSeverity severity);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.resolved = false")
    Long countUnresolvedAlerts();
}