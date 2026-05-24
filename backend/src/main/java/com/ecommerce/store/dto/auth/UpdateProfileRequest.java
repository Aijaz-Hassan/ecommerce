package com.ecommerce.store.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Please enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(max = 30, message = "Phone number is too long")
    private String phoneNumber;

    @Pattern(
        regexp = "^(https?://.+|data:image/(png|jpeg|jpg|gif|webp);base64,.+)?$",
        message = "Profile image must be a valid image URL or uploaded image"
    )
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
