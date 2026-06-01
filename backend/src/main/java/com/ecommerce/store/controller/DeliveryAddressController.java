package com.ecommerce.store.controller;

import com.ecommerce.store.dto.address.DeliveryAddressRequest;
import com.ecommerce.store.dto.address.DeliveryAddressResponse;
import com.ecommerce.store.service.DeliveryAddressService;
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
@RequestMapping("/api/addresses")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    public DeliveryAddressController(DeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    @GetMapping
    public List<DeliveryAddressResponse> getAddresses(Principal principal) {
        return deliveryAddressService.getAddresses(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryAddressResponse createAddress(@Valid @RequestBody DeliveryAddressRequest request, Principal principal) {
        return deliveryAddressService.createAddress(principal.getName(), request);
    }

    @PutMapping("/{addressId}")
    public DeliveryAddressResponse updateAddress(
        @PathVariable Long addressId,
        @Valid @RequestBody DeliveryAddressRequest request,
        Principal principal
    ) {
        return deliveryAddressService.updateAddress(principal.getName(), addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@PathVariable Long addressId, Principal principal) {
        deliveryAddressService.deleteAddress(principal.getName(), addressId);
    }
}
