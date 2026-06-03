package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By errorMessage = By.cssSelector(".error-text");
    private final By accountButton = By.cssSelector(".account-button");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        return this;
    }

    public LoginPage login(String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(emailInput)).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password, Keys.ENTER);
        wait.until(driver -> !driver.findElements(accountButton).isEmpty() || !driver.findElements(errorMessage).isEmpty());
        return this;
    }

    public boolean isAccountMenuVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(accountButton));
        return driver.findElement(accountButton).isDisplayed();
    }

    public String accountName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(accountButton)).getText();
    }

    public String errorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }
}
