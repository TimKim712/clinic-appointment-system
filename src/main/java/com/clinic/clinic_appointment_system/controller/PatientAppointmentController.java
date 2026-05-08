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
 * Patient-specific endpoints for managing their appointments.
 */
@RestController
@RequestMapping("/api/patients/{patientId}/appointments")
public class PatientAppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(PatientAppointmentController.class);

    private final AppointmentService appointmentService;

    public PatientAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Get all appointments for a specific patient
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> getPatientAppointments(@PathVariable Long patientId) {
        logger.info("API: Patient {} fetching their appointments", patientId);
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
        logger.info("API: Found {} appointments for patient {}", appointments.size(), patientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Cancel an appointment (patient action)
     */
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long patientId,
            @PathVariable Long appointmentId) {
        
        logger.info("API: Patient {} attempting to cancel appointment {}", patientId, appointmentId);
        
        try {
            appointmentService.cancelAppointment(appointmentId, patientId);
            logger.info("API: Patient {} successfully cancelled appointment {}", patientId, appointmentId);
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("API: Patient {} failed to cancel appointment {}: {}", 
                       patientId, appointmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("API: Error cancelling appointment {} for patient {}", appointmentId, patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel appointment"));
        }
    }
}