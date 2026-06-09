package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
    private final By logoutMenuButton = By.xpath("//div[contains(@class,'account-dropdown')]//button[normalize-space()='Logout']");
    private final By confirmLogoutButton = By.xpath("//div[@role='dialog']//button[normalize-space()='Logout']");

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

    public LoginPage logout() {
        wait.until(ExpectedConditions.elementToBeClickable(accountButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(confirmLogoutButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        wait.until(ExpectedConditions.urlContains("/login"));
        return this;
    }

    public LoginPage openProtectedPageExpectingLogin(String baseUrl, String path) {
        driver.get(baseUrl + path);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        wait.until(ExpectedConditions.urlContains("/login"));
        return this;
    }

    public boolean isAccountMenuVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(accountButton));
        return driver.findElement(accountButton).isDisplayed();
    }

    public boolean isOnLoginPage() {
        wait.until(ExpectedConditions.urlContains("/login"));
        return driver.getCurrentUrl().contains("/login");
    }

    public String accountName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(accountButton)).getText();
    }

    public boolean hasStoredSession() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        return Boolean.TRUE.equals(javascriptExecutor.executeScript(
                "return Boolean(localStorage.getItem('lumenlane_token') && localStorage.getItem('lumenlane_user'));"
        ));
    }

    public String errorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }
}
