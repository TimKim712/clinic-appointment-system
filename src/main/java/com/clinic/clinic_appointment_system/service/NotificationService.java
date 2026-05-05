package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.NotificationRequest;
import com.clinic.clinic_appointment_system.model.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Client façade that hides all HTTP details from the rest of the application.
 *
 * Design rationale (coarse-grained interface):
 *   • A single method — sendAppointmentConfirmation() — is the only way the
 *     main application interacts with the Notification Service.
 *   • All appointment data travels in one HTTP request (NotificationRequest).
 *     There are no preliminary calls to "open a session", "set the recipient",
 *     or "set the subject" separately; everything is bundled.
 *   • The method signature is deliberately high-level. Callers provide
 *     business concepts (patient name, service name, date/time) rather than
 *     low-level primitives (SMTP headers, template IDs, etc.).
 *   • Changing the notification transport (e-mail → SMS → push) requires only
 *     a change inside the Notification Service — the interface stays the same.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RestTemplate restTemplate;

    /**
     * Base URL of the (mock) Notification Service.
     * Defaults to localhost so the mock controller works out-of-the-box.
     * Override via application.properties:
     *   notification.service.url=https://real-notification-service.example.com
     */
    @Value("${notification.service.url:http://localhost:8080}")
    private String notificationServiceUrl;

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Single coarse-grained operation exposed to the rest of the application.
     *
     * @param request  all data needed to notify the patient in one object
     * @return         the service's response (status + opaque message ID)
     */
    public NotificationResponse sendAppointmentConfirmation(NotificationRequest request) {

        String endpoint = notificationServiceUrl + "/mock/notify";

        log.info("Calling Notification Service at {} for appointment {}",
                 endpoint, request.getAppointmentId());

        try {
            NotificationResponse response =
                    restTemplate.postForObject(endpoint, request, NotificationResponse.class);

            log.info("Notification Service responded with status={} messageId={}",
                     response != null ? response.getStatus()    : "null",
                     response != null ? response.getMessageId() : "null");

            return response;

        } catch (Exception ex) {
            // Notification failures are non-fatal; the appointment is already booked.
            // Log the error and return a FAILED response so the caller can decide.
            log.error("Notification Service call failed: {}", ex.getMessage());
            return new NotificationResponse("FAILED", null,
                    "Notification service unreachable: " + ex.getMessage());
        }
    }
}
