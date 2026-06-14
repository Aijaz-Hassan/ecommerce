package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.ProductDetailsPage;
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

    @Test(description = "Empty product search should keep products visible")
    public void emptySearchKeepsProductsVisible() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        int initialProductCount = productsPage.visibleProductCount();

        productsPage.search("");

        Assert.assertTrue(productsPage.visibleProductCount() > 0, "Empty search should keep products visible.");
        Assert.assertEquals(productsPage.visibleProductCount(), initialProductCount, "Empty search should not remove products.");
    }

    @Test(description = "Product page search should find a product by name")
    public void productSearchFindsProductByName() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String productName = productsPage.firstProductName();

        productsPage.search(productName);

        Assert.assertTrue(productsPage.hasProductNamed(productName), "Product search should show the searched product.");
    }

    @Test(description = "Product page search should find products by partial keyword")
    public void productSearchFindsProductsByPartialKeyword() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String expectedProductName = productsPage.firstProductName();
        String partialKeyword = productsPage.partialKeywordFromFirstProductName();

        productsPage.search(partialKeyword);

        Assert.assertTrue(productsPage.visibleProductCount() > 0, "Partial keyword search should show matching products.");
        Assert.assertTrue(productsPage.hasProductNamed(expectedProductName), "Partial keyword should find the matching product.");
    }

    @Test(description = "Category filter should show products for selected category")
    public void categoryFilterShowsMatchingProducts() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String category = productsPage.firstAvailableCategory();

        productsPage.filterByCategory(category);

        Assert.assertTrue(productsPage.visibleProductCount() > 0, "Category filter should show matching products.");
        Assert.assertTrue(productsPage.firstProductIsVisible(), "Filtered product cards should be visible.");
    }

    @Test(description = "Quick View should handle dynamic product modal")
    public void quickViewModalOpensAndCloses() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String productName = productsPage.firstProductName();

        productsPage.openFirstQuickView();

        Assert.assertEquals(productsPage.quickViewProductName(), productName, "Quick View should show selected product.");

        productsPage.closeQuickView();

        Assert.assertTrue(productsPage.isQuickViewClosed(), "Quick View modal should close.");
    }

    @Test(description = "Details button should navigate to product details page with matching product data")
    public void productDetailsPageShowsSelectedProductDetails() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        String expectedName = productsPage.firstProductName();
        String expectedPrice = productsPage.firstProductPrice();

        ProductDetailsPage detailsPage = productsPage.openFirstProductDetails();

        Assert.assertTrue(detailsPage.isOpen(), "Details button should navigate to the product details page.");
        Assert.assertEquals(detailsPage.productName(), expectedName, "Details page should show the selected product name.");
        Assert.assertEquals(detailsPage.price(), expectedPrice, "Details page should show the selected product price.");
        Assert.assertTrue(detailsPage.hasVisibleProductDetails(), "Details page should show image, price, stock, and description.");
    }
}
