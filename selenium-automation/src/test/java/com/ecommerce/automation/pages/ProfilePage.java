package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProfilePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By heading = By.cssSelector(".section-heading h1");
    private final By editButton = By.xpath("//button[normalize-space()='Edit Profile']");
    private final By saveButton = By.xpath("//button[normalize-space()='Save Profile']");
    private final By successMessage = By.cssSelector(".toast-message.success");

    public ProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public ProfilePage open(String baseUrl) {
        driver.get(baseUrl + "/profile");
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        return this;
    }

    public ProfilePage updatePhoneNumber(String phoneNumber) {
        wait.until(ExpectedConditions.elementToBeClickable(editButton)).click();
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.name("phoneNumber")));
        input.clear();
        input.sendKeys(phoneNumber);
        click(wait.until(ExpectedConditions.elementToBeClickable(saveButton)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        return this;
    }

    public boolean wasUpdated() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).isDisplayed();
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
