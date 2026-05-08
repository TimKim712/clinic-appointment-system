package com.clinic.clinic_appointment_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for monitoring application status.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Basic health check endpoint
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "Clinic Appointment System");
        
        // Check database connectivity
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("database", "UP");
            logger.debug("Database connection healthy");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("status", "DEGRADED");
            logger.error("Database connection failed", e);
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with component status
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        logger.info("Detailed health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", LocalDateTime.now());
        
        Map<String, String> components = new HashMap<>();
        
        // Check database
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            components.put("database", "UP");
            
            // Count records
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            Integer appointmentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments", Integer.class);
            
            health.put("userCount", userCount);
            health.put("appointmentCount", appointmentCount);
            
        } catch (Exception e) {
            components.put("database", "DOWN");
            logger.error("Database health check failed", e);
        }
        
        // Overall status
        boolean allUp = components.values().stream().allMatch(status -> status.equals("UP"));
        health.put("status", allUp ? "UP" : "DEGRADED");
        health.put("components", components);
        
        return ResponseEntity.ok(health);
    }
}