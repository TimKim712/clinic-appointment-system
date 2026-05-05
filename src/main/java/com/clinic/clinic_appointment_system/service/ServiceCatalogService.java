package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.MedicalService;
import com.clinic.clinic_appointment_system.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<MedicalService> getAllServices() {
        return serviceRepository.findAll();
    }
}