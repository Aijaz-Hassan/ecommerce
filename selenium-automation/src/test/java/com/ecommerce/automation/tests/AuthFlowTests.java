package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.LoginPage;
import com.ecommerce.automation.pages.RegisterPage;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthFlowTests extends BaseTest {
    private static final String VALID_PASSWORD = "Test12345";

    @Test(description = "Registration should succeed with valid details")
    public void validRegistrationRedirectsToLogin() {
        TestUser user = uniqueUser();

        LoginPage loginPage = new RegisterPage(driver)
                .open(baseUrl)
                .register(user.fullName(), user.email(), VALID_PASSWORD);

        Assert.assertTrue(loginPage.isOnLoginPage(), "Valid registration should redirect to the login page.");
    }

    @Test(description = "Registration should show an error for invalid details")
    public void invalidRegistrationShowsError() {
        TestUser user = uniqueUser();

        RegisterPage registerPage = new RegisterPage(driver)
                .open(baseUrl)
                .registerExpectingError(user.fullName(), user.email(), "short1");

        Assert.assertTrue(registerPage.isOnRegisterPage(), "Invalid registration should keep the user on register page.");
        Assert.assertFalse(registerPage.errorText().isBlank(), "Invalid registration should show an error message.");
    }

    @Test(description = "Login should succeed with valid credentials")
    public void validLoginShowsAccountMenu() {
        TestUser user = uniqueUser();

        LoginPage loginPage = new RegisterPage(driver)
                .open(baseUrl)
                .register(user.fullName(), user.email(), VALID_PASSWORD)
                .login(user.email(), VALID_PASSWORD);

        Assert.assertTrue(loginPage.isAccountMenuVisible(), "Account menu should be visible after login.");
        Assert.assertTrue(loginPage.accountName().contains(user.fullName()), "Logged-in user's full name should be shown.");
    }

    @Test(description = "Login page should show an error for invalid credentials")
    public void invalidLoginShowsError() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl)
                .login("missing.user@example.com", "Wrong12345");

        Assert.assertFalse(loginPage.errorText().isBlank(), "Invalid login should show an error message.");
    }

    private TestUser uniqueUser() {
        String uniqueId = String.valueOf(Instant.now().toEpochMilli());
        return new TestUser(
                "Selenium User " + uniqueId,
                "selenium.user." + uniqueId + "@example.com"
        );
    }

    private record TestUser(String fullName, String email) {
    }
}
