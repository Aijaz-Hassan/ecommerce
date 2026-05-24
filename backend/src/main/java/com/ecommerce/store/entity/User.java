package com.ecommerce.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_CUSTOMER";

    private String phoneNumber;

    @Lob
    private String profilePictureUrl;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    private String alternateAddressLine1;

    private String alternateAddressLine2;

    private String alternateCity;

    private String alternateState;

    private String alternatePostalCode;

    private String alternateCountry;

    private Boolean darkModeEnabled = false;

    private Boolean orderNotificationsEnabled = true;

    private Boolean marketingNotificationsEnabled = false;

    private String language = "en";

    private LocalDateTime createdAt;

    private LocalDateTime passwordChangedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (passwordChangedAt == null) {
            passwordChangedAt = createdAt;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAlternateAddressLine1() {
        return alternateAddressLine1;
    }

    public void setAlternateAddressLine1(String alternateAddressLine1) {
        this.alternateAddressLine1 = alternateAddressLine1;
    }

    public String getAlternateAddressLine2() {
        return alternateAddressLine2;
    }

    public void setAlternateAddressLine2(String alternateAddressLine2) {
        this.alternateAddressLine2 = alternateAddressLine2;
    }

    public String getAlternateCity() {
        return alternateCity;
    }

    public void setAlternateCity(String alternateCity) {
        this.alternateCity = alternateCity;
    }

    public String getAlternateState() {
        return alternateState;
    }

    public void setAlternateState(String alternateState) {
        this.alternateState = alternateState;
    }

    public String getAlternatePostalCode() {
        return alternatePostalCode;
    }

    public void setAlternatePostalCode(String alternatePostalCode) {
        this.alternatePostalCode = alternatePostalCode;
    }

    public String getAlternateCountry() {
        return alternateCountry;
    }

    public void setAlternateCountry(String alternateCountry) {
        this.alternateCountry = alternateCountry;
    }

    public Boolean getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(Boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public Boolean getOrderNotificationsEnabled() {
        return orderNotificationsEnabled;
    }

    public void setOrderNotificationsEnabled(Boolean orderNotificationsEnabled) {
        this.orderNotificationsEnabled = orderNotificationsEnabled;
    }

    public Boolean getMarketingNotificationsEnabled() {
        return marketingNotificationsEnabled;
    }

    public void setMarketingNotificationsEnabled(Boolean marketingNotificationsEnabled) {
        this.marketingNotificationsEnabled = marketingNotificationsEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }
}
