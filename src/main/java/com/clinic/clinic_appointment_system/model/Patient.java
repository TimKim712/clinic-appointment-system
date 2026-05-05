package com.clinic.clinic_appointment_system.model;

public class Patient extends User {

    private String email;

    public Patient() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}