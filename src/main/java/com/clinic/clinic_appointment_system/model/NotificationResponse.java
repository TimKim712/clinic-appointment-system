package com.clinic.clinic_appointment_system.model;

/**
 * Coarse-grained response DTO returned by the external Notification Service.
 *
 * Returns a status and a unique message ID so the caller can log or audit the
 * notification without needing to understand the internal mechanics of delivery
 * (e-mail, SMS, push, etc.).
 */
public class NotificationResponse {

    private String status;      // "SENT" | "QUEUED" | "FAILED"
    private String messageId;   // opaque identifier for the dispatched notification
    private String detail;      // human-readable message (optional)

    public NotificationResponse() {}

    public NotificationResponse(String status, String messageId, String detail) {
        this.status = status;
        this.messageId = messageId;
        this.detail = detail;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}