package com.clinic.clinic_appointment_system.model;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for application metrics and monitoring
 */
@Configuration
@EnableScheduling
public class MetricsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricsConfiguration.class);
    
    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;

    public MetricsConfiguration(MeterRegistry meterRegistry, JdbcTemplate jdbcTemplate) {
        this.meterRegistry = meterRegistry;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Register JVM memory metrics
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Register JVM thread metrics
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Register processor metrics
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Collect custom business metrics every minute
     * - Total appointments count
     * - Available slots count
     * - Total users count
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void collectBusinessMetrics() {
        try {
            logger.debug("Collecting business metrics");
            
            // Total appointments
            Integer totalAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments", 
                Integer.class
            );
            meterRegistry.gauge("appointments.total", totalAppointments != null ? totalAppointments : 0);
            
            // Available slots
            Integer availableSlots = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM availability_slots WHERE is_booked = false", 
                Integer.class
            );
            meterRegistry.gauge("appointments.slots.available", availableSlots != null ? availableSlots : 0);
            
            // Total users
            Integer totalUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", 
                Integer.class
            );
            meterRegistry.gauge("users.total", totalUsers != null ? totalUsers : 0);
            
            logger.debug("Business metrics collected - Appointments: {}, Available Slots: {}, Users: {}", 
                        totalAppointments, availableSlots, totalUsers);
            
        } catch (Exception e) {
            logger.error("Error collecting business metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log booking performance metrics every 5 minutes
     * Shows average latency, max latency, and percentiles
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes (300,000 ms)
    public void logBookingPerformanceMetrics() {
        try {
            logger.debug("Collecting booking performance metrics");
            
            // Get the booking duration timer
            Timer bookingTimer = meterRegistry.find("appointments.booking.duration")
                .tag("service", "appointment")
                .timer();
            
            if (bookingTimer != null && bookingTimer.count() > 0) {
                // Calculate statistics
                long totalBookings = (long) bookingTimer.count();
                double totalTime = bookingTimer.totalTime(TimeUnit.MILLISECONDS);
                double average = totalTime / totalBookings;
                double maxDuration = bookingTimer.max(TimeUnit.MILLISECONDS);
                
                // Log performance summary
                logger.info("═══════════════════════════════════════════════════════════");
                logger.info("PERFORMANCE_METRICS - Booking Operation Statistics");
                logger.info("───────────────────────────────────────────────────────────");
                logger.info("  Total Bookings: {}", totalBookings);
                logger.info("  Average Latency: {}ms", String.format("%.2f", average));
                logger.info("  Max Latency: {}ms", String.format("%.2f", maxDuration));
                logger.info("  Total Time: {}s", String.format("%.2f", totalTime / 1000));
                
                // Performance assessment
                String performanceLevel;
                if (average < 100) {
                    performanceLevel = "EXCELLENT";
                } else if (average < 300) {
                    performanceLevel = "GOOD";
                } else if (average < 500) {
                    performanceLevel = "ACCEPTABLE";
                } else if (average < 1000) {
                    performanceLevel = "POOR";
                } else {
                    performanceLevel = "CRITICAL";
                }
                logger.info("  Performance Level: {}", performanceLevel);
                logger.info("═══════════════════════════════════════════════════════════");
                
                // Warn if performance is degrading
                if (average > 500) {
                    logger.warn("ALERT: Booking latency is high ({}ms avg). Investigation recommended.", String.format("%.2f", average));
                }
                if (maxDuration > 2000) {
                    logger.warn("ALERT: Maximum booking latency is very high ({}ms). Check for slow queries or lock contention.", String.format("%.2f", maxDuration));
                }
            } else {
                logger.debug("No booking operations recorded yet");
            }
            
        } catch (Exception e) {
            logger.error("Error collecting performance metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log comprehensive system metrics every 10 minutes
     * Provides a complete health snapshot
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void logComprehensiveMetrics() {
        try {
            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("SYSTEM_HEALTH_SNAPSHOT - Complete Metrics Overview");
            logger.info("───────────────────────────────────────────────────────────");
            
            // Business metrics
            Integer totalAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments", Integer.class);
            Integer availableSlots = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM availability_slots WHERE is_booked = false", Integer.class);
            Integer totalUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class);
            
            logger.info("Business Metrics:");
            logger.info("  Total Appointments: {}", totalAppointments);
            logger.info("  Available Slots: {}", availableSlots);
            logger.info("  Total Users: {}", totalUsers);
            
            // Booking counters
            Double successfulBookings = meterRegistry.find("appointments.bookings.success")
                .counter() != null ? meterRegistry.find("appointments.bookings.success").counter().count() : 0;
            Double failedBookings = meterRegistry.find("appointments.bookings.failed")
                .counter() != null ? meterRegistry.find("appointments.bookings.failed").counter().count() : 0;
            
            double failureRate = (successfulBookings + failedBookings) > 0 
                ? (failedBookings / (successfulBookings + failedBookings)) * 100 
                : 0;
            
            logger.info("Booking Statistics:");
            logger.info("  Successful: {}", successfulBookings.longValue());
            logger.info("  Failed: {}", failedBookings.longValue());
            logger.info("  Failure Rate: {}%", String.format("%.2f", failureRate));
            
            // Performance metrics
            Timer bookingTimer = meterRegistry.find("appointments.booking.duration")
                .tag("service", "appointment").timer();
            
            if (bookingTimer != null && bookingTimer.count() > 0) {
                double avgLatency = bookingTimer.totalTime(TimeUnit.MILLISECONDS) / bookingTimer.count();
                logger.info("Performance:");
                logger.info("  Avg Latency: {}ms", String.format("%.2f", avgLatency));
                logger.info("  Max Latency: {}ms", String.format("%.2f", bookingTimer.max(TimeUnit.MILLISECONDS)));
            }
            
            // System resources
            Runtime runtime = Runtime.getRuntime();
            long freeMemoryMB = runtime.freeMemory() / (1024 * 1024);
            long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
            long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
            long usedMemoryMB = totalMemoryMB - freeMemoryMB;
            double memoryUsagePercent = (double) usedMemoryMB / maxMemoryMB * 100;
            
            logger.info("System Resources:");
            logger.info("  Memory: {}MB / {}MB ({}% used)", usedMemoryMB, maxMemoryMB, String.format("%.1f", memoryUsagePercent));
            logger.info("  Free Memory: {}MB", freeMemoryMB);
            logger.info("  CPU Cores: {}", runtime.availableProcessors());
            
            logger.info("═══════════════════════════════════════════════════════════");
            
            // Warnings
            if (availableSlots != null && availableSlots < 10) {
                logger.warn("ALERT: Low available slots ({}). Consider creating more appointment slots.", availableSlots);
            }
            if (failureRate > 10) {
                logger.warn("ALERT: High booking failure rate ({}%). Investigation needed.", String.format("%.2f", failureRate));
            }
            if (memoryUsagePercent > 80) {
                logger.warn("ALERT: High memory usage ({}%). Consider increasing heap size or investigating memory leaks.", String.format("%.1f", memoryUsagePercent));
            }
            
        } catch (Exception e) {
            logger.error("Error collecting comprehensive metrics: {}", e.getMessage(), e);
        }
    }
}