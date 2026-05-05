package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.Provider;
import com.clinic.clinic_appointment_system.repository.ProviderRepository;
import com.clinic.clinic_appointment_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    public ProviderService(UserRepository userRepository, ProviderRepository providerRepository) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
    }

    public void createProvider(Provider provider) {

        Long userId = userRepository.save(provider);

        provider.setId(userId);

        providerRepository.save(provider);
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }
}