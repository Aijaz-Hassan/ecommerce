package com.ecommerce.automation.support;

import com.ecommerce.automation.pages.LoginPage;
import com.ecommerce.automation.pages.RegisterPage;
import java.time.Instant;
import org.openqa.selenium.WebDriver;

public class AuthTestHelper {
    private static final String DEFAULT_PASSWORD = "Test12345";

    private final WebDriver driver;
    private final String baseUrl;

    public AuthTestHelper(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
    }

    public LoginPage loginAs(TestUser user) {
        return new LoginPage(driver)
                .open(baseUrl)
                .login(user.email(), user.password());
    }

    public LoginPage loginWithCredentials(String email, String password) {
        return new LoginPage(driver)
                .open(baseUrl)
                .login(email, password);
    }

    public LoginPage register(TestUser user) {
        return new RegisterPage(driver)
                .open(baseUrl)
                .register(user.fullName(), user.email(), user.password());
    }

    public RegisterPage registerExpectingError(TestUser user) {
        return new RegisterPage(driver)
                .open(baseUrl)
                .registerExpectingError(user.fullName(), user.email(), user.password());
    }

    public static TestUser uniqueCustomer() {
        String uniqueId = String.valueOf(Instant.now().toEpochMilli());
        return new TestUser(
                "Selenium User " + uniqueId,
                "selenium.user." + uniqueId + "@example.com",
                DEFAULT_PASSWORD
        );
    }

    public static TestUser invalidCustomer() {
        String uniqueId = String.valueOf(Instant.now().toEpochMilli());
        return new TestUser(
                "Invalid Selenium User " + uniqueId,
                "invalid.selenium.user." + uniqueId + "@example.com",
                "short1"
        );
    }

    public static TestUser defaultAdmin() {
        return new TestUser(
                System.getProperty("adminFullName", "Lumen Lane Admin"),
                "miraijazhassan@gmail.com",
                "12345678qwerty"
        );
    }
}
