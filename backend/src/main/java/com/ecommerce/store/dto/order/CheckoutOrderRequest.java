package com.ecommerce.store.dto.order;

import jakarta.validation.constraints.NotBlank;

public class CheckoutOrderRequest {

    @NotBlank(message = "Payment provider is required")
    private String paymentProvider;

    private String paymentReference;

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
}
