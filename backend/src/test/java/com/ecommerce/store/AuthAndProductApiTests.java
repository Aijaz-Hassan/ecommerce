package com.ecommerce.store;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndProductApiTests {

    @Autowired
    private MockMvc mockMvc;

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
}
