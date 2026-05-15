package com.ecommerce.store.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    String status,
    LocalDateTime createdAt,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal shippingAmount,
    BigDecimal totalAmount,
    String paymentProvider,
    String recipientName,
    String phoneNumber,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country,
    String trackingLocation,
    String trackingNote,
    String cancellationReason,
    List<OrderItemResponse> items
) {
}
