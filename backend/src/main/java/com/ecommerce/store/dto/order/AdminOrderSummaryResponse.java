package com.ecommerce.store.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderSummaryResponse(
    Long orderId,
    String orderNumber,
    String customerName,
    String customerEmail,
    String status,
    LocalDateTime createdAt,
    BigDecimal totalAmount,
    List<AdminOrderItemResponse> items
) {
}
