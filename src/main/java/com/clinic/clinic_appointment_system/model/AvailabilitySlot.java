package com.clinic.clinic_appointment_system.model;

import java.time.LocalDateTime;

public class AvailabilitySlot {

    private Long id;

    private Long providerId;
    private Long serviceId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int version;

    private boolean booked;

    public AvailabilitySlot() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getVersion() { 
        return version; 
    }
    
    public void setVersion(int version) { 
        this.version = version; 
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}