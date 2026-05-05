package com.ecommerce.store.service;

import com.ecommerce.store.dto.product.ProductRequest;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.exception.BadRequestException;
import com.ecommerce.store.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartService cartService;

    public ProductService(ProductRepository productRepository, CartService cartService) {
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    public Product addProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setStock(request.getStock());
        return productRepository.save(product);
    }

    public List<Product> getAllProducts(String search, String category) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasSearch && hasCategory) {
            return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(search, search).stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category))
                .toList();
        }

        if (hasSearch) {
            return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(search, search);
        }

        if (hasCategory) {
            return productRepository.findByCategoryIgnoreCase(category);
        }

        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Product not found"));
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setStock(request.getStock());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Product not found"));
        cartService.removeProductFromCarts(product);
        productRepository.delete(product);
    }
}
