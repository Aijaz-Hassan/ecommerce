package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProductDetailsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By productTitle = By.cssSelector(".product-info-panel h1");
    private final By productPrice = By.cssSelector(".product-page-price");
    private final By productImage = By.cssSelector(".product-page-image");
    private final By detailsList = By.cssSelector(".details-list");
    private final By stockStatus = By.cssSelector(".buy-box-stock span");
    private final By buyBoxPrice = By.cssSelector(".buy-box-price");
    private final By addToCartButton = By.xpath("//button[normalize-space()='Add to cart']");
    private final By viewCartLink = By.xpath("//a[normalize-space()='View cart']");

    public ProductDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public ProductDetailsPage waitUntilOpen() {
        wait.until(ExpectedConditions.urlContains("/products/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle));
        return this;
    }

    public boolean isOpen() {
        waitUntilOpen();
        return driver.getCurrentUrl().contains("/products/");
    }

    public String productName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle)).getText();
    }

    public String price() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(productPrice)).getText();
    }

    public boolean hasVisibleProductDetails() {
        WebElement image = wait.until(ExpectedConditions.visibilityOfElementLocated(productImage));
        return !productName().isBlank()
                && !price().isBlank()
                && !text(detailsList).isBlank()
                && !text(stockStatus).isBlank()
                && !text(buyBoxPrice).isBlank()
                && image.isDisplayed()
                && hasImageSource(image);
    }

    public ProductDetailsPage addToCart() {
        String expectedProductName = productName();
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton)).click();
        String alertText = wait.until(ExpectedConditions.alertIsPresent()).getText();
        if (!alertText.contains(expectedProductName) || !alertText.contains("added to cart")) {
            throw new AssertionError("Unexpected add-to-cart alert: " + alertText);
        }
        driver.switchTo().alert().accept();
        return this;
    }

    public CartPage viewCart() {
        wait.until(ExpectedConditions.elementToBeClickable(viewCartLink)).click();
        return new CartPage(driver).waitUntilOpen();
    }

    private String text(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    private boolean hasImageSource(WebElement image) {
        String source = image.getAttribute("src");
        return source != null && !source.trim().isEmpty();
    }
}
