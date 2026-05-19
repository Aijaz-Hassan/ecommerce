package com.ecommerce.store.dto.auth;

import java.time.LocalDateTime;

public record ResetTokenValidationResponse(
    boolean valid,
    String message,
    LocalDateTime expiresAt
) {
}
