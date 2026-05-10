package com.ecommerce.store.dto.purchase;

import java.math.BigDecimal;

public record PurchaseItemResponse(
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
