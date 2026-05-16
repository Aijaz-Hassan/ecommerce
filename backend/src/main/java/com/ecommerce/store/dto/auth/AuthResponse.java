package com.ecommerce.store.dto.auth;

public class AuthResponse {

    private final String token;
    private final UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getRole() {
        return user.getRole();
    }
}
