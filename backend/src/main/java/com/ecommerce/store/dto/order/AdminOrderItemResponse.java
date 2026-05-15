package com.ecommerce.store.dto.order;

import java.math.BigDecimal;

public record AdminOrderItemResponse(
    Long productId,
    String productName,
    Integer orderedQuantity,
    Integer remainingStock,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}
