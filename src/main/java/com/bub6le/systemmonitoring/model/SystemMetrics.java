package com.bub6le.systemmonitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics")
public class SystemMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "server_name")
    private String serverName;
    
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    @Column(name = "memory_usage")
    private Double memoryUsage;
    
    @Column(name = "disk_usage")
    private Double diskUsage;
    
    @Column(name = "network_in")
    private Double networkIn;
    
    @Column(name = "network_out")
    private Double networkOut;
    
    @Column(name = "load_average")
    private Double loadAverage;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "region")
    private String region;
    
    @Column(name = "service_type")
    private String serviceType;
    
    // Constructors
    public SystemMetrics() {
        this.timestamp = LocalDateTime.now();
    }
    
    public SystemMetrics(String serverName, Double cpuUsage, Double memoryUsage, 
                        Double diskUsage, Double networkIn, Double networkOut, 
                        Double loadAverage, String region, String serviceType) {
        this();
        this.serverName = serverName;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.networkIn = networkIn;
        this.networkOut = networkOut;
        this.loadAverage = loadAverage;
        this.region = region;
        this.serviceType = serviceType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public Double getCpuUsage() {
        return cpuUsage;
    }
    
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public Double getMemoryUsage() {
        return memoryUsage;
    }
    
    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    
    public Double getDiskUsage() {
        return diskUsage;
    }
    
    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }
    
    public Double getNetworkIn() {
        return networkIn;
    }
    
    public void setNetworkIn(Double networkIn) {
        this.networkIn = networkIn;
    }
    
    public Double getNetworkOut() {
        return networkOut;
    }
    
    public void setNetworkOut(Double networkOut) {
        this.networkOut = networkOut;
    }
    
    public Double getLoadAverage() {
        return loadAverage;
    }
    
    public void setLoadAverage(Double loadAverage) {
        this.loadAverage = loadAverage;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}