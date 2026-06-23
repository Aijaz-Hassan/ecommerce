package com.ecommerce.store.config;

import com.ecommerce.store.entity.User;
import com.ecommerce.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CustomerSeedConfig {

    @Bean
    ApplicationRunner customerSeedRunner(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.customer.seed.enabled:true}") boolean seedEnabled,
        @Value("${app.customer.email:customer@lumenlane.test}") String customerEmail,
        @Value("${app.customer.password:Customer12345}") String customerPassword,
        @Value("${app.customer.full-name:Lumen Lane Customer}") String customerFullName
    ) {
        return args -> {
            if (!seedEnabled) {
                return;
            }

            String email = customerEmail.trim().toLowerCase();
            User customer = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
            customer.setFullName(customerFullName.trim());
            customer.setName(email);
            customer.setEmail(email);
            customer.setPassword(passwordEncoder.encode(customerPassword));
            customer.setRole("ROLE_CUSTOMER");
            userRepository.save(customer);
        };
    }
}
