package com.clinic.clinic_appointment_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    
    private final JdbcTemplate jdbcTemplate;

    public HealthCheckController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Basic health check endpoint
     * Returns: status (UP/DOWN), database status, timestamp, version
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;
        
        // Check database connectivity
        Map<String, String> databaseStatus = checkDatabase();
        health.put("database", databaseStatus);
        
        if ("DOWN".equals(databaseStatus.get("status"))) {
            isHealthy = false;
            logger.warn("Health check failed - Database is down");
        }
        
        // Build response
        health.put("status", isHealthy ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        
        HttpStatus httpStatus = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        
        logger.info("Health check completed - Status: {}", health.get("status"));
        
        return new ResponseEntity<>(health, httpStatus);
    }
    
    /**
     * Detailed health check with component info
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        logger.debug("Detailed health check requested");
        
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;
        
        // Check database
        Map<String, String> databaseStatus = checkDatabase();
        health.put("database", databaseStatus);
        if ("DOWN".equals(databaseStatus.get("status"))) {
            isHealthy = false;
        }
        
        // Check critical tables
        Map<String, Object> tableStatus = checkCriticalTables();
        health.put("tables", tableStatus);
        if (!"OK".equals(tableStatus.get("status"))) {
            isHealthy = false;
        }
        
        // System metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("availableProcessors", runtime.availableProcessors());
        systemMetrics.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        systemMetrics.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        systemMetrics.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        health.put("system", systemMetrics);
        
        // Overall status
        health.put("status", isHealthy ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        
        HttpStatus httpStatus = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        
        logger.info("Detailed health check completed - Status: {}", health.get("status"));
        
        return new ResponseEntity<>(health, httpStatus);
    }
    
    /**
     * Check database connectivity
     */
    private Map<String, String> checkDatabase() {
        Map<String, String> status = new HashMap<>();
        try {
            // Execute simple query
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("status", "UP");
            status.put("message", "Database connection successful");
            logger.debug("Database health check passed");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("message", "Database connection failed: " + e.getMessage());
            logger.error("Database health check failed: {}", e.getMessage(), e);
        }
        return status;
    }
    
    /**
     * Check critical tables are accessible
     */
    private Map<String, Object> checkCriticalTables() {
        Map<String, Object> status = new HashMap<>();
        try {
            // Verify each critical table
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM availability_slots", Integer.class);
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments", Integer.class);
            
            status.put("status", "OK");
            status.put("message", "All critical tables accessible");
            logger.debug("Critical tables health check passed");
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("message", "Error accessing critical tables: " + e.getMessage());
            logger.error("Critical tables health check failed: {}", e.getMessage(), e);
        }
        return status;
    }
}