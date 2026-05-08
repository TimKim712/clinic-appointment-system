package com.clinic.clinic_appointment_system.model;

/**
 * Coarse-grained request DTO sent to the external Notification Service.
 */
public class NotificationRequest {

    private Long   appointmentId;
    private String patientEmail;
    private String patientName;
    private String providerName;
    private String serviceName;
    private String appointmentDateTime; 

    public NotificationRequest() {}

    public NotificationRequest(Long appointmentId, String patientEmail, String patientName, String providerName, String serviceName, String appointmentDateTime) {
        this.appointmentId = appointmentId;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.providerName = providerName;
        this.serviceName = serviceName;
        this.appointmentDateTime = appointmentDateTime;
    }

    public Long getAppointmentId() { 
        return appointmentId; 
    }

    public void setAppointmentId(Long appointmentId) { 
        this.appointmentId = appointmentId; 
    }

    public String getPatientEmail() { 
        return patientEmail; 
    }

    public void setPatientEmail(String patientEmail) { 
        this.patientEmail = patientEmail; 
    }

    public String getPatientName() { 
        return patientName; 
    }

    public void setPatientName(String patientName) { 
        this.patientName = patientName; 
    }

    public String getProviderName() { 
        return providerName; 
    }

    public void setProviderName(String providerName) { 
        this.providerName = providerName; 
    }

    public String getServiceName() { 
        return serviceName; 
    }

    public void setServiceName(String serviceName) { 
        this.serviceName = serviceName; 
    }

    public String getAppointmentDateTime() { 
        return appointmentDateTime; 
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
}
