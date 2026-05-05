package com.ecommerce.store;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndProductApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerApiCreatesUserAndReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "Test User",
                      "email": "test.user@example.com",
                      "password": "Demo1234"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.email").value("test.user@example.com"));
    }

    @Test
    void loginApiValidatesCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "Login User",
                      "email": "login.user@example.com",
                      "password": "Demo1234"
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.user@example.com",
                      "password": "Demo1234"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.email").value("login.user@example.com"));
    }

    @Test
    void getProductsApiIsPublic() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk());
    }

    @Test
    void addProductApiRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Aurora Headset",
                      "description": "Premium audio device",
                      "price": 199.99,
                      "category": "Audio",
                      "imageUrl": "https://example.com/headset.jpg",
                      "stock": 25
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void addProductApiWorksForAdmin() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Aurora Headset",
                      "description": "Premium audio device",
                      "price": 199.99,
                      "category": "Audio",
                      "imageUrl": "https://example.com/headset.jpg",
                      "stock": 25
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Aurora Headset"));
    }

    @Test
    void getProductByIdApiReturnsRequestedProduct() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Orbit Lamp",
                      "description": "Ambient desk light",
                      "price": 89.99,
                      "category": "Lighting",
                      "imageUrl": "https://example.com/lamp.jpg",
                      "stock": 8
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode createdProduct = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = createdProduct.get("id").asLong();

        mockMvc.perform(get("/api/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Orbit Lamp"));
    }

    @Test
    void getAllProductsSupportsSearchAndCategoryFilter() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Sky Speaker",
                      "description": "Portable audio device",
                      "price": 129.99,
                      "category": "Audio",
                      "imageUrl": "https://example.com/speaker.jpg",
                      "stock": 18
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Canvas Keyboard",
                      "description": "Mechanical workspace keyboard",
                      "price": 159.99,
                      "category": "Workspace",
                      "imageUrl": "https://example.com/keyboard.jpg",
                      "stock": 10
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products")
                .param("search", "speaker"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Sky Speaker"));

        mockMvc.perform(get("/api/products")
                .param("category", "Workspace"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].category").value("Workspace"));
    }

    @Test
    void deleteProductApiWorksForAdmin() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Delete Me",
                      "description": "To be removed",
                      "price": 49.99,
                      "category": "Audio",
                      "imageUrl": "https://example.com/delete.jpg",
                      "stock": 4
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode createdProduct = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = createdProduct.get("id").asLong();

        mockMvc.perform(delete("/api/products/" + productId)
                .with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateProductApiWorksForAdmin() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Original Product",
                      "description": "Original description",
                      "price": 79.99,
                      "category": "Lighting",
                      "imageUrl": "https://example.com/original.jpg",
                      "stock": 12
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode createdProduct = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = createdProduct.get("id").asLong();

        mockMvc.perform(put("/api/products/" + productId)
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Updated Product",
                      "description": "Updated description",
                      "price": 89.99,
                      "category": "Workspace",
                      "imageUrl": "https://example.com/updated.jpg",
                      "stock": 9
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Product"))
            .andExpect(jsonPath("$.category").value("Workspace"));
    }

    @Test
    void getUsersApiIsAdminOnly() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/users")
                .with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void cartApisAndAdminSummaryWork() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "Cart User",
                      "email": "cart.user@example.com",
                      "password": "Demo1234"
                    }
                    """))
            .andExpect(status().isCreated());

        MvcResult createResult = mockMvc.perform(post("/api/products")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Purchase Product",
                      "description": "Bought item",
                      "price": 99.99,
                      "category": "Audio",
                      "imageUrl": "https://example.com/purchase.jpg",
                      "stock": 10
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode createdProduct = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = createdProduct.get("id").asLong();

        mockMvc.perform(post("/api/cart/items")
                .with(user("cart.user@example.com").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": %d,
                      "quantity": 2
                    }
                    """.formatted(productId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerEmail").value("cart.user@example.com"))
            .andExpect(jsonPath("$.items[0].productName").value("Purchase Product"));

        mockMvc.perform(get("/api/cart")
                .with(user("cart.user@example.com").roles("CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].quantity").value(2));

        mockMvc.perform(put("/api/cart/items/" + productId)
                .with(user("cart.user@example.com").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": %d,
                      "quantity": 3
                    }
                    """.formatted(productId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].quantity").value(3));

        mockMvc.perform(get("/api/cart/admin-summary")
                .with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].customerEmail").value("cart.user@example.com"))
            .andExpect(jsonPath("$[0].items[0].quantity").value(3));

        mockMvc.perform(delete("/api/cart/items/" + productId)
                .with(user("cart.user@example.com").roles("CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isEmpty());
    }
}
