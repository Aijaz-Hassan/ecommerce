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
        loginAsDefaultCustomer();
        new CartPage(driver).open(baseUrl).clearIfNeeded();

        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String productName = productsPage.firstAddableProductName();

        CartPage cartPage = productsPage
                .addFirstProductToCart()
                .openCart(baseUrl);

        Assert.assertTrue(cartPage.itemCount() > 0, "Cart should contain at least one product.");
        Assert.assertTrue(cartPage.hasItemNamed(productName), "Cart should contain the product added from listing.");
        Assert.assertEquals(cartPage.firstItemQuantity(), 1, "New product quantity should be 1.");
        assertCartPrices(cartPage);

        cartPage.increaseFirstItemQuantity();
        Assert.assertEquals(cartPage.firstItemQuantity(), 2, "Quantity should increase to 2.");
        assertCartPrices(cartPage);

        cartPage.decreaseFirstItemQuantity();
        Assert.assertEquals(cartPage.firstItemQuantity(), 1, "Quantity should decrease back to 1.");
        assertCartPrices(cartPage);

        Assert.assertTrue(cartPage.hasItemNamed(productName), "Product should be visible before removing it from cart.");
        cartPage.removeFirstItem();
        Assert.assertTrue(cartPage.hasNoItemNamed(productName), "Removed product should no longer be visible in the cart.");
        Assert.assertTrue(cartPage.isEmpty(), "Cart should show empty state after removing the only product.");

        ProductDetailsPage detailsPage = new ProductsPage(driver)
                .open(baseUrl)
                .openFirstAddableProductDetails();
        String detailsProductName = detailsPage.productName();

        CartPage detailsCartPage = detailsPage
                .addToCart()
                .viewCart();

        Assert.assertTrue(detailsCartPage.hasItemNamed(detailsProductName), "Cart should contain product added from details page.");
        Assert.assertEquals(detailsCartPage.firstItemQuantity(), 1, "Details page add-to-cart quantity should be 1.");
        assertCartPrices(detailsCartPage);
    }

    private void loginAsDefaultCustomer() {
        TestUser customer = AuthTestHelper.defaultCustomer();
        AuthTestHelper auth = new AuthTestHelper(driver, baseUrl);
        auth.loginAs(customer);
    }

    private void assertCartPrices(CartPage cartPage) {
        double expectedLineTotal = cartPage.firstItemUnitPrice() * cartPage.firstItemQuantity();
        double expectedSubtotal = expectedLineTotal;
        double expectedDiscount = 0;
        double expectedShipping = expectedSubtotal >= 500 ? 0 : 49;
        double expectedTax = roundToTwoDecimals((expectedSubtotal - expectedDiscount) * 0.18);
        double expectedFinalTotal = roundToTwoDecimals(expectedSubtotal - expectedDiscount + expectedTax + expectedShipping);

        Assert.assertEquals(cartPage.firstItemLineTotal(), expectedLineTotal, 0.05, "Cart item line total should match price multiplied by quantity.");
        Assert.assertEquals(cartPage.subtotal(), expectedSubtotal, 0.05, "Cart subtotal should match item totals.");
        Assert.assertEquals(cartPage.discount(), expectedDiscount, 0.05, "Cart discount should be zero when no coupon is applied.");
        Assert.assertEquals(cartPage.shipping(), expectedShipping, 0.05, "Cart shipping should match the cart shipping rule.");
        Assert.assertEquals(cartPage.tax(), expectedTax, 0.05, "Cart tax should be 18% of subtotal after discount.");
        Assert.assertEquals(cartPage.finalTotal(), expectedFinalTotal, 0.05, "Cart final total should include subtotal, tax, shipping, and discount.");
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
