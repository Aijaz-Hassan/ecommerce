package com.ecommerce.store.repository;

import com.ecommerce.store.entity.PasswordResetToken;
import com.ecommerce.store.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);

    Optional<PasswordResetToken> findFirstByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("update PasswordResetToken token set token.used = true where token.user = :user and token.used = false")
    void markActiveTokensUsedForUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
