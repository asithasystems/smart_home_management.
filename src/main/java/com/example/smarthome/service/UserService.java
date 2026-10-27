package com.example.smarthome.service;

import com.example.smarthome.dto.RegistrationDto;
import com.example.smarthome.entity.AppUser;
import com.example.smarthome.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        if (username == null) {
            return false;
        }
        return userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    @Transactional
    public AppUser registerNewUser(RegistrationDto registrationDto) {
        String trimmedUsername = registrationDto.getUsername().trim();
        if (usernameExists(trimmedUsername)) {
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken.");
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        AppUser user = new AppUser(trimmedUsername, encodedPassword);
        return userRepository.save(user);
    }
}
