package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Order;
import com.ecommerce.store.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
