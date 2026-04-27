package com.ecommerce.store.service;

import com.ecommerce.store.dto.auth.AuthResponse;
import com.ecommerce.store.dto.auth.LoginRequest;
import com.ecommerce.store.dto.auth.RegisterRequest;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.UserRepository;
import com.ecommerce.store.security.JwtService;
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
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRepository.count() == 0 ? "ROLE_ADMIN" : "ROLE_CUSTOMER");

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
}
