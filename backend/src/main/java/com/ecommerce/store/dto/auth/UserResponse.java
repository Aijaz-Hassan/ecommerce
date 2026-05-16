package com.ecommerce.store.dto.auth;

public class UserResponse {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String role;
    private final String phoneNumber;
    private final String profilePictureUrl;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;

    public UserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String phoneNumber,
        String profilePictureUrl,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.profilePictureUrl = profilePictureUrl;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public Long getId() {
        return id;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }
}
