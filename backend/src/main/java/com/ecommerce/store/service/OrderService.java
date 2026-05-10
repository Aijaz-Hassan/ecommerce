package com.ecommerce.store.service;

import com.ecommerce.store.dto.order.CheckoutOrderRequest;
import com.ecommerce.store.dto.order.OrderItemResponse;
import com.ecommerce.store.dto.order.OrderResponse;
import com.ecommerce.store.entity.Cart;
import com.ecommerce.store.entity.CartItem;
import com.ecommerce.store.entity.Order;
import com.ecommerce.store.entity.OrderItem;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.CartRepository;
import com.ecommerce.store.repository.OrderRepository;
import com.ecommerce.store.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse checkout(String customerEmail, CheckoutOrderRequest request) {
        User user = findUser(customerEmail);
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new BadRequestException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        BigDecimal subtotal = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingAmount = subtotal.compareTo(new BigDecimal("500.00")) >= 0
            ? BigDecimal.ZERO
            : new BigDecimal("49.00");
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingAmount);

        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setUser(user);
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shippingAmount);
        order.setTotalAmount(totalAmount);
        order.setPaymentProvider(request.getPaymentProvider().toUpperCase());
        order.setPaymentReference(request.getPaymentReference());
        order.setStatus(Order.OrderStatus.CONFIRMED);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setCategory(cartItem.getProduct().getCategory());
            orderItem.setImageUrl(cartItem.getProduct().getImageUrl());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSelectedColor(cartItem.getSelectedColor());
            orderItem.setSelectedSize(cartItem.getSelectedSize());
            orderItem.setCustomizationNote(cartItem.getCustomizationNote());
            orderItem.setLineTotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String customerEmail) {
        User user = findUser(customerEmail);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::toResponse)
            .toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getCategory(),
                item.getImageUrl(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSelectedColor(),
                item.getSelectedSize(),
                item.getCustomizationNote(),
                item.getLineTotal()
            ))
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getSubtotal(),
            order.getTaxAmount(),
            order.getShippingAmount(),
            order.getTotalAmount(),
            order.getPaymentProvider(),
            items
        );
    }
}
