package com.ecommerce.store.service;

import com.ecommerce.store.dto.purchase.PurchaseItemResponse;
import com.ecommerce.store.dto.purchase.PurchaseResponse;
import com.ecommerce.store.entity.Purchase;
import com.ecommerce.store.entity.PurchaseItem;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.PurchaseRepository;
import com.ecommerce.store.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, UserRepository userRepository) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getMyOrders(String customerEmail) {
        User user = findUser(customerEmail);
        return purchaseRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getMyOrderById(String customerEmail, Long purchaseId) {
        User user = findUser(customerEmail);
        Purchase purchase = purchaseRepository.findByIdAndUser(purchaseId, user)
            .orElseThrow(() -> new BadRequestException("Order not found"));
        return toResponse(purchase);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        List<PurchaseItemResponse> items = purchase.getItems().stream()
            .map(this::toItemResponse)
            .toList();

        return new PurchaseResponse(
            purchase.getId(),
            purchase.getOrderNumber(),
            purchase.getStatus().name(),
            purchase.getCreatedAt(),
            purchase.getSubtotal(),
            purchase.getTaxAmount(),
            purchase.getShippingAmount(),
            purchase.getTotalAmount(),
            purchase.getPaymentProvider(),
            items
        );
    }

    private PurchaseItemResponse toItemResponse(PurchaseItem item) {
        return new PurchaseItemResponse(
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
        );
    }
}
