package com.ecommerce.store.service;

import com.ecommerce.store.dto.order.CancelOrderRequest;
import com.ecommerce.store.dto.order.AdminOrderItemResponse;
import com.ecommerce.store.dto.order.AdminOrderSummaryResponse;
import com.ecommerce.store.dto.order.CheckoutOrderRequest;
import com.ecommerce.store.dto.order.OrderItemResponse;
import com.ecommerce.store.dto.order.OrderResponse;
import com.ecommerce.store.dto.order.UpdateOrderRequest;
import com.ecommerce.store.entity.Cart;
import com.ecommerce.store.entity.CartItem;
import com.ecommerce.store.entity.Order;
import com.ecommerce.store.entity.OrderItem;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.CartRepository;
import com.ecommerce.store.repository.OrderRepository;
import com.ecommerce.store.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    public OrderService(
        OrderRepository orderRepository,
        CartRepository cartRepository,
        UserRepository userRepository,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
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
        order.setRecipientName(request.getRecipientName().trim());
        order.setPhoneNumber(request.getPhoneNumber().trim());
        order.setAddressLine1(request.getAddressLine1().trim());
        order.setAddressLine2(request.getAddressLine2() == null ? null : request.getAddressLine2().trim());
        order.setCity(request.getCity().trim());
        order.setState(request.getState().trim());
        order.setPostalCode(request.getPostalCode().trim());
        order.setCountry(request.getCountry().trim());
        order.setStatus(Order.OrderStatus.CONFIRMED);

        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getQuantity() > cartItem.getProduct().getStock()) {
                throw new BadRequestException("Only " + cartItem.getProduct().getStock() + " item(s) available for " + cartItem.getProduct().getName());
            }

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

            cartItem.getProduct().setStock(cartItem.getProduct().getStock() - cartItem.getQuantity());
            productRepository.save(cartItem.getProduct());
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

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrderHistory(String customerEmail) {
        User user = findUser(customerEmail);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminOrderSummaryResponse> getAdminOrderSummary() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(order -> new AdminOrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getFullName(),
                order.getUser().getEmail(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getTotalAmount(),
                order.getItems().stream()
                    .map(item -> new AdminOrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        currentStock(item.getProductId()),
                        item.getUnitPrice(),
                        item.getLineTotal()
                    ))
                    .toList()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(String customerEmail, Long orderId) {
        User user = findUser(customerEmail);
        Order order = orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .filter(item -> item.getId().equals(orderId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Order not found"));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateMyOrder(String customerEmail, Long orderId, UpdateOrderRequest request) {
        User user = findUser(customerEmail);
        Order order = findMyOrder(user, orderId);

        String nextStatus = request.getStatus().trim().toUpperCase();

        try {
            order.setStatus(Order.OrderStatus.valueOf(nextStatus));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid order status");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            String reason = request.getCancellationReason() == null ? "" : request.getCancellationReason().trim();
            if (reason.length() < 10) {
                throw new BadRequestException("Please provide a valid cancellation reason with at least 10 characters.");
            }
            order.setCancellationReason(reason);
        } else {
            order.setCancellationReason(null);
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void cancelMyOrder(String customerEmail, Long orderId, CancelOrderRequest request) {
        User user = findUser(customerEmail);
        Order order = findMyOrder(user, orderId);

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be cancelled.");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("This order is already cancelled.");
        }

        String reason = request.getReason() == null ? "" : request.getReason().trim();
        if (reason.length() < 5) {
            throw new BadRequestException("Please provide a valid cancellation reason with at least 5 characters.");
        }

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock((product.getStock() == null ? 0 : product.getStock()) + item.getQuantity());
                productRepository.save(product);
            }
        }
        orderRepository.delete(order);
    }

    @Transactional
    public void deleteMyOrder(String customerEmail, Long orderId) {
        User user = findUser(customerEmail);
        Order order = orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .filter(item -> item.getId().equals(orderId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Order not found"));
        orderRepository.delete(order);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private Order findMyOrder(User user, Long orderId) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .filter(item -> item.getId().equals(orderId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Order not found"));
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
            order.getRecipientName(),
            order.getPhoneNumber(),
            order.getAddressLine1(),
            order.getAddressLine2(),
            order.getCity(),
            order.getState(),
            order.getPostalCode(),
            order.getCountry(),
            trackingLocation(order),
            trackingNote(order),
            order.getCancellationReason(),
            items
        );
    }

    private String trackingLocation(Order order) {
        return switch (order.getStatus()) {
            case CONFIRMED -> "Order confirmed at the seller desk";
            case PROCESSING -> "Packed at the city fulfillment hub";
            case SHIPPED -> "Courier route active near " + order.getCity();
            case DELIVERED -> "Delivered to " + order.getCity();
            case CANCELLED -> "Order cancelled before delivery";
        };
    }

    private String trackingNote(Order order) {
        return switch (order.getStatus()) {
            case CONFIRMED -> "Your payment is confirmed and the seller is preparing the package.";
            case PROCESSING -> "Your items are being packed and readied for dispatch.";
            case SHIPPED -> "Your order is on the move to the saved delivery address.";
            case DELIVERED -> "Your package has reached the saved delivery address.";
            case CANCELLED -> "This order is no longer in transit.";
        };
    }

    private Integer currentStock(Long productId) {
        return productRepository.findById(productId)
            .map(product -> product.getStock() == null ? 0 : product.getStock())
            .orElse(0);
    }
}
