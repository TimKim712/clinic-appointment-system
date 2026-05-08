package com.clinic.clinic_appointment_system.controller;

import com.clinic.clinic_appointment_system.model.NotificationRequest;
import com.clinic.clinic_appointment_system.model.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/mock")
public class MockNotificationController {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationController.class);

    /**
     * Simulate sending an appointment-confirmation notification.
     */
    @PostMapping("/notify")
    public ResponseEntity<NotificationResponse> notify(@RequestBody NotificationRequest request) {

        
        if (request.getPatientEmail() == null || request.getPatientEmail().isBlank()) {
            log.warn("Notification rejected: missing patient e-mail");
            return ResponseEntity.badRequest()
                    .body(new NotificationResponse("FAILED", null,
                            "Patient e-mail is required"));
        }

        String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("[MOCK NOTIFICATION SERVICE] Sending confirmation email");
        log.info("To {}", request.getPatientEmail());
        log.info("Patient : {}", request.getPatientName());
        log.info("  Provider : {}", request.getProviderName());
        log.info("  Service  : {}", request.getServiceName());
        log.info("  DateTime : {}", request.getAppointmentDateTime());
        log.info("  Appt ID  : {}", request.getAppointmentId());
        log.info("  MessageID: {}", messageId);

        return ResponseEntity.ok(
                new NotificationResponse(
                        "SENT",
                        messageId,
                        "Confirmation sent to " + request.getPatientEmail()
                )
        );
    }
}
