package com.ecommerce.store.dto.cart;

import jakarta.validation.constraints.NotBlank;

public class CheckoutConfirmRequest {

    @NotBlank(message = "Provider is required")
    private String provider;

    private String orderId;

    private String paymentId;

    private String signature;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
