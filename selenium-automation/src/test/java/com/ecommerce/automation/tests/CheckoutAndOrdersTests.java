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

    @Test(description = "Customer can checkout and view the order")
    public void customerCanCheckoutAndViewOrderDetails() {
        TestUser customer = AuthTestHelper.uniqueCustomer();
        AuthTestHelper auth = new AuthTestHelper(driver, baseUrl);
        auth.register(customer);
        auth.loginAs(customer);

        CartPage cartPage = new ProductsPage(driver)
                .open(baseUrl)
                .addFirstProductToCart()
                .openCart(baseUrl);

        OrdersPage ordersPage = cartPage
                .fillDeliveryAddress(customer.fullName())
                .checkoutWithDemoPayment();

        Assert.assertTrue(ordersPage.hasOrders(), "Orders page should show the completed checkout order.");
        Assert.assertTrue(ordersPage.openHistory().isHistoryVisible(), "Order history dropdown should open.");

        OrderDetailsPage detailsPage = ordersPage.openFirstOrderDetails();

        Assert.assertTrue(detailsPage.hasTrackingAndBillDetails(), "Order details should show tracking, purchased item, and bill details.");
    }
}
