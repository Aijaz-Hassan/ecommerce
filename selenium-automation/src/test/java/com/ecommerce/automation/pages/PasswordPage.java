package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PasswordPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By currentPassword = By.name("currentPassword");
    private final By newPassword = By.name("newPassword");
    private final By confirmPassword = By.name("confirmPassword");
    private final By submitButton = By.xpath("//button[normalize-space()='Update Password']");
    private final By errorMessage = By.cssSelector(".error-text");

    public PasswordPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public PasswordPage open(String baseUrl) {
        driver.get(baseUrl + "/account/password");
        wait.until(ExpectedConditions.visibilityOfElementLocated(currentPassword));
        return this;
    }

    public PasswordPage submitMismatchedPasswords(String current, String next, String confirm) {
        driver.findElement(currentPassword).sendKeys(current);
        driver.findElement(newPassword).sendKeys(next);
        driver.findElement(confirmPassword).sendKeys(confirm);
        driver.findElement(submitButton).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return this;
    }

    public boolean hasValidationError() {
        return !wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText().isBlank();
    }
}
