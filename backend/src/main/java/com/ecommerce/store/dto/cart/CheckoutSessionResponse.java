package com.ecommerce.store.dto.cart;

public record CheckoutSessionResponse(
    String provider,
    String keyId,
    String orderId,
    Long amount,
    String currency,
    String customerName,
    String customerEmail,
    String merchantName,
    String description
) {
}
