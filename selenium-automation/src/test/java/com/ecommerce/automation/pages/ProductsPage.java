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

    public boolean hasProductNamed(String expectedName) {
        return visibleProducts().stream()
                .map((product) -> product.findElement(productName).getText())
                .anyMatch((name) -> name.equalsIgnoreCase(expectedName));
    }

    public String firstProductName() {
        return visibleProducts().get(0).findElement(productName).getText().trim();
    }

    public String firstProductPrice() {
        return visibleProducts().get(0).findElement(productPrice).getText().trim();
    }

    public ProductDetailsPage openFirstProductDetails() {
        WebElement details = visibleProducts().get(0).findElement(detailsButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", details);
        wait.until(ExpectedConditions.elementToBeClickable(details));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", details);
        return new ProductDetailsPage(driver).waitUntilOpen();
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
}
