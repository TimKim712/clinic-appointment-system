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
 * Provider-specific endpoints for managing their own appointments.
 */
@RestController
@RequestMapping("/api/providers/{providerId}/appointments")
public class ProviderAppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAppointmentController.class);

    private final AppointmentService appointmentService;

    public ProviderAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Get all appointments for a specific provider
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> getProviderAppointments(@PathVariable Long providerId) {
        logger.info("API: Provider {} fetching their appointments", providerId);
        List<Appointment> appointments = appointmentService.getAppointmentsByProvider(providerId);
        logger.info("API: Found {} appointments for provider {}", appointments.size(), providerId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Create a new appointment
     */
    @PostMapping
    public ResponseEntity<?> createAppointment(
            @PathVariable Long providerId,
            @RequestBody Appointment appointment) {
        
        logger.info("API: Provider {} creating appointment for patient {} in slot {}", 
                   providerId, appointment.getPatientId(), appointment.getSlotId());
        
        try {
            Appointment created = appointmentService.createAppointmentByProvider(appointment, providerId);
            logger.info("API: Provider {} successfully created appointment {}", providerId, created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException e) {
            logger.warn("API: Provider {} failed to create appointment: {}", providerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("API: Error creating appointment for provider {}", providerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create appointment"));
        }
    }

    /**
     * Delete an appointment
     */
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> deleteAppointment(
            @PathVariable Long providerId,
            @PathVariable Long appointmentId) {
        
        logger.info("API: Provider {} attempting to delete appointment {}", providerId, appointmentId);
        
        try {
            appointmentService.deleteAppointmentByProvider(appointmentId, providerId);
            logger.info("API: Provider {} successfully deleted appointment {}", providerId, appointmentId);
            return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("API: Provider {} failed to delete appointment {}: {}", 
                       providerId, appointmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("API: Error deleting appointment {} for provider {}", appointmentId, providerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete appointment"));
        }
    }
}