package com.ecommerce.store.repository;

import com.ecommerce.store.entity.DeliveryAddress;
import com.ecommerce.store.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

    List<DeliveryAddress> findAllByUserOrderByIdAsc(User user);

    Optional<DeliveryAddress> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
