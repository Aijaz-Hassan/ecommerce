package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By fullNameInput = By.name("fullName");
    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By errorMessage = By.cssSelector(".error-text");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public RegisterPage open(String baseUrl) {
        driver.get(baseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput));
        return this;
    }

    public LoginPage register(String fullName, String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(fullNameInput)).sendKeys(fullName);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password, Keys.ENTER);
        acceptSuccessAlertIfPresent();
        wait.until(ExpectedConditions.urlContains("/login"));
        return new LoginPage(driver);
    }

    public String errorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    private void acceptSuccessAlertIfPresent() {
        try {
            Alert alert = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (TimeoutException ignored) {
            // The app may navigate to login before Chrome exposes the alert in headless runs.
        }
    }
}
