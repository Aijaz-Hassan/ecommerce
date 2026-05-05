package com.ecommerce.store.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
    Long id,
    Long productId,
    String productName,
    String category,
    String imageUrl,
    BigDecimal price,
    Integer quantity,
    BigDecimal lineTotal
) {
}
