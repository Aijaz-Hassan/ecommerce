package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OrdersPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By orderCard = By.cssSelector(".order-card");
    private final By firstTrackLink = By.cssSelector(".order-card .solid-link");
    private final By orderHistoryToggle = By.cssSelector(".history-toggle");
    private final By historyList = By.cssSelector(".history-list");
    private final By loadingPanel = By.cssSelector(".loading-panel");

    public OrdersPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public OrdersPage open(String baseUrl) {
        driver.get(baseUrl + "/orders");
        waitUntilOpen();
        return this;
    }

    public OrdersPage waitUntilOpen() {
        wait.until(ExpectedConditions.urlContains("/orders"));
        wait.until((currentDriver) ->
                !currentDriver.findElements(orderCard).isEmpty()
                        || !currentDriver.findElements(loadingPanel).isEmpty());
        return this;
    }

    public boolean hasOrders() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(orderCard)).isDisplayed();
    }

    public OrdersPage openHistory() {
        wait.until(ExpectedConditions.elementToBeClickable(orderHistoryToggle)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(historyList));
        return this;
    }

    public boolean isHistoryVisible() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(historyList)).isDisplayed();
    }

    public OrderDetailsPage openFirstOrderDetails() {
        click(wait.until(ExpectedConditions.elementToBeClickable(firstTrackLink)));
        return new OrderDetailsPage(driver).waitUntilOpen();
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
