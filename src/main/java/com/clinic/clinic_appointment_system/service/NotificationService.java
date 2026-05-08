package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Mock remote service that simulates sending notifications to external systems.
 * In a real application, this would call an external email/SMS service.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final Random random = new Random();

    /**
     * Simulates sending appointment confirmation to external notification service.
     * Includes artificial delay and occasional failures to demonstrate remote service behavior.
     */
    public void sendAppointmentConfirmation(Appointment appointment) {
        logger.info("Calling remote notification service for appointment {}", appointment.getId());
        
        // Simulate network delay (100-500ms)
        try {
            int delay = 100 + random.nextInt(400);
            Thread.sleep(delay);
            logger.debug("Remote service call took {}ms", delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Remote service call interrupted", e);
            throw new RuntimeException("Notification service interrupted", e);
        }

        // Simulate occasional service failures (10% failure rate)
        if (random.nextInt(10) == 0) {
            logger.error("Remote notification service temporarily unavailable for appointment {}", 
                        appointment.getId());
            throw new RuntimeException("Remote notification service unavailable");
        }

        logger.info("Successfully sent notification for appointment {} to patient {} and provider {}", 
                    appointment.getId(), appointment.getPatientId(), appointment.getProviderId());
    }

    /**
     * Simulates sending cancellation notification.
     */
    public void sendCancellationNotification(Long appointmentId, Long patientId, Long providerId) {
        logger.info("Sending cancellation notification for appointment {}", appointmentId);
        
        try {
            Thread.sleep(100 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Notification service interrupted", e);
        }

        logger.info("Cancellation notification sent for appointment {}", appointmentId);
    }
}
