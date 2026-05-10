package com.ecommerce.store.controller;

import com.ecommerce.store.dto.purchase.PurchaseResponse;
import com.ecommerce.store.service.PurchaseService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/my")
    public List<PurchaseResponse> getMyOrders(Principal principal) {
        return purchaseService.getMyOrders(principal.getName());
    }

    @GetMapping("/my/{purchaseId}")
    public PurchaseResponse getMyOrderById(@PathVariable Long purchaseId, Principal principal) {
        return purchaseService.getMyOrderById(principal.getName(), purchaseId);
    }
}
