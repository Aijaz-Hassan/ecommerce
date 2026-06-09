package com.ecommerce.automation.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AdminDashboardPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By adminWorkspace = By.cssSelector(".admin-workspace");
    private final By adminProfileName = By.cssSelector(".admin-profile-chip strong");

    public AdminDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public AdminDashboardPage waitUntilOpen() {
        wait.until(ExpectedConditions.urlContains("/admin"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminWorkspace));
        return this;
    }

    public boolean isOpen() {
        waitUntilOpen();
        return driver.getCurrentUrl().contains("/admin");
    }

    public String profileName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(adminProfileName)).getText();
    }
}
