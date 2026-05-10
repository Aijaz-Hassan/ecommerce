package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Purchase;
import com.ecommerce.store.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @EntityGraph(attributePaths = {"items"})
    List<Purchase> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "items"})
    Optional<Purchase> findByIdAndUser(Long id, User user);
}
