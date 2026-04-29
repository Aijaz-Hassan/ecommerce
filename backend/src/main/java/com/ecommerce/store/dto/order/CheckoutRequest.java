package com.ecommerce.store.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CheckoutRequest {

    @Valid
    @NotEmpty(message = "At least one cart item is required")
    private List<CheckoutItemRequest> items;

    public List<CheckoutItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CheckoutItemRequest> items) {
        this.items = items;
    }
}
