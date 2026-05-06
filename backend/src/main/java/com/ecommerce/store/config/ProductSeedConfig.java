package com.ecommerce.store.config;

import com.ecommerce.store.entity.Product;
import com.ecommerce.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductSeedConfig {

    @Bean
    ApplicationRunner productSeedRunner(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            productRepository.saveAll(List.of(
                createProduct(
                    "Halo Portable Projector",
                    "Pocket-sized cinema energy for intimate rooms and weekend escapes with crisp projection and easy wireless casting.",
                    "Entertainment",
                    "https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=900&q=80",
                    new BigDecimal("299.99"),
                    14
                ),
                createProduct(
                    "Vanta Mechanical Keyboard",
                    "Low-profile tactile switches, refined aluminum body, and smooth typing comfort for long work sessions.",
                    "Workspace",
                    "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=900&q=80",
                    new BigDecimal("189.00"),
                    27
                ),
                createProduct(
                    "Nova Travel Speaker",
                    "Spatial sound, durable shell, and all-day battery life built for portable music without compromise.",
                    "Audio",
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80",
                    new BigDecimal("149.50"),
                    33
                ),
                createProduct(
                    "Luna Smart Watch",
                    "Health tracking, polished ceramic finish, and fast notifications designed for everyday movement.",
                    "Wearables",
                    "https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=900&q=80",
                    new BigDecimal("229.99"),
                    19
                ),
                createProduct(
                    "Atlas Travel Backpack",
                    "Structured commuter bag with device protection, hidden pockets, and weather-ready fabric for daily carry.",
                    "Travel",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80",
                    new BigDecimal("119.00"),
                    21
                ),
                createProduct(
                    "Prism Desk Monitor Light",
                    "A glare-free monitor lamp tuned for coding sessions, focused reading, and late-night desk setups.",
                    "Lighting",
                    "https://images.unsplash.com/photo-1519710164239-da123dc03ef4?auto=format&fit=crop&w=900&q=80",
                    new BigDecimal("84.50"),
                    42
                )
            ));
        };
    }

    private Product createProduct(
        String name,
        String description,
        String category,
        String imageUrl,
        BigDecimal price,
        int stock
    ) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }
}
