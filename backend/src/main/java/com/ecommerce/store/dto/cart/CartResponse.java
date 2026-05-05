package com.ecommerce.store.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
    Long cartId,
    String customerName,
    String customerEmail,
    BigDecimal totalAmount,
    LocalDateTime updatedAt,
    List<CartItemResponse> items
) {
}
