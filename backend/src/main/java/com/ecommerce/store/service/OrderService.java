package com.ecommerce.store.service;

import com.ecommerce.store.dto.order.CheckoutItemRequest;
import com.ecommerce.store.dto.order.CheckoutRequest;
import com.ecommerce.store.dto.order.OrderResponse;
import com.ecommerce.store.entity.Order;
import com.ecommerce.store.entity.OrderItem;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.OrderRepository;
import com.ecommerce.store.repository.ProductRepository;
import com.ecommerce.store.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse checkout(String customerEmail, CheckoutRequest request) {
        User user = userRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new BadRequestException("User not found"));

        Order order = new Order();
        order.setUser(user);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CheckoutItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new BadRequestException("Product not found"));

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            product.setStock(product.getStock() - itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<String> products = order.getItems().stream()
            .map(item -> item.getProduct().getName() + " x" + item.getQuantity())
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getUser().getFullName(),
            order.getUser().getEmail(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            products
        );
    }
}
