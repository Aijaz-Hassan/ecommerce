package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OrderDetailsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By orderNumber = By.cssSelector(".section-heading h1");
    private final By tracker = By.cssSelector(".order-tracker");
    private final By purchasedItem = By.cssSelector(".order-item-row");
    private final By billDetails = By.cssSelector(".cart-aside .admin-panel");

    public OrderDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public OrderDetailsPage waitUntilOpen() {
        wait.until(ExpectedConditions.urlContains("/orders/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(orderNumber));
        return this;
    }

    public boolean hasTrackingAndBillDetails() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(tracker)).isDisplayed()
                && wait.until(ExpectedConditions.visibilityOfElementLocated(purchasedItem)).isDisplayed()
                && wait.until(ExpectedConditions.visibilityOfElementLocated(billDetails)).isDisplayed();
    }
}
