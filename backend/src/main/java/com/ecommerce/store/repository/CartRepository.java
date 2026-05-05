package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Cart;
import com.ecommerce.store.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Cart> findByUser(User user);

    void deleteByUser(User user);

    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Cart> findAll();
}
