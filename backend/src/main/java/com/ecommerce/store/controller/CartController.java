package com.ecommerce.store.controller;

import com.ecommerce.store.dto.cart.CartItemRequest;
import com.ecommerce.store.dto.cart.CartResponse;
import com.ecommerce.store.dto.cart.CheckoutCompleteResponse;
import com.ecommerce.store.dto.cart.CheckoutConfirmRequest;
import com.ecommerce.store.dto.cart.CheckoutSessionResponse;
import com.ecommerce.store.service.CartService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCurrentCart(Principal principal) {
        return cartService.getCurrentCart(principal.getName());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@Valid @RequestBody CartItemRequest request, Principal principal) {
        return cartService.addItem(principal.getName(), request);
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateItem(
        @PathVariable Long cartItemId,
        @Valid @RequestBody CartItemRequest request,
        Principal principal
    ) {
        return cartService.updateItem(principal.getName(), cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItem(@PathVariable Long cartItemId, Principal principal) {
        return cartService.removeItem(principal.getName(), cartItemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
    }

    @GetMapping("/admin-summary")
    public List<CartResponse> getAdminSummary() {
        return cartService.getAllCartsForAdmin();
    }

    @PostMapping("/checkout/session")
    public CheckoutSessionResponse createCheckoutSession(Principal principal) {
        return cartService.createCheckoutSession(principal.getName());
    }

    @PostMapping("/checkout/confirm")
    public CheckoutCompleteResponse confirmCheckout(@Valid @RequestBody CheckoutConfirmRequest request, Principal principal) {
        return cartService.confirmCheckout(principal.getName(), request);
    }
}
