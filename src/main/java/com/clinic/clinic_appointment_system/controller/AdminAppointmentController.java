package com.clinic.clinic_appointment_system.controller;

import com.clinic.clinic_appointment_system.model.Appointment;
import com.clinic.clinic_appointment_system.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for monitoring all appointments in the system.
 * Provides detailed views including patient and provider information.
 */
@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAppointmentController.class);

    private final AppointmentService appointmentService;

    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Get all appointments in the system (admin monitoring)
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> monitorAllAppointments() {
        logger.info("API: Admin monitoring all appointments");
        List<Appointment> appointments = appointmentService.getAllAppointments();
        logger.info("API: Admin retrieved {} appointments for monitoring", appointments.size());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get detailed information about a specific appointment
     * Includes patient details, provider details, service info, and timing
     */
    @GetMapping("/{appointmentId}/details")
    public ResponseEntity<?> getAppointmentDetails(@PathVariable Long appointmentId) {
        logger.info("API: Admin requesting details for appointment {}", appointmentId);
        
        try {
            Appointment appointment = appointmentService.getAppointmentDetails(appointmentId);
            
            if (appointment == null) {
                logger.warn("API: Appointment {} not found", appointmentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Appointment not found"));
            }
            
            logger.info("API: Admin retrieved details for appointment {}", appointmentId);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            logger.error("API: Error retrieving details for appointment {}", appointmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve appointment details"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAppointmentStats() {
        logger.info("API: Admin requesting appointment statistics");
        
        try {
            List<Appointment> allAppointments = appointmentService.getAllAppointments();
            
            Map<String, Object> stats = Map.of(
                "totalAppointments", allAppointments.size(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            logger.info("API: Admin retrieved statistics: {} total appointments", allAppointments.size());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("API: Error retrieving appointment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics"));
        }
    }
}