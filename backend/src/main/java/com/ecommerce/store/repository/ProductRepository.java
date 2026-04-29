package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
    List<Product> findByCategoryIgnoreCase(String category);
}
