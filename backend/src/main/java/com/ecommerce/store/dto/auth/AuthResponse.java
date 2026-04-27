package com.ecommerce.store.dto.auth;

public class AuthResponse {

    private final String token;
    private final String fullName;
    private final String email;
    private final String role;

    public AuthResponse(String token, String fullName, String email, String role) {
        this.token = token;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
