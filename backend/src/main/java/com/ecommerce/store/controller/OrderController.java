package com.ecommerce.store.controller;

import com.ecommerce.store.dto.order.CancelOrderRequest;
import com.ecommerce.store.dto.order.AdminOrderSummaryResponse;
import com.ecommerce.store.dto.order.CheckoutOrderRequest;
import com.ecommerce.store.dto.order.OrderResponse;
import com.ecommerce.store.dto.order.UpdateOrderRequest;
import com.ecommerce.store.service.OrderService;
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

    @GetMapping("/admin-summary")
    public List<AdminOrderSummaryResponse> getAdminOrderSummary() {
        return orderService.getAdminOrderSummary();
    }

    @GetMapping("/history")
    public List<OrderResponse> getMyOrderHistory(Principal principal) {
        return orderService.getMyOrderHistory(principal.getName());
    }

    @GetMapping("/my/{orderId}")
    public OrderResponse getMyOrderById(@PathVariable Long orderId, Principal principal) {
        return orderService.getMyOrderById(principal.getName(), orderId);
    }

    @PutMapping("/my/{orderId}")
    public OrderResponse updateMyOrder(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderRequest request, Principal principal) {
        return orderService.updateMyOrder(principal.getName(), orderId, request);
    }

    @PutMapping("/my/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelMyOrder(@PathVariable Long orderId, @Valid @RequestBody CancelOrderRequest request, Principal principal) {
        orderService.cancelMyOrder(principal.getName(), orderId, request);
    }

    @DeleteMapping("/my/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyOrder(@PathVariable Long orderId, Principal principal) {
        orderService.deleteMyOrder(principal.getName(), orderId);
    }
}
