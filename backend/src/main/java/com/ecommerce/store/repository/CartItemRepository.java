package com.ecommerce.store.repository;

import com.ecommerce.store.entity.CartItem;
import com.ecommerce.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteByProduct(Product product);
}
