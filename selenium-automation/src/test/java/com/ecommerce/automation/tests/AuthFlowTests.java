package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.LoginPage;
import com.ecommerce.automation.pages.RegisterPage;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthFlowTests extends BaseTest {

    @Test(description = "User should register successfully and then log in with the same credentials")
    public void userCanRegisterAndLogin() {
        String uniqueId = String.valueOf(Instant.now().toEpochMilli());
        String fullName = "Selenium User " + uniqueId;
        String email = "selenium.user." + uniqueId + "@example.com";
        String password = "Test12345";

        LoginPage loginPage = new RegisterPage(driver)
                .open(baseUrl)
                .register(fullName, email, password)
                .login(email, password);

        Assert.assertTrue(loginPage.isAccountMenuVisible(), "Account menu should be visible after login.");
        Assert.assertTrue(loginPage.accountName().contains(fullName), "Logged-in user's full name should be shown.");
    }

    @Test(description = "Login page should show an error for invalid credentials")
    public void invalidLoginShowsError() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl)
                .login("missing.user@example.com", "Wrong12345");

        Assert.assertFalse(loginPage.errorText().isBlank(), "Invalid login should show an error message.");
    }
}
