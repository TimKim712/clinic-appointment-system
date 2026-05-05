package com.clinic.clinic_appointment_system.controller;

import com.clinic.clinic_appointment_system.model.NotificationRequest;
import com.clinic.clinic_appointment_system.model.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Mock external Notification Service.
 *
 * In a real distributed system this would live in its own deployable artifact
 * (JAR / container) with its own database, scaling policy, and team ownership.
 * For this deliverable it is co-located in the same Spring Boot application to
 * make local demonstration straightforward, but it intentionally models a
 * separate bounded context:
 *
 *   • It has no direct dependency on any class in the appointment scheduler.
 *   • It communicates only through the coarse-grained HTTP contract defined by
 *     NotificationRequest / NotificationResponse.
 *   • Its single endpoint (/mock/notify) represents the full capability of the
 *     notification boundary — callers never need to know whether notifications
 *     are delivered by e-mail, SMS, or push notification.
 *
 * Coarse-grained interface justification:
 *   POST /mock/notify  — one endpoint handles the entire "notify patient"
 *   use-case. A fine-grained alternative would require the caller to:
 *     1. POST /notifications/create  (create a notification record)
 *     2. PATCH /notifications/{id}/recipient  (set recipient)
 *     3. PATCH /notifications/{id}/content    (set message body)
 *     4. POST  /notifications/{id}/send       (trigger delivery)
 *   That would couple the scheduler tightly to the notification service's
 *   internal state machine and quadruple network round-trips.  One coarse call
 *   keeps the boundary clean and the interaction efficient.
 */
@RestController
@RequestMapping("/mock")
public class MockNotificationController {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationController.class);

    /**
     * Simulate sending an appointment-confirmation notification.
     *
     * Accepts all appointment data in a single request body; returns an opaque
     * message ID and a status string.  The caller (AppointmentController) does
     * not need to know how the message is ultimately delivered.
     *
     * @param request  coarse-grained notification payload
     * @return         HTTP 200 with NotificationResponse
     */
    @PostMapping("/notify")
    public ResponseEntity<NotificationResponse> notify(@RequestBody NotificationRequest request) {

        // ── Validate ─────────────────────────────────────────────────────────
        if (request.getPatientEmail() == null || request.getPatientEmail().isBlank()) {
            log.warn("Notification rejected: missing patient e-mail");
            return ResponseEntity.badRequest()
                    .body(new NotificationResponse("FAILED", null,
                            "Patient e-mail is required"));
        }

        // ── Simulate delivery (stub) ──────────────────────────────────────────
        String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("[MOCK NOTIFICATION SERVICE] Sending confirmation email");
        log.info("To {}", request.getPatientEmail());
        log.info("Patient : {}", request.getPatientName());
        log.info("  Provider : {}", request.getProviderName());
        log.info("  Service  : {}", request.getServiceName());
        log.info("  DateTime : {}", request.getAppointmentDateTime());
        log.info("  Appt ID  : {}", request.getAppointmentId());
        log.info("  MessageID: {}", messageId);

        // In a real implementation this would delegate to an SMTP client,
        // an SMS gateway, or a push-notification broker.  The caller is
        // shielded from all of that complexity.

        return ResponseEntity.ok(
                new NotificationResponse(
                        "SENT",
                        messageId,
                        "Confirmation sent to " + request.getPatientEmail()
                )
        );
    }
}
