package com.ecommerce.store.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long productId,
    String productName,
    String category,
    String imageUrl,
    BigDecimal unitPrice,
    Integer quantity,
    String selectedColor,
    String selectedSize,
    String customizationNote,
    BigDecimal lineTotal
) {
}
