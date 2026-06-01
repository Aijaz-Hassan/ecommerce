package com.ecommerce.store.service;

import com.ecommerce.store.dto.address.DeliveryAddressRequest;
import com.ecommerce.store.dto.address.DeliveryAddressResponse;
import com.ecommerce.store.entity.DeliveryAddress;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.DeliveryAddressRepository;
import com.ecommerce.store.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final UserRepository userRepository;

    public DeliveryAddressService(DeliveryAddressRepository deliveryAddressRepository, UserRepository userRepository) {
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DeliveryAddressResponse> getAddresses(String email) {
        User user = findUser(email);
        return deliveryAddressRepository.findAllByUserOrderByIdAsc(user).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public DeliveryAddressResponse createAddress(String email, DeliveryAddressRequest request) {
        DeliveryAddress address = new DeliveryAddress();
        address.setUser(findUser(email));
        applyRequest(address, request);
        return toResponse(deliveryAddressRepository.save(address));
    }

    @Transactional
    public DeliveryAddressResponse updateAddress(String email, Long addressId, DeliveryAddressRequest request) {
        User user = findUser(email);
        DeliveryAddress address = deliveryAddressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new BadRequestException("Delivery address not found"));
        applyRequest(address, request);
        return toResponse(deliveryAddressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = findUser(email);
        DeliveryAddress address = deliveryAddressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new BadRequestException("Delivery address not found"));
        deliveryAddressRepository.delete(address);
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private void applyRequest(DeliveryAddress address, DeliveryAddressRequest request) {
        address.setLabel(request.getLabel().trim());
        address.setRecipientName(request.getRecipientName().trim());
        address.setPhoneNumber(request.getPhoneNumber().trim());
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(clean(request.getAddressLine2()));
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPostalCode(request.getPostalCode().trim());
        address.setCountry(request.getCountry().trim());
    }

    private String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private DeliveryAddressResponse toResponse(DeliveryAddress address) {
        return new DeliveryAddressResponse(
            address.getId(),
            address.getLabel(),
            address.getRecipientName(),
            address.getPhoneNumber(),
            address.getAddressLine1(),
            address.getAddressLine2(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            address.getCountry()
        );
    }
}
