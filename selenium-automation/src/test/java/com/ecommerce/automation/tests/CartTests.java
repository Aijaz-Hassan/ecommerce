package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.CartPage;
import com.ecommerce.automation.pages.ProductDetailsPage;
import com.ecommerce.automation.pages.ProductsPage;
import com.ecommerce.automation.support.AuthTestHelper;
import com.ecommerce.automation.support.TestUser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CartTests extends BaseTest {

    @Test(description = "Customer can complete cart actions in one login")
    public void customerCanCompleteCartActionsInOneLogin() {
        loginAsNewCustomer();

        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String productName = productsPage.firstAddableProductName();

        CartPage cartPage = productsPage
                .addFirstProductToCart()
                .openCart(baseUrl);

        Assert.assertTrue(cartPage.itemCount() > 0, "Cart should contain at least one product.");
        Assert.assertTrue(cartPage.hasItemNamed(productName), "Cart should contain the product added from listing.");
        Assert.assertEquals(cartPage.firstItemQuantity(), 1, "New product quantity should be 1.");

        cartPage.increaseFirstItemQuantity();
        Assert.assertEquals(cartPage.firstItemQuantity(), 2, "Quantity should increase to 2.");

        cartPage.decreaseFirstItemQuantity();
        Assert.assertEquals(cartPage.firstItemQuantity(), 1, "Quantity should decrease back to 1.");

        cartPage.removeFirstItem();
        Assert.assertTrue(cartPage.isEmpty(), "Cart should show empty state after removing the only product.");

        ProductDetailsPage detailsPage = new ProductsPage(driver)
                .open(baseUrl)
                .openFirstProductDetails();
        String detailsProductName = detailsPage.productName();

        CartPage detailsCartPage = detailsPage
                .addToCart()
                .viewCart();

        Assert.assertTrue(detailsCartPage.hasItemNamed(detailsProductName), "Cart should contain product added from details page.");
        Assert.assertEquals(detailsCartPage.firstItemQuantity(), 1, "Details page add-to-cart quantity should be 1.");
    }

    private void loginAsNewCustomer() {
        TestUser customer = AuthTestHelper.uniqueCustomer();
        AuthTestHelper auth = new AuthTestHelper(driver, baseUrl);
        auth.register(customer);
        auth.loginAs(customer);
    }
}
