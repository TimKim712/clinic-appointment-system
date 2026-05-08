package com.clinic.clinic_appointment_system.controller;

import com.clinic.clinic_appointment_system.model.Patient;
import com.clinic.clinic_appointment_system.model.Provider;
import com.clinic.clinic_appointment_system.repository.PatientRepository;
import com.clinic.clinic_appointment_system.repository.ProviderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserDirectoryController {

    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;

    public UserDirectoryController(PatientRepository patientRepository,
                                   ProviderRepository providerRepository) {
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> listPatients() {
        return ResponseEntity.ok(patientRepository.findAllPublic());
    }

    @GetMapping("/providers")
    public ResponseEntity<List<Provider>> listProviders() {
        return ResponseEntity.ok(providerRepository.findAllPublic());
    }
}
