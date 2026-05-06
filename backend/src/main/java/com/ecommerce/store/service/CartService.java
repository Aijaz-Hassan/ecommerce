package com.ecommerce.store.service;

import com.ecommerce.store.dto.cart.CartItemRequest;
import com.ecommerce.store.dto.cart.CartItemResponse;
import com.ecommerce.store.dto.cart.CartResponse;
import com.ecommerce.store.dto.cart.CheckoutCompleteResponse;
import com.ecommerce.store.dto.cart.CheckoutConfirmRequest;
import com.ecommerce.store.dto.cart.CheckoutSessionResponse;
import com.ecommerce.store.entity.Cart;
import com.ecommerce.store.entity.CartItem;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.CartItemRepository;
import com.ecommerce.store.repository.CartRepository;
import com.ecommerce.store.repository.ProductRepository;
import com.ecommerce.store.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${payment.razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${payment.razorpay.key-secret:}")
    private String razorpayKeySecret;

    public CartService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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
            .filter(item ->
                item.getProduct().getId().equals(product.getId()) &&
                normalize(item.getSelectedColor()).equals(normalize(request.getSelectedColor())) &&
                normalize(item.getSelectedSize()).equals(normalize(request.getSelectedSize())) &&
                normalize(item.getCustomizationNote()).equals(normalize(request.getCustomizationNote()))
            )
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
            cartItem.setSelectedColor(request.getSelectedColor());
            cartItem.setSelectedSize(request.getSelectedSize());
            cartItem.setCustomizationNote(request.getCustomizationNote());
            cart.getItems().add(cartItem);
        }

        validateStock(product, nextQuantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(String customerEmail, Long cartItemId, CartItemRequest request) {
        User user = findUser(customerEmail);
        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cart.getItems().stream()
            .filter(item -> item.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Cart item not found"));

        Product product = cartItem.getProduct();
        validateStock(product, request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setSelectedColor(request.getSelectedColor());
        cartItem.setSelectedSize(request.getSelectedSize());
        cartItem.setCustomizationNote(request.getCustomizationNote());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(String customerEmail, Long cartItemId) {
        User user = findUser(customerEmail);
        Cart cart = getOrCreateCart(user);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        if (!removed) {
            throw new BadRequestException("Cart item not found");
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

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(String customerEmail) {
        Cart cart = getCartWithItems(customerEmail);
        BigDecimal totalAmount = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long amountInSubunits = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        if (hasRazorpayKeys()) {
            return createRazorpayOrder(cart, amountInSubunits);
        }

        return new CheckoutSessionResponse(
            "demo",
            "",
            "demo-" + cart.getId() + "-" + System.currentTimeMillis(),
            amountInSubunits,
            "INR",
            cart.getUser().getFullName(),
            cart.getUser().getEmail(),
            "Lumen Lane",
            "Cart payment"
        );
    }

    @Transactional
    public CheckoutCompleteResponse confirmCheckout(String customerEmail, CheckoutConfirmRequest request) {
        Cart cart = getCartWithItems(customerEmail);

        if ("razorpay".equalsIgnoreCase(request.getProvider())) {
            if (isBlank(request.getOrderId()) || isBlank(request.getPaymentId())) {
                throw new BadRequestException("Razorpay confirmation requires order and payment details");
            }
        }

        cart.getItems().clear();
        cartRepository.save(cart);
        return new CheckoutCompleteResponse("Payment recorded successfully and cart cleared.");
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private Cart getCartWithItems(String customerEmail) {
        User user = findUser(customerEmail);
        Cart cart = getOrCreateCart(user);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }
        return cart;
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
                item.getSelectedColor(),
                item.getSelectedSize(),
                item.getCustomizationNote(),
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

    private CheckoutSessionResponse createRazorpayOrder(Cart cart, long amountInSubunits) {
        try {
            String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                    "amount", amountInSubunits,
                    "currency", "INR",
                    "receipt", "cart-" + cart.getId() + "-" + System.currentTimeMillis()
                )
            );

            String credentials = Base64.getEncoder()
                .encodeToString((razorpayKeyId + ":" + razorpayKeySecret).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.razorpay.com/v1/orders"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BadRequestException("Unable to start Razorpay checkout right now");
            }

            JsonNode payload = objectMapper.readTree(response.body());
            return new CheckoutSessionResponse(
                "razorpay",
                razorpayKeyId,
                payload.path("id").asText(),
                payload.path("amount").asLong(),
                payload.path("currency").asText("INR"),
                cart.getUser().getFullName(),
                cart.getUser().getEmail(),
                "Lumen Lane",
                "Cart payment"
            );
        } catch (Exception exception) {
            throw new BadRequestException("Unable to start Razorpay checkout right now");
        }
    }

    private boolean hasRazorpayKeys() {
        return !isBlank(razorpayKeyId) && !isBlank(razorpayKeySecret);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
