package com.ecommerce.automation.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProductsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageTitle = By.cssSelector(".product-hero-bar h1");
    private final By searchInput = By.cssSelector(".product-search-panel input");
    private final By categoryDropdown = By.cssSelector(".filter-group select");
    private final By productCards = By.cssSelector(".premium-product-card:not(.product-skeleton)");
    private final By productName = By.cssSelector(".premium-card-body h2");
    private final By productImage = By.cssSelector(".premium-card-media img");
    private final By productPrice = By.cssSelector(".price-row strong");
    private final By stockText = By.cssSelector(".stock-text");
    private final By detailsButton = By.xpath(".//button[normalize-space()='Details']");
    private final By addToCartButton = By.xpath(".//button[normalize-space()='Add to Cart']");
    private final By quickViewButton = By.cssSelector(".quick-view-pill");
    private final By quickViewModal = By.cssSelector(".product-detail-modal");
    private final By quickViewTitle = By.cssSelector(".product-detail-modal h2");
    private final By quickViewCloseButton = By.cssSelector(".quick-view-close");
    private final By toastMessage = By.cssSelector(".floating-toast");

    public ProductsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public ProductsPage open(String baseUrl) {
        driver.get(baseUrl + "/products");
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(productCards, 0));
        return this;
    }

    public int visibleProductCount() {
        return visibleProducts().size();
    }

    public ProductsPage search(String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        input.clear();
        input.sendKeys(value);
        wait.until(driver -> !driver.findElements(productCards).isEmpty());
        return this;
    }

    public String firstAvailableCategory() {
        Select categorySelect = new Select(wait.until(ExpectedConditions.elementToBeClickable(categoryDropdown)));
        return categorySelect.getOptions().stream()
                .map((option) -> option.getText().trim())
                .filter((category) -> !category.isEmpty() && !"All Categories".equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No product categories are available."));
    }

    public ProductsPage filterByCategory(String category) {
        Select categorySelect = new Select(wait.until(ExpectedConditions.elementToBeClickable(categoryDropdown)));
        categorySelect.selectByVisibleText(category);
        wait.until(driver -> !driver.findElements(productCards).isEmpty());
        return this;
    }

    public boolean hasProductNamed(String expectedName) {
        return visibleProducts().stream()
                .map((product) -> product.findElement(productName).getText())
                .anyMatch((name) -> name.equalsIgnoreCase(expectedName));
    }

    public String firstProductName() {
        return visibleProducts().get(0).findElement(productName).getText().trim();
    }

    public String firstAddableProductName() {
        return firstAddableProduct().findElement(productName).getText().trim();
    }

    public String partialKeywordFromFirstProductName() {
        String firstWord = firstProductName().split("\\s+")[0];
        return firstWord.substring(0, Math.min(4, firstWord.length()));
    }

    public String firstProductPrice() {
        return visibleProducts().get(0).findElement(productPrice).getText().trim();
    }

    public ProductDetailsPage openFirstProductDetails() {
        WebElement details = visibleProducts().get(0).findElement(detailsButton);
        click(details);
        return new ProductDetailsPage(driver).waitUntilOpen();
    }

    public ProductDetailsPage openFirstAddableProductDetails() {
        WebElement details = firstAddableProduct().findElement(detailsButton);
        click(details);
        return new ProductDetailsPage(driver).waitUntilOpen();
    }

    public ProductsPage addFirstProductToCart() {
        String expectedProductName = firstAddableProductName();
        WebElement button = firstAddableProduct().findElement(addToCartButton);
        click(button);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(toastMessage, expectedProductName + " added to cart."));
        return this;
    }

    public CartPage openCart(String baseUrl) {
        return new CartPage(driver).open(baseUrl);
    }

    public ProductsPage openFirstQuickView() {
        WebElement quickView = visibleProducts().get(0).findElement(quickViewButton);
        click(quickView);
        wait.until(ExpectedConditions.visibilityOfElementLocated(quickViewModal));
        return this;
    }

    public String quickViewProductName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(quickViewTitle)).getText().trim();
    }

    public ProductsPage closeQuickView() {
        click(wait.until(ExpectedConditions.elementToBeClickable(quickViewCloseButton)));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(quickViewModal));
        return this;
    }

    public boolean isQuickViewClosed() {
        return driver.findElements(quickViewModal).isEmpty();
    }

    public boolean firstProductIsVisible() {
        return visibleProducts().get(0).isDisplayed();
    }

    public boolean allVisibleProductsHaveRequiredDetails() {
        return visibleProducts().stream().allMatch(this::productHasRequiredDetails);
    }

    private List<WebElement> visibleProducts() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productCards));
    }

    private WebElement firstAddableProduct() {
        return visibleProducts().stream()
                .filter((product) -> !product.findElements(addToCartButton).isEmpty())
                .filter((product) -> product.findElement(addToCartButton).isEnabled())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No addable product is available."));
    }

    private boolean productHasRequiredDetails(WebElement product) {
        WebElement image = product.findElement(productImage);
        return hasText(product, productName)
                && hasText(product, productPrice)
                && hasText(product, stockText)
                && product.findElement(detailsButton).isDisplayed()
                && image.isDisplayed()
                && hasImageSource(image);
    }

    private boolean hasText(WebElement product, By locator) {
        return !product.findElement(locator).getText().trim().isEmpty();
    }

    private boolean hasImageSource(WebElement image) {
        String source = image.getAttribute("src");
        return source != null && !source.trim().isEmpty();
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
