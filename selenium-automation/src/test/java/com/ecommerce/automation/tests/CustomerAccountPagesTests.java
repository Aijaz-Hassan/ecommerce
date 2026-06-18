package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.PasswordPage;
import com.ecommerce.automation.pages.ProfilePage;
import com.ecommerce.automation.pages.SettingsPage;
import com.ecommerce.automation.support.AuthTestHelper;
import com.ecommerce.automation.support.TestUser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerAccountPagesTests extends BaseTest {
    private TestUser customer;

    @BeforeMethod
    public void createCustomer() {
        customer = AuthTestHelper.uniqueCustomer();
        AuthTestHelper auth = new AuthTestHelper(driver, baseUrl);
        auth.register(customer);
        auth.loginAs(customer);
    }

    @Test(description = "Customer can update profile details")
    public void customerCanUpdateProfile() {
        ProfilePage profilePage = new ProfilePage(driver)
                .open(baseUrl)
                .updatePhoneNumber("9876543210");

        Assert.assertTrue(profilePage.wasUpdated(), "Profile page should confirm profile update.");
    }

    @Test(description = "Customer can update account settings")
    public void customerCanUpdateSettings() {
        SettingsPage settingsPage = new SettingsPage(driver)
                .open(baseUrl)
                .selectLanguage("Hindi");

        Assert.assertTrue(settingsPage.wasUpdated(), "Settings page should confirm settings update.");
    }

    @Test(description = "Password page should validate mismatched passwords")
    public void passwordPageValidatesMismatchedPasswords() {
        PasswordPage passwordPage = new PasswordPage(driver)
                .open(baseUrl)
                .submitMismatchedPasswords(customer.password(), "NewPass123!", "Different123!");

        Assert.assertTrue(passwordPage.hasValidationError(), "Password page should show a validation error.");
    }
}
