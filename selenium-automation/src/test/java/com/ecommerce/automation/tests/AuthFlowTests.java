package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.AdminDashboardPage;
import com.ecommerce.automation.pages.LoginPage;
import com.ecommerce.automation.pages.RegisterPage;
import com.ecommerce.automation.support.AuthTestHelper;
import com.ecommerce.automation.support.TestUser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthFlowTests extends BaseTest {
    private static TestUser registeredCustomer;

    @Test(priority = 1, description = "Login page should show an error for invalid credentials")
    public void invalidLoginShowsError() {
        LoginPage loginPage = auth()
                .loginWithCredentials("missing.user@example.com", "Wrong12345");

        Assert.assertFalse(loginPage.errorText().isBlank(), "Invalid login should show an error message.");
    }

    @Test(priority = 2, description = "Registration should show an error for invalid details")
    public void invalidRegistrationShowsError() {
        RegisterPage registerPage = auth()
                .registerExpectingError(AuthTestHelper.invalidCustomer());

        Assert.assertTrue(registerPage.isOnRegisterPage(), "Invalid registration should keep the user on register page.");
        Assert.assertFalse(registerPage.errorText().isBlank(), "Invalid registration should show an error message.");
    }

    @Test(priority = 3, description = "Registration should succeed with valid details")
    public void validRegistrationRedirectsToLogin() {
        registeredCustomer = AuthTestHelper.uniqueCustomer();

        LoginPage loginPage = auth().register(registeredCustomer);

        Assert.assertTrue(loginPage.isOnLoginPage(), "Valid registration should redirect to the login page.");
    }

    @Test(priority = 4, description = "Customer login should succeed with valid credentials")
    public void userLoginUsesSharedLoginUtility() {
        TestUser user = ensureRegisteredCustomer();

        LoginPage loginPage = auth().loginAs(user);

        Assert.assertTrue(loginPage.isAccountMenuVisible(), "Account menu should be visible after login.");
        Assert.assertTrue(loginPage.accountName().contains(user.fullName()), "Logged-in user's full name should be shown.");
    }

    @Test(priority = 5, description = "Admin login should succeed through the same login utility")
    public void adminLoginUsesSharedLoginUtility() {
        TestUser admin = AuthTestHelper.defaultAdmin();

        AdminDashboardPage adminDashboardPage = auth()
                .loginAs(admin)
                .adminDashboard();

        Assert.assertTrue(adminDashboardPage.isOpen(), "Admin users should land on the admin dashboard.");
        Assert.assertFalse(adminDashboardPage.profileName().isBlank(), "Admin profile name should be visible.");
    }

    @Test(priority = 6, description = "Logout should end the active user session")
    public void logoutClearsSessionAndRedirectsToLogin() {
        TestUser user = ensureRegisteredCustomer();

        LoginPage loginPage = auth().loginAs(user);

        Assert.assertTrue(loginPage.hasStoredSession(), "Login should store the active session.");

        loginPage.logout();

        Assert.assertTrue(loginPage.isOnLoginPage(), "Logout should redirect to the login page.");
        Assert.assertFalse(loginPage.hasStoredSession(), "Logout should clear token and user session data.");
    }

    @Test(priority = 7, description = "Logged-out users should be redirected away from protected pages")
    public void loggedOutSessionCannotAccessProtectedPage() {
        TestUser user = ensureRegisteredCustomer();

        LoginPage loginPage = auth()
                .loginAs(user)
                .logout()
                .openProtectedPageExpectingLogin(baseUrl, "/profile");

        Assert.assertTrue(loginPage.isOnLoginPage(), "Protected pages should redirect logged-out users to login.");
        Assert.assertFalse(loginPage.hasStoredSession(), "Logged-out users should not keep local session data.");
    }

    private AuthTestHelper auth() {
        return new AuthTestHelper(driver, baseUrl);
    }

    private TestUser ensureRegisteredCustomer() {
        if (registeredCustomer == null) {
            registeredCustomer = AuthTestHelper.uniqueCustomer();
            auth().register(registeredCustomer);
        }
        return registeredCustomer;
    }
}
