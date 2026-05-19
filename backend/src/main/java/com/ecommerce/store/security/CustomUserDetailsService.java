package com.ecommerce.store.security;

import com.ecommerce.store.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.ecommerce.store.entity.User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.getEmail())
            .password(user.getPassword())
            .roles(normalizeRole(user.getRole()).replace("ROLE_", ""))
            .build();
    }

    private String normalizeRole(String role) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        if ("ADMIN".equals(normalizedRole) || "ROLE_ADMIN".equals(normalizedRole)) {
            return "ROLE_ADMIN";
        }
        return "ROLE_CUSTOMER";
    }
}
