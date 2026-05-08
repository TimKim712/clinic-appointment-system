package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.Appointment;
import com.clinic.clinic_appointment_system.model.AvailabilitySlot;
import com.clinic.clinic_appointment_system.repository.AppointmentRepository;
import com.clinic.clinic_appointment_system.repository.AvailabilitySlotRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final NotificationService notificationService;
    private final MeterRegistry meterRegistry;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AvailabilitySlotRepository slotRepository,
                              NotificationService notificationService,
                              MeterRegistry meterRegistry) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.notificationService = notificationService;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Appointment bookAppointment(Appointment appointment) {
        logger.info("Attempting to book appointment for patient {} with provider {} for slot {}",
                    appointment.getPatientId(), appointment.getProviderId(), appointment.getSlotId());

        Timer.Sample sample = Timer.start(meterRegistry);

        AvailabilitySlot slot = slotRepository.findByIdForUpdate(appointment.getSlotId());

        if (slot.isBooked()) {
            logger.warn("Slot {} is already booked", appointment.getSlotId());
            meterRegistry.counter("appointments.bookings.failed").increment();
            throw new IllegalStateException("Slot " + appointment.getSlotId() + " is already booked.");
        }

        Long appointmentId = appointmentRepository.save(appointment);
        appointment.setId(appointmentId);

        int rowsUpdated = slotRepository.markSlotBooked(slot.getId(), slot.getVersion());

        if (rowsUpdated == 0) {
            logger.error("Optimistic locking failure for slot {}", slot.getId());
            meterRegistry.counter("appointments.bookings.failed").increment();
            throw new IllegalStateException("Slot was booked by another user. Please choose a different time.");
        }

        logger.info("Successfully booked appointment {} for slot {}", appointmentId, slot.getId());
        meterRegistry.counter("appointments.bookings.success").increment();
        sample.stop(meterRegistry.timer("appointments.booking.duration", "service", "appointment"));

        Appointment booked = appointmentRepository.findById(appointmentId);

        try {
            notificationService.sendAppointmentConfirmation(booked);
            logger.info("Notification sent for appointment {}", appointmentId);
        } catch (Exception e) {
            logger.error("Failed to send notification for appointment {}: {}", appointmentId, e.getMessage());
        }

        return booked;
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, Long patientId) {
        logger.info("Patient {} attempting to cancel appointment {}", patientId, appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId);
        
        if (appointment == null) {
            logger.warn("Appointment {} not found", appointmentId);
            throw new IllegalArgumentException("Appointment not found");
        }
        
        if (!appointment.getPatientId().equals(patientId)) {
            logger.warn("Patient {} attempted to cancel appointment {} which belongs to patient {}", 
                       patientId, appointmentId, appointment.getPatientId());
            throw new IllegalStateException("You can only cancel your own appointments");
        }

        slotRepository.releaseSlot(appointment.getSlotId());
        appointmentRepository.delete(appointmentId);
        
        logger.info("Successfully cancelled appointment {}", appointmentId);
    }

    @Transactional
    public Appointment createAppointmentByProvider(Appointment appointment, Long providerId) {
        logger.info("Provider {} creating appointment for patient {} in slot {}", 
                    providerId, appointment.getPatientId(), appointment.getSlotId());
        
        if (!appointment.getProviderId().equals(providerId)) {
            logger.warn("Provider {} attempted to create appointment for different provider {}", 
                       providerId, appointment.getProviderId());
            throw new IllegalStateException("Providers can only create appointments for themselves");
        }

        return bookAppointment(appointment);
    }

    @Transactional
    public void deleteAppointmentByProvider(Long appointmentId, Long providerId) {
        logger.info("Provider {} attempting to delete appointment {}", providerId, appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId);
        
        if (appointment == null) {
            logger.warn("Appointment {} not found", appointmentId);
            throw new IllegalArgumentException("Appointment not found");
        }
        
        if (!appointment.getProviderId().equals(providerId)) {
            logger.warn("Provider {} attempted to delete appointment {} which belongs to provider {}", 
                       providerId, appointmentId, appointment.getProviderId());
            throw new IllegalStateException("Providers can only delete their own appointments");
        }

        slotRepository.releaseSlot(appointment.getSlotId());
        appointmentRepository.delete(appointmentId);
        
        logger.info("Successfully deleted appointment {}", appointmentId);
    }

    public List<Appointment> getAllAppointments() {
        logger.info("Retrieving all appointments");
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByProvider(Long providerId) {
        logger.info("Retrieving appointments for provider {}", providerId);
        return appointmentRepository.findByProviderId(providerId);
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        logger.info("Retrieving appointments for patient {}", patientId);
        return appointmentRepository.findByPatientId(patientId);
    }

    public Appointment getAppointmentDetails(Long appointmentId) {
        logger.info("Retrieving details for appointment {}", appointmentId);
        return appointmentRepository.findByIdWithDetails(appointmentId);
    }
}