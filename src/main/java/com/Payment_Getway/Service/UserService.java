package com.Payment_Getway.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.Payment_Getway.Model.User;
import com.Payment_Getway.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User registerUser(User user) {

        if (Boolean.TRUE.equals(userRepository.existsByEmail(user.getEmail()))) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        user.setCreatedAt(LocalDateTime.now());

        user.setRole("CUSTOMER");

        user.setAccountStatus("ACTIVE");

        // Encrypt Password
        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        return userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    public User getRequiredUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }
}
