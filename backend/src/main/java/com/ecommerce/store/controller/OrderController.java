package com.ecommerce.store.controller;

import com.ecommerce.store.dto.order.CheckoutOrderRequest;
import com.ecommerce.store.dto.order.OrderResponse;
import com.ecommerce.store.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@Valid @RequestBody CheckoutOrderRequest request, Principal principal) {
        return orderService.checkout(principal.getName(), request);
    }

    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(Principal principal) {
        return orderService.getMyOrders(principal.getName());
    }
}
