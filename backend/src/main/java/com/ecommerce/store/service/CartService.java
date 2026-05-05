package com.ecommerce.store.service;

import com.ecommerce.store.dto.cart.CartItemRequest;
import com.ecommerce.store.dto.cart.CartItemResponse;
import com.ecommerce.store.dto.cart.CartResponse;
import com.ecommerce.store.entity.Cart;
import com.ecommerce.store.entity.CartItem;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.entity.User;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.CartItemRepository;
import com.ecommerce.store.repository.CartRepository;
import com.ecommerce.store.repository.ProductRepository;
import com.ecommerce.store.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCurrentCart(String customerEmail) {
        User user = findUser(customerEmail);
        return toResponse(getOrCreateCart(user));
    }

    @Transactional
    public CartResponse addItem(String customerEmail, CartItemRequest request) {
        User user = findUser(customerEmail);
        Product product = findProduct(request.getProductId());
        Cart cart = getOrCreateCart(user);

        CartItem existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst()
            .orElse(null);

        int nextQuantity = request.getQuantity();
        if (existingItem != null) {
            nextQuantity += existingItem.getQuantity();
            existingItem.setQuantity(nextQuantity);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cart.getItems().add(cartItem);
        }

        validateStock(product, nextQuantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItemQuantity(String customerEmail, Long productId, CartItemRequest request) {
        User user = findUser(customerEmail);
        Product product = findProduct(productId);
        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Product is not in the cart"));

        validateStock(product, request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(String customerEmail, Long productId) {
        User user = findUser(customerEmail);
        Cart cart = getOrCreateCart(user);
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new BadRequestException("Product is not in the cart");
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String customerEmail) {
        User user = findUser(customerEmail);
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getAllCartsForAdmin() {
        return cartRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void removeProductFromCarts(Product product) {
        cartItemRepository.deleteByProduct(product);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BadRequestException("Product not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUser(user);
                return cartRepository.save(cart);
            });
    }

    private void validateStock(Product product, int quantity) {
        if (quantity > product.getStock()) {
            throw new BadRequestException("Only " + product.getStock() + " item(s) available for " + product.getName());
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(item -> new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getCategory(),
                item.getProduct().getImageUrl(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            ))
            .toList();

        BigDecimal totalAmount = items.stream()
            .map(CartItemResponse::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
            cart.getId(),
            cart.getUser().getFullName(),
            cart.getUser().getEmail(),
            totalAmount,
            cart.getUpdatedAt(),
            items
        );
    }
}
