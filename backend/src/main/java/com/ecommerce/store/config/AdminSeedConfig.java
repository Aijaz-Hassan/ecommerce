package com.ecommerce.store.config;

import com.ecommerce.store.entity.User;
import com.ecommerce.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeedConfig {

    @Bean
    ApplicationRunner adminSeedRunner(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.seed.enabled:true}") boolean seedEnabled,
        @Value("${app.admin.email:admin@lumenlane.test}") String adminEmail,
        @Value("${app.admin.password:Admin12345}") String adminPassword,
        @Value("${app.admin.full-name:Lumen Lane Admin}") String adminFullName
    ) {
        return args -> {
            if (!seedEnabled) {
                return;
            }

            String email = adminEmail.trim().toLowerCase();
            User admin = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
            admin.setFullName(adminFullName.trim());
            admin.setName(email);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
        };
    }
}
