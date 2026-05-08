package com.clinic.clinic_appointment_system.model;

/**
 * Returns a status and a unique message ID so the caller can log or audit the
 * notification without needing to understand the internal mechanics of delivery
 * (e-mail, SMS, push, etc.).
 */
public class NotificationResponse {

    private String status;  
    private String messageId;   
    private String detail;      

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