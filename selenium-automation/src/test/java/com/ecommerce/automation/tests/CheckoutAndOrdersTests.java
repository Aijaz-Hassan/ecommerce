package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.CartPage;
import com.ecommerce.automation.pages.OrderDetailsPage;
import com.ecommerce.automation.pages.OrdersPage;
import com.ecommerce.automation.pages.ProductsPage;
import com.ecommerce.automation.support.AuthTestHelper;
import com.ecommerce.automation.support.TestUser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckoutAndOrdersTests extends BaseTest {

    @Test(description = "Checkout should not start when recipient name is empty", invocationCount = 1)
    public void checkoutRequiresRecipientName() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        CartPage cartPage = openCartWithProductAfterLogin();

        cartPage.fillDeliveryAddress(customer.fullName())
                .clearRecipientName()
                .startCheckoutExpectingValidation("Please add the recipient name.");
    }

    @Test(description = "Checkout should not start with an 11-digit phone number", invocationCount = 1)
    public void checkoutRejectsElevenDigitPhoneNumber() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        CartPage cartPage = openCartWithProductAfterLogin();

        cartPage.fillDeliveryAddress(customer.fullName())
                .enterPhoneNumber("98765432109")
                .startCheckoutExpectingValidation("Please enter a 10-digit phone number.");
    }

    @Test(description = "Checkout should not start with an unsupported country", invocationCount = 1)
    public void checkoutRejectsInvalidCountry() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        CartPage cartPage = openCartWithProductAfterLogin();

        cartPage.fillDeliveryAddress(customer.fullName())
                .enterCountry("Atlantis")
                .startCheckoutExpectingValidation("Delivery is currently available only in India.");
    }

    @Test(description = "Checkout should not start with an invalid pincode", invocationCount = 1)
    public void checkoutRejectsInvalidPincode() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        CartPage cartPage = openCartWithProductAfterLogin();

        cartPage.fillDeliveryAddress(customer.fullName())
                .enterPostalCode("01234")
                .startCheckoutExpectingValidation("Please enter a valid 6-digit pincode.");
    }

    @Test(description = "Customer can checkout and view the order", invocationCount = 1)
    public void customerCanCheckoutAndViewOrderDetails() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        CartPage cartPage = openCartWithProductAfterLogin();

        cartPage.fillDeliveryAddress(customer.fullName());
        assertDeliveryFormValues(cartPage, customer.fullName());

        String paymentPrompt = cartPage.initiateCheckoutAndCancelPayment();

        Assert.assertTrue(paymentPrompt.contains("Demo payment"), "Valid delivery form should start checkout payment confirmation.");

        cartPage.fillDeliveryAddress(customer.fullName());
        OrdersPage ordersPage = cartPage.checkoutWithDemoPayment();

        Assert.assertTrue(ordersPage.hasOrders(), "Orders page should show the completed checkout order.");
        Assert.assertTrue(ordersPage.openHistory().isHistoryVisible(), "Order history dropdown should open.");

        OrderDetailsPage detailsPage = ordersPage.openFirstOrderDetails();

        Assert.assertTrue(detailsPage.hasTrackingAndBillDetails(), "Order details should show tracking, purchased item, and bill details.");
    }

    private CartPage openCartWithProductAfterLogin() {
        loginAsDefaultCustomer();
        new CartPage(driver).open(baseUrl).clearIfNeeded();

        return new ProductsPage(driver)
                .open(baseUrl)
                .addFirstProductToCart()
                .openCart(baseUrl)
                .waitUntilReadyForCheckout();
    }

    private void assertDeliveryFormValues(CartPage cartPage, String expectedName) {
        Assert.assertEquals(cartPage.recipientName(), expectedName, "Checkout form should keep the recipient name.");
        Assert.assertEquals(cartPage.addressLine1(), "12 Selenium Street", "Checkout form should keep address line 1.");
        Assert.assertEquals(cartPage.addressLine2(), "Automation Block", "Checkout form should keep address line 2.");
        Assert.assertEquals(cartPage.city(), "Srinagar", "Checkout form should keep the city.");
        Assert.assertEquals(cartPage.state(), "Jammu and Kashmir", "Checkout form should keep the state.");
        Assert.assertEquals(cartPage.postalCode(), "190001", "Checkout form should keep the postal code.");
        Assert.assertEquals(cartPage.country(), "India", "Checkout form should keep the country.");
    }

    private TestUser loginAsDefaultCustomer() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        new AuthTestHelper(driver, baseUrl).loginAs(customer);
        return customer;
    }
}
