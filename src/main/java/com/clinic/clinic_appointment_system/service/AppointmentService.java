package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.Appointment;
import com.clinic.clinic_appointment_system.model.AvailabilitySlot;
import com.clinic.clinic_appointment_system.repository.AppointmentRepository;
import com.clinic.clinic_appointment_system.repository.AvailabilitySlotRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final Counter successfulBookingsCounter;
    private final Counter failedBookingsCounter;
    private final Timer bookingTimer;
    private final MeterRegistry meterRegistry;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AvailabilitySlotRepository slotRepository,
                              MeterRegistry meterRegistry) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics
        this.successfulBookingsCounter = Counter.builder("appointments.bookings.success")
                .description("Number of successful appointment bookings")
                .tag("service", "appointment")
                .register(meterRegistry);
                
        this.failedBookingsCounter = Counter.builder("appointments.bookings.failed")
                .description("Number of failed appointment bookings")
                .tag("service", "appointment")
                .register(meterRegistry);
                
        this.bookingTimer = Timer.builder("appointments.booking.duration")
                .description("Time taken to book an appointment")
                .tag("service", "appointment")
                .register(meterRegistry);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void bookAppointment(Appointment appointment) {
        
        // Start timing the booking operation
        Timer.Sample sample = Timer.start();
        long startTime = System.currentTimeMillis();
        
        logger.info("Attempting to book appointment - PatientId: {}, ProviderId: {}, SlotId: {}", 
                    appointment.getPatientId(), 
                    appointment.getProviderId(), 
                    appointment.getSlotId());

        try {
            // Acquire slot with pessimistic lock
            logger.debug("Acquiring lock for slot: {}", appointment.getSlotId());
            AvailabilitySlot slot = slotRepository.findByIdForUpdate(appointment.getSlotId());

            // Check if slot is already booked
            if (slot.isBooked()) {
                logger.warn("Booking attempt failed - Slot {} is already booked. PatientId: {}", 
                           appointment.getSlotId(), 
                           appointment.getPatientId());
                failedBookingsCounter.increment();
                throw new IllegalStateException("Slot " + appointment.getSlotId() + " is already booked.");
            }

            // Save the appointment
            logger.debug("Saving appointment record for PatientId: {}, SlotId: {}", 
                        appointment.getPatientId(), 
                        appointment.getSlotId());
            appointmentRepository.save(appointment);

            // Mark slot as booked using optimistic locking
            logger.debug("Marking slot {} as booked (version: {})", 
                        slot.getId(), 
                        slot.getVersion());
            int rowsUpdated = slotRepository.markSlotBooked(slot.getId(), slot.getVersion());

            if (rowsUpdated == 0) {
                logger.error("Concurrent booking conflict detected - Slot {} was modified by another transaction. PatientId: {}", 
                            slot.getId(), 
                            appointment.getPatientId());
                failedBookingsCounter.increment();
                throw new IllegalStateException("Slot was booked by another user. Please choose a different time.");
            }

            // Calculate this operation's duration
            long duration = System.currentTimeMillis() - startTime;
            
            // Get current statistics from the timer
            long totalBookings = (long) bookingTimer.count();
            double totalTime = bookingTimer.totalTime(TimeUnit.MILLISECONDS);
            double currentAverage = totalBookings > 0 ? totalTime / totalBookings : 0;
            
            // Success - log with duration and current average
            logger.info("Appointment booked successfully - PatientId: {}, ProviderId: {}, SlotId: {} | Duration: {}ms | Avg Latency: {:.2f}ms (based on {} bookings)", 
                       appointment.getPatientId(), 
                       appointment.getProviderId(), 
                       appointment.getSlotId(),
                       duration,
                       currentAverage,
                       totalBookings);
            
            successfulBookingsCounter.increment();
            
        } catch (IllegalStateException e) {
            // Business logic failures (already logged above)
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during booking - PatientId: {}, SlotId: {}, Error: {}", 
                        appointment.getPatientId(), 
                        appointment.getSlotId(), 
                        e.getMessage(), 
                        e);
            failedBookingsCounter.increment();
            throw new RuntimeException("Failed to book appointment due to system error", e);
        } finally {
            // Record the duration of the booking operation
            sample.stop(bookingTimer);
        }
    }

    public List<Appointment> getAllAppointments() {
        logger.debug("Retrieving all appointments");
        try {
            List<Appointment> appointments = appointmentRepository.findAll();
            logger.info("Retrieved {} appointments", appointments.size());
            return appointments;
        } catch (Exception e) {
            logger.error("Error retrieving appointments: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve appointments", e);
        }
    }
    
    /**
     * Get current booking performance statistics
     * Useful for monitoring and debugging
     */
    public void logCurrentStatistics() {
        long totalBookings = (long) bookingTimer.count();
        double totalTime = bookingTimer.totalTime(TimeUnit.MILLISECONDS);
        double average = totalBookings > 0 ? totalTime / totalBookings : 0;
        double maxDuration = bookingTimer.max(TimeUnit.MILLISECONDS);
        
        logger.info("BOOKING_STATS - Total Bookings: {}, Avg Latency: {:.2f}ms, Max: {:.2f}ms", 
                   totalBookings, average, maxDuration);
    }
}