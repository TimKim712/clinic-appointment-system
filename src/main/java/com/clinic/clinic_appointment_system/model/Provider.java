package com.clinic.clinic_appointment_system.model;

public class Provider extends User {

    private String specialty;

    public Provider() {}

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}