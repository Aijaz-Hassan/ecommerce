package com.ecommerce.store.service;

import com.ecommerce.store.dto.auth.AuthResponse;
import com.ecommerce.store.dto.auth.ForgotPasswordRequest;
import com.ecommerce.store.dto.auth.LoginRequest;
import com.ecommerce.store.dto.auth.RegisterRequest;
import com.ecommerce.store.dto.auth.UpdateProfileRequest;
import com.ecommerce.store.dto.auth.UserResponse;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.CartRepository;
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
    private final CartRepository cartRepository;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        CartRepository cartRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.cartRepository = cartRepository;
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
        return new AuthResponse(token, toUserResponse(savedUser));
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

        return new AuthResponse(token, toUserResponse(user));
    }

    public UserResponse getCurrentUser(String email) {
        return toUserResponse(findUser(email));
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUser(email);
        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(clean(request.getPhoneNumber()));
        user.setProfilePictureUrl(clean(request.getProfilePictureUrl()));
        user.setAddressLine1(clean(request.getAddressLine1()));
        user.setAddressLine2(clean(request.getAddressLine2()));
        user.setCity(clean(request.getCity()));
        user.setState(clean(request.getState()));
        user.setPostalCode(clean(request.getPostalCode()));
        user.setCountry(clean(request.getCountry()));
        return toUserResponse(userRepository.save(user));
    }

    public void resetForgottenPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("No account exists for this email."));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toUserResponse)
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

        cartRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getPhoneNumber(),
            user.getProfilePictureUrl(),
            user.getAddressLine1(),
            user.getAddressLine2(),
            user.getCity(),
            user.getState(),
            user.getPostalCode(),
            user.getCountry()
        );
    }
}
