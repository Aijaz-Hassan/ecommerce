package com.ecommerce.store.service;

import com.ecommerce.store.dto.auth.AuthResponse;
import com.ecommerce.store.dto.auth.LoginRequest;
import com.ecommerce.store.dto.auth.RegisterRequest;
import com.ecommerce.store.dto.auth.UserResponse;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.UserRepository;
import com.ecommerce.store.security.JwtService;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setName(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        boolean adminExists = userRepository.existsByRoleIn(List.of("ROLE_ADMIN", "ADMIN"));
        user.setRole(adminExists ? "ROLE_CUSTOMER" : "ROLE_ADMIN");

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.withUsername(savedUser.getEmail())
                .password(savedUser.getPassword())
                .roles(savedUser.getRole().replace("ROLE_", ""))
                .build(),
            Map.of("role", savedUser.getRole(), "fullName", savedUser.getFullName())
        );
        return new AuthResponse(token, savedUser.getFullName(), savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build(),
            Map.of("role", user.getRole(), "fullName", user.getFullName())
        );

        return new AuthResponse(token, user.getFullName(), user.getEmail(), user.getRole());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole()))
            .toList();
    }

    public void deleteUser(Long userId, String currentUserEmail) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new BadRequestException("You cannot delete your own admin account");
        }

        if (List.of("ROLE_ADMIN", "ADMIN").contains(user.getRole())) {
            long adminCount = userRepository.countByRoleIn(List.of("ROLE_ADMIN", "ADMIN"));
            if (adminCount <= 1) {
                throw new BadRequestException("At least one admin user must remain");
            }
        }

        userRepository.delete(user);
    }
}
