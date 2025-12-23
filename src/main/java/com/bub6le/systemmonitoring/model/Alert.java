package com.bub6le.systemmonitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source")
    private String source;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private AlertSeverity severity;
    
    @Column(name = "message")
    private String message;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "resolved")
    private Boolean resolved;
    
    // Constructors
    public Alert() {
        this.timestamp = LocalDateTime.now();
        this.resolved = false;
    }
    
    public Alert(String source, AlertSeverity severity, String message) {
        this();
        this.source = source;
        this.severity = severity;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public AlertSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Boolean getResolved() {
        return resolved;
    }
    
    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }
    
    // Alert Severity Enum
    public enum AlertSeverity {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("严重");
        
        private final String description;
        
        AlertSeverity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}