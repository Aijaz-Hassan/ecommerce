package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By fullNameInput = By.name("fullName");
    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By errorMessage = By.cssSelector(".error-text");
    private final By submitButton = By.xpath("//button[normalize-space()='Register']");

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
        fillAndSubmit(fullName, email, password);
        acceptSuccessAlertIfPresent();
        wait.until(currentDriver ->
                currentDriver.getCurrentUrl().contains("/login") || !currentDriver.findElements(errorMessage).isEmpty());
        if (!driver.findElements(errorMessage).isEmpty()) {
            throw new AssertionError("Registration should succeed, but error was shown: " + driver.findElement(errorMessage).getText());
        }
        return new LoginPage(driver);
    }

    public RegisterPage registerExpectingError(String fullName, String email, String password) {
        fillAndSubmit(fullName, email, password);
        acceptAlertIfPresent();
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return this;
    }

    public RegisterPage registerExpectingFailure(String fullName, String email, String password) {
        fillAndSubmit(fullName, email, password);
        acceptAlertIfPresent();
        wait.until(currentDriver -> hasErrorMessage() || !firstValidationMessage().isBlank());
        return this;
    }

    public boolean isOnRegisterPage() {
        return driver.getCurrentUrl().contains("/register");
    }

    private void fillAndSubmit(String fullName, String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(fullNameInput)).sendKeys(fullName);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public String errorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public String failureText() {
        if (hasErrorMessage()) {
            return driver.findElement(errorMessage).getText();
        }
        return firstValidationMessage();
    }

    private boolean hasErrorMessage() {
        return !driver.findElements(errorMessage).isEmpty() && driver.findElement(errorMessage).isDisplayed();
    }

    private String firstValidationMessage() {
        for (By input : new By[] { fullNameInput, emailInput, passwordInput }) {
            WebElement element = driver.findElement(input);
            String message = element.getAttribute("validationMessage");
            if (message != null && !message.isBlank()) {
                return message;
            }
        }
        return "";
    }

    private void acceptSuccessAlertIfPresent() {
        acceptAlertIfPresent();
    }

    private void acceptAlertIfPresent() {
        try {
            Alert alert = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (TimeoutException ignored) {

        }
    }
}
