package com.ecommerce.store.dto.auth;

import java.time.LocalDateTime;

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
    private final String alternateAddressLine1;
    private final String alternateAddressLine2;
    private final String alternateCity;
    private final String alternateState;
    private final String alternatePostalCode;
    private final String alternateCountry;
    private final Boolean darkModeEnabled;
    private final Boolean orderNotificationsEnabled;
    private final Boolean marketingNotificationsEnabled;
    private final String language;
    private final LocalDateTime createdAt;

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
        String country,
        String alternateAddressLine1,
        String alternateAddressLine2,
        String alternateCity,
        String alternateState,
        String alternatePostalCode,
        String alternateCountry,
        Boolean darkModeEnabled,
        Boolean orderNotificationsEnabled,
        Boolean marketingNotificationsEnabled,
        String language,
        LocalDateTime createdAt
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
        this.alternateAddressLine1 = alternateAddressLine1;
        this.alternateAddressLine2 = alternateAddressLine2;
        this.alternateCity = alternateCity;
        this.alternateState = alternateState;
        this.alternatePostalCode = alternatePostalCode;
        this.alternateCountry = alternateCountry;
        this.darkModeEnabled = darkModeEnabled;
        this.orderNotificationsEnabled = orderNotificationsEnabled;
        this.marketingNotificationsEnabled = marketingNotificationsEnabled;
        this.language = language;
        this.createdAt = createdAt;
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

    public String getAlternateAddressLine1() {
        return alternateAddressLine1;
    }

    public String getAlternateAddressLine2() {
        return alternateAddressLine2;
    }

    public String getAlternateCity() {
        return alternateCity;
    }

    public String getAlternateState() {
        return alternateState;
    }

    public String getAlternatePostalCode() {
        return alternatePostalCode;
    }

    public String getAlternateCountry() {
        return alternateCountry;
    }

    public Boolean getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public Boolean getOrderNotificationsEnabled() {
        return orderNotificationsEnabled;
    }

    public Boolean getMarketingNotificationsEnabled() {
        return marketingNotificationsEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
