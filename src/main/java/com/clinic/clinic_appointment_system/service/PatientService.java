package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.Patient;
import com.clinic.clinic_appointment_system.repository.PatientRepository;
import com.clinic.clinic_appointment_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public PatientService(UserRepository userRepository, PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    public void createPatient(Patient patient) {

        Long userId = userRepository.save(patient);

        patient.setId(userId);

        patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}