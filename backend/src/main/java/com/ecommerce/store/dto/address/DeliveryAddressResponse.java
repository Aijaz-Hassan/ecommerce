package com.ecommerce.store.dto.address;

public record DeliveryAddressResponse(
    Long id,
    String label,
    String recipientName,
    String phoneNumber,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country
) {
}
