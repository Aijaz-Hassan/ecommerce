package com.ecommerce.automation.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
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
    private final By unitPrice = By.cssSelector(".cart-quantity-panel > strong");
    private final By lineTotal = By.cssSelector(".cart-quantity-panel em");
    private final By increaseButton = By.xpath(".//div[contains(@class,'quantity-selector')]/button[normalize-space()='+']");
    private final By decreaseButton = By.xpath(".//div[contains(@class,'quantity-selector')]/button[normalize-space()='-']");
    private final By removeButton = By.xpath(".//div[contains(@class,'cart-item-actions')]/button[normalize-space()='Remove']");
    private final By emptyCartState = By.cssSelector(".empty-cart-state");
    private final By removeAllButton = By.xpath("//button[contains(normalize-space(),'Remove All')]");
    private final By checkoutButton = By.xpath("//button[normalize-space()='Proceed to Checkout']");
    private final By toastMessage = By.cssSelector(".floating-toast");
    private final By addressLoading = By.cssSelector(".saved-address-loading");

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

    public CartPage waitUntilReadyForCheckout() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutButton));
        waitUntilAddressFormReady();
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

    public boolean hasNoItemNamed(String removedName) {
        try {
            return driver.findElements(cartItem).stream()
                    .map((item) -> item.findElement(itemName).getText().trim())
                    .noneMatch((name) -> name.equalsIgnoreCase(removedName));
        } catch (StaleElementReferenceException staleElement) {
            return false;
        }
    }

    public int firstItemQuantity() {
        String quantity = firstCartItem().findElement(quantityValue).getText().trim();
        return Integer.parseInt(quantity);
    }

    public double firstItemUnitPrice() {
        return money(firstCartItem().findElement(unitPrice).getText());
    }

    public double firstItemLineTotal() {
        return money(firstCartItem().findElement(lineTotal).getText());
    }

    public double subtotal() {
        return summaryAmount("Subtotal");
    }

    public double discount() {
        return summaryAmount("Discount");
    }

    public double shipping() {
        return summaryAmount("Shipping");
    }

    public double tax() {
        return summaryAmount("Tax");
    }

    public double finalTotal() {
        return summaryAmount("Final total");
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
        WebElement item = firstCartItem();
        String removedName = item.findElement(itemName).getText().trim();
        click(item.findElement(removeButton));
        wait.until((driver) -> hasNoItemNamed(removedName));
        wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartState));
        return this;
    }

    public boolean isEmpty() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartState)).isDisplayed();
    }

    public CartPage clearIfNeeded() {
        if (driver.findElements(cartItem).isEmpty()) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartState));
            return this;
        }

        click(wait.until(ExpectedConditions.elementToBeClickable(removeAllButton)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartState));
        return this;
    }

    public CartPage fillDeliveryAddress(String fullName) {
        waitUntilAddressFormReady();
        type("recipientName", fullName);
        type("phoneNumber", "9876543210");
        type("addressLine1", "12 Selenium Street");
        type("addressLine2", "Automation Block");
        type("city", "Srinagar");
        type("state", "Jammu and Kashmir");
        type("postalCode", "190001");
        type("country", "India");
        wait.until((driver) -> fullName.equals(value("recipientName"))
                && "12 Selenium Street".equals(value("addressLine1"))
                && "Srinagar".equals(value("city")));
        return this;
    }

    public String recipientName() {
        return value("recipientName");
    }

    public String addressLine1() {
        return value("addressLine1");
    }

    public String addressLine2() {
        return value("addressLine2");
    }

    public String city() {
        return value("city");
    }

    public String state() {
        return value("state");
    }

    public String postalCode() {
        return value("postalCode");
    }

    public String country() {
        return value("country");
    }

    public CartPage clearRequiredDeliveryFields() {
        clear("recipientName");
        clear("phoneNumber");
        clear("addressLine1");
        clear("city");
        clear("state");
        clear("postalCode");
        clear("country");
        return this;
    }

    public CartPage clearRecipientName() {
        clear("recipientName");
        return this;
    }

    public CartPage enterPhoneNumber(String phoneNumber) {
        type("phoneNumber", phoneNumber);
        return this;
    }

    public CartPage enterPostalCode(String postalCode) {
        type("postalCode", postalCode);
        return this;
    }

    public CartPage enterCountry(String country) {
        type("country", country);
        return this;
    }

    public CartPage enterRecipientName(String fullName) {
        type("recipientName", fullName);
        return this;
    }

    public CartPage startCheckoutExpectingValidation(String expectedMessage) {
        click(wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(toastMessage, expectedMessage));
        return this;
    }

    public String initiateCheckoutAndCancelPayment() {
        click(wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)));
        String alertText = wait.until(ExpectedConditions.alertIsPresent()).getText();
        driver.switchTo().alert().dismiss();
        return alertText;
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

    private double summaryAmount(String label) {
        String amount = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'summary-line')][span[normalize-space()='" + label + "']]/strong")
        )).getText();
        return money(amount);
    }

    private double money(String value) {
        String normalized = value.replaceAll("[^0-9.\\-]", "");
        return normalized.isBlank() ? 0 : Double.parseDouble(normalized);
    }

    private void type(String name, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.name(name)));
        ((JavascriptExecutor) driver).executeScript(
                """
                    const input = arguments[0];
                    const value = arguments[1];
                    const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                    setter.call(input, value);
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                """,
                input,
                value
        );
        wait.until((driver) -> value.equals(input.getAttribute("value")));
    }

    private void waitUntilAddressFormReady() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("recipientName")));
        wait.until((driver) -> driver.findElements(addressLoading).isEmpty());
    }

    private String value(String name) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name))).getAttribute("value");
    }

    private void clear(String name) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.name(name)));
        ((JavascriptExecutor) driver).executeScript(
                """
                    const input = arguments[0];
                    const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                    setter.call(input, '');
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                """,
                input
        );
        wait.until((driver) -> input.getAttribute("value").isEmpty());
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
