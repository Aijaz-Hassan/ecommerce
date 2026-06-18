package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SettingsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By heading = By.cssSelector(".section-heading h1");
    private final By languageSelect = By.cssSelector(".settings-select select");
    private final By saveButton = By.cssSelector(".settings-save");
    private final By successMessage = By.cssSelector(".toast-message.success");

    public SettingsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public SettingsPage open(String baseUrl) {
        driver.get(baseUrl + "/settings");
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        return this;
    }

    public SettingsPage selectLanguage(String language) {
        new Select(wait.until(ExpectedConditions.elementToBeClickable(languageSelect))).selectByVisibleText(language);
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
