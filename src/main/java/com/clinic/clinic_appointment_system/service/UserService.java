package com.clinic.clinic_appointment_system.service;

import com.clinic.clinic_appointment_system.model.User;
import com.clinic.clinic_appointment_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Long createUser(User user) {
        return userRepository.save(user);
    }
}