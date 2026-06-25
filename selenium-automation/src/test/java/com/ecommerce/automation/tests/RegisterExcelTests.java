package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.LoginPage;
import com.ecommerce.automation.pages.RegisterPage;
import com.ecommerce.automation.support.RegisterExcelData;
import com.ecommerce.automation.support.RegisterTestData;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RegisterExcelTests extends BaseTest {

    @DataProvider(name = "validRegisterRows")
    public Object[][] validRegisterRows() {
        return RegisterExcelData.rows("Valid_Register");
    }

    @DataProvider(name = "invalidRegisterRows")
    public Object[][] invalidRegisterRows() {
        return RegisterExcelData.rows("Invalid_Register");
    }

    @Test(dataProvider = "validRegisterRows", description = "Valid registration data from Excel should create accounts")
    public void validRegisterDataFromExcelShouldSucceed(RegisterTestData data) {
        LoginPage loginPage = new RegisterPage(driver)
                .open(baseUrl)
                .register(data.fullName(), data.email(), data.password());

        Assert.assertTrue(loginPage.isOnLoginPage(), data.testCaseId() + " should redirect to login after registration.");
    }

    @Test(dataProvider = "invalidRegisterRows", description = "Invalid registration data from Excel should show validation")
    public void invalidRegisterDataFromExcelShouldFail(RegisterTestData data) {
        RegisterPage registerPage = new RegisterPage(driver)
                .open(baseUrl)
                .registerExpectingFailure(data.fullName(), data.email(), data.password());

        Assert.assertTrue(registerPage.isOnRegisterPage(), data.testCaseId() + " should stay on the register page.");
        Assert.assertFalse(registerPage.failureText().isBlank(), data.testCaseId() + " should show a validation message.");
        if (data.password().isBlank()) {
            Assert.assertTrue(
                    registerPage.failureText().contains("Password"),
                    data.testCaseId() + " should show a password validation message."
            );
            return;
        }
        if (!isBrowserEmailValidation(data)) {
            Assert.assertTrue(
                    registerPage.failureText().contains(data.expectedMessage()),
                    data.testCaseId() + " should show expected message: " + data.expectedMessage()
            );
        }
    }

    private boolean isBrowserEmailValidation(RegisterTestData data) {
        return data.expectedMessage().equals("Please enter a valid email")
                && !data.email().isBlank()
                && !data.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}
