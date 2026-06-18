package com.ecommerce.automation.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By cartItem = By.cssSelector(".premium-cart-item");
    private final By itemName = By.cssSelector(".premium-cart-copy h2");
    private final By quantityValue = By.cssSelector(".quantity-selector span");
    private final By increaseButton = By.xpath(".//div[contains(@class,'quantity-selector')]/button[normalize-space()='+']");
    private final By decreaseButton = By.xpath(".//div[contains(@class,'quantity-selector')]/button[normalize-space()='-']");
    private final By removeButton = By.xpath(".//div[contains(@class,'cart-item-actions')]/button[normalize-space()='Remove']");
    private final By emptyCartState = By.cssSelector(".empty-cart-state");
    private final By checkoutButton = By.xpath("//button[normalize-space()='Proceed to Checkout']");
    private final By toastMessage = By.cssSelector(".floating-toast");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public CartPage open(String baseUrl) {
        driver.get(baseUrl + "/cart");
        return waitUntilOpen();
    }

    public CartPage waitUntilOpen() {
        wait.until((currentDriver) ->
                !currentDriver.findElements(cartItem).isEmpty()
                        || !currentDriver.findElements(emptyCartState).isEmpty());
        return this;
    }

    public int itemCount() {
        return cartItems().size();
    }

    public boolean hasItemNamed(String expectedName) {
        return cartItems().stream()
                .map((item) -> item.findElement(itemName).getText().trim())
                .anyMatch((name) -> name.equalsIgnoreCase(expectedName));
    }

    public int firstItemQuantity() {
        String quantity = firstCartItem().findElement(quantityValue).getText().trim();
        return Integer.parseInt(quantity);
    }

    public CartPage increaseFirstItemQuantity() {
        int currentQuantity = firstItemQuantity();
        firstCartItem().findElement(increaseButton).click();
        wait.until((driver) -> firstItemQuantity() == currentQuantity + 1);
        return this;
    }

    public CartPage decreaseFirstItemQuantity() {
        int currentQuantity = firstItemQuantity();
        firstCartItem().findElement(decreaseButton).click();
        wait.until((driver) -> firstItemQuantity() == currentQuantity - 1);
        return this;
    }

    public CartPage removeFirstItem() {
        firstCartItem().findElement(removeButton).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartState));
        return this;
    }

    public boolean isEmpty() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartState)).isDisplayed();
    }

    public CartPage fillDeliveryAddress(String fullName) {
        type("recipientName", fullName);
        type("phoneNumber", "9876543210");
        type("addressLine1", "12 Selenium Street");
        type("addressLine2", "Automation Block");
        type("city", "Srinagar");
        type("state", "Jammu and Kashmir");
        type("postalCode", "190001");
        type("country", "India");
        return this;
    }

    public OrdersPage checkoutWithDemoPayment() {
        click(wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)));
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(toastMessage, "Demo payment successful"));
        return new OrdersPage(driver).open(currentBaseUrl());
    }

    private WebElement firstCartItem() {
        return cartItems().get(0);
    }

    private List<WebElement> cartItems() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(cartItem));
    }

    private void type(String name, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.name(name)));
        input.clear();
        input.sendKeys(value);
    }

    private String currentBaseUrl() {
        String url = driver.getCurrentUrl();
        int pathStart = url.indexOf("/", url.indexOf("//") + 2);
        return pathStart > 0 ? url.substring(0, pathStart) : url;
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
