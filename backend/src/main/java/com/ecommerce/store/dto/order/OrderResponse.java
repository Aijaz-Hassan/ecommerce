package com.ecommerce.store.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private final Long orderId;
    private final String customerName;
    private final String customerEmail;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final List<String> purchasedProducts;

    public OrderResponse(
        Long orderId,
        String customerName,
        String customerEmail,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<String> purchasedProducts
    ) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.purchasedProducts = purchasedProducts;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getPurchasedProducts() {
        return purchasedProducts;
    }
}
