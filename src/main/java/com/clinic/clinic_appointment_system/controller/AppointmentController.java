package com.clinic.clinic_appointment_system.controller;

import com.clinic.clinic_appointment_system.model.Appointment;
import com.clinic.clinic_appointment_system.model.AvailabilitySlot;
import com.clinic.clinic_appointment_system.repository.AvailabilitySlotRepository;
import com.clinic.clinic_appointment_system.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final AvailabilitySlotRepository slotRepository;

    public AppointmentController(AppointmentService appointmentService,
                                AvailabilitySlotRepository slotRepository) {
        this.appointmentService = appointmentService;
        this.slotRepository = slotRepository;
    }

    /**
     * Get all available appointment slots
     */
    @GetMapping("/slots/available")
    public ResponseEntity<List<AvailabilitySlot>> getAvailableSlots() {
        logger.info("API: Fetching available slots");
        List<AvailabilitySlot> slots = slotRepository.findAvailableSlots();
        logger.info("API: Found {} available slots", slots.size());
        return ResponseEntity.ok(slots);
    }

    /**
     * Book a new appointment (patient action)
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment) {
        logger.info("API: Booking appointment for patient {} with provider {} for slot {}", 
                   appointment.getPatientId(), appointment.getProviderId(), appointment.getSlotId());
        
        try {
            Appointment booked = appointmentService.bookAppointment(appointment);
            logger.info("API: Successfully booked appointment {}", booked.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(booked);
        } catch (IllegalStateException e) {
            logger.warn("API: Failed to book appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("API: Error booking appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to book appointment"));
        }
    }

    /**
     * Get all appointments (basic list)
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        logger.info("API: Fetching all appointments");
        List<Appointment> appointments = appointmentService.getAllAppointments();
        logger.info("API: Found {} appointments", appointments.size());
        return ResponseEntity.ok(appointments);
    }
}