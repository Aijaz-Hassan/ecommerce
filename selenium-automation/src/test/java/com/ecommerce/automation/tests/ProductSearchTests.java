package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProductSearchTests extends BaseTest {

    @Test(description = "Product listing page should show visible products")
    public void productListingShowsVisibleProducts() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);

        Assert.assertTrue(productsPage.visibleProductCount() > 0, "Products page should show at least one product.");
        Assert.assertTrue(productsPage.firstProductIsVisible(), "First product card should be visible with key details.");
    }

    @Test(description = "Visible product cards should include image, name, price, stock, and details action")
    public void productCardsShowRequiredDetails() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);

        Assert.assertTrue(
                productsPage.allVisibleProductsHaveRequiredDetails(),
                "Every visible product should show image, name, price, stock, and details action."
        );
    }

    @Test(description = "Product page search should find products by category")
    public void productSearchFindsCategoryProducts() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String category = productsPage.firstAvailableCategory();

        productsPage.search(category);

        Assert.assertTrue(productsPage.visibleProductCount() > 0, "Category search should show matching products.");
        Assert.assertTrue(productsPage.firstProductIsVisible(), "Category search should show visible product cards.");
    }
}
