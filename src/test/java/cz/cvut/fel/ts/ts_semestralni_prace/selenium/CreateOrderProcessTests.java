package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CartPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CheckoutPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.OrderConfirmationPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ShopDetailPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ShopDetailPage.ProductInfo;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreateOrderProcessTests extends SeleniumBaseTest {

    private static final String SHOP_A = "shop-1";
    private static final String SHOP_B = "shop-2";

    private static final String CUSTOMER_FIRST_NAME = "Jan";
    private static final String CUSTOMER_LAST_NAME = "Novák";
    private static final String CUSTOMER_EMAIL = "jan.novak@example.com";
    private static final String CUSTOMER_PHONE = "+420777123456";

    private ShopDetailPage shopPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private OrderConfirmationPage confirmationPage;

    @BeforeEach
    void setUpPages() {
        loginAsUser();
        shopPage = new ShopDetailPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        confirmationPage = new OrderConfirmationPage(driver);
    }

    // ---------- CPT path 1: e1,e2,e3,e4,e5,e7,e8  (happy path, merge existing item) ----------
    @Test
    @Order(1)
    @DisplayName(
        "P1 — add same product twice (merge) → checkout → order confirmed"
    )
    void path1_mergeExistingItem_orderConfirmed() {
        shopPage.open(baseUrl(), SHOP_A);
        ProductInfo product = highStockProduct();

        shopPage.addProductByName(product.name(), 1);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(product.name(), 1);
        submitValidCheckout();

        assertThat(confirmationPage.isOnConfirmationPage())
            .as("P1 — confirmation page must be shown")
            .isTrue();
    }

    // ---------- CPT path 2: e1,e2,e3,e4,e6,e7,e8  (happy path, add new item) ----------
    @Test
    @Order(2)
    @DisplayName("P2 — add new product → checkout → order confirmed")
    void path2_addNewItem_orderConfirmed() {
        shopPage.open(baseUrl(), SHOP_A);
        List<ProductInfo> products = shopPage.getAvailableProducts();
        ProductInfo productA = products.get(0);
        ProductInfo productB = products.get(1);

        shopPage.addProductByName(productA.name(), 1);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(productB.name(), 1);

        submitValidCheckout();

        assertThat(confirmationPage.isOnConfirmationPage())
            .as("P2 — confirmation page must be shown")
            .isTrue();
    }

    // ---------- CPT path 3: e1,e9  (product not found → redirect /) ----------
    @Test
    @Order(3)
    @DisplayName("P3 — add non-existent product → error \"Produkt nenalezen\"")
    void path3_productNotFound_redirectsHomeWithError() {
        driver.get(baseUrl() + "/");
        postAddToCart("this-id-does-not-exist", 1);

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
            d.getCurrentUrl().matches(".*:\\d+/?$")
        );
        assertThat(flashErrorText())
            .as("P3 — flash error must indicate missing product")
            .contains("Produkt nenalezen");
    }

    // ---------- CPT path 4: e1,e2,e10  (insufficient stock → redirect /shop/{id}) ----------
    @Test
    @Order(4)
    @DisplayName("P4 — quantity above stock → error \"Nedostatek zásob\"")
    void path4_insufficientStock_redirectsShopWithError() {
        shopPage.open(baseUrl(), SHOP_A);
        ProductInfo product = highStockProduct();
        int overStock = product.stock() + 1;

        postAddToCart(product.id(), overStock);

        waitForPath("/shop/" + SHOP_A);
        assertThat(flashErrorText())
            .as("P4 — flash error must indicate insufficient stock")
            .contains("Nedostatek zásob");
    }

    // ---------- CPT path 5: e1,e2,e3,e11  (different shop → redirect /shop/{id}) ----------
    @Test
    @Order(5)
    @DisplayName(
        "P5 — add product from different shop → error \"jiné zmrzlinárny\""
    )
    void path5_differentShop_redirectsShopWithError() {
        shopPage.open(baseUrl(), SHOP_B);
        ProductInfo productShopB = shopPage.getAvailableProducts().get(0);

        shopPage.open(baseUrl(), SHOP_A);
        ProductInfo productShopA = shopPage.getAvailableProducts().get(0);
        shopPage.addProductByName(productShopA.name(), 1);

        driver.get(baseUrl() + "/shop/" + SHOP_B);
        postAddToCart(productShopB.id(), 1);

        waitForPath("/shop/" + SHOP_B);
        assertThat(flashErrorText())
            .as("P5 — flash error must indicate cross-shop conflict")
            .contains("jiné zmrzlinárny");
    }

    // ---------- CPT path 6: e1,e2,e3,e4,e5,e12  (merge then cart emptied before checkout) ----------
    @Test
    @Order(6)
    @DisplayName(
        "P6 — merge then clear cart → checkout error \"Košík je prázdný\""
    )
    void path6_mergeThenEmptyCart_checkoutError() {
        shopPage.open(baseUrl(), SHOP_A);
        ProductInfo product = highStockProduct();

        shopPage.addProductByName(product.name(), 1);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(product.name(), 1);

        cartPage.open(baseUrl());
        cartPage.clearCart();

        driver.get(baseUrl() + "/order/checkout");

        waitForPath("/cart");
        assertThat(flashErrorText())
            .as("P6 — flash error must indicate empty cart")
            .contains("Košík je prázdný");
    }

    // ---------- CPT path 7: e1,e2,e3,e4,e6,e12  (add new then cart emptied before checkout) ----------
    @Test
    @Order(7)
    @DisplayName(
        "P7 — add new then clear cart → checkout error \"Košík je prázdný\""
    )
    void path7_addNewThenEmptyCart_checkoutError() {
        shopPage.open(baseUrl(), SHOP_A);
        ProductInfo product = highStockProduct();

        shopPage.addProductByName(product.name(), 1);

        cartPage.open(baseUrl());
        cartPage.clearCart();

        driver.get(baseUrl() + "/order/checkout");

        waitForPath("/cart");
        assertThat(flashErrorText())
            .as("P7 — flash error must indicate empty cart")
            .contains("Košík je prázdný");
    }

    // ---------- CPT path 8: e1,e2,e3,e4,e5,e7,e13  (total below min on final POST) ----------
    @Test
    @Order(8)
    @DisplayName(
        "P8 — total below 50 Kč → checkout error \"Minimální hodnota\""
    )
    void path8_totalBelowMinimum_checkoutError() {
        shopPage.open(baseUrl(), SHOP_A);
        ProductInfo cheapest = cheapestAvailableProduct();
        assertThat(cheapest.price())
            .as(
                "Precondition: cheapest product must stay below the 50 Kč minimum"
            )
            .isLessThan(50);

        shopPage.addProductByName(cheapest.name(), 1);

        driver.get(baseUrl() + "/order/checkout");
        fillValidCustomerDetails();
        checkoutPage.submit();

        waitForPath("/cart");
        assertThat(flashErrorText())
            .as("P8 — flash error must indicate minimum order value")
            .contains("Minimální hodnota");
    }

    // ---------- shared helpers ----------

    private void submitValidCheckout() {
        driver.get(baseUrl() + "/order/checkout");
        fillValidCustomerDetails();
        checkoutPage.submit();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.urlContains("/order/confirmation")
        );
    }

    private void fillValidCustomerDetails() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        checkoutPage.fillContactDetails(
            CUSTOMER_FIRST_NAME,
            CUSTOMER_LAST_NAME,
            CUSTOMER_EMAIL,
            CUSTOMER_PHONE
        );
    }

    private void postAddToCart(String productId, int quantity) {
        String script =
            "const csrfInput = document.querySelector('input[name=\"_csrf\"]');" +
            "const form = document.createElement('form');" +
            "form.method = 'POST';" +
            "form.action = arguments[0];" +
            "if (csrfInput) {" +
            "  const c = document.createElement('input');" +
            "  c.type = 'hidden'; c.name = '_csrf'; c.value = csrfInput.value;" +
            "  form.appendChild(c);" +
            "}" +
            "const q = document.createElement('input');" +
            "q.type = 'hidden'; q.name = 'quantity'; q.value = arguments[1];" +
            "form.appendChild(q);" +
            "document.body.appendChild(form);" +
            "form.submit();";
        ((JavascriptExecutor) driver).executeScript(
            script,
            "/cart/add/" + productId,
            Integer.toString(quantity)
        );
    }

    private void waitForPath(String pathSuffix) {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.urlContains(pathSuffix)
        );
    }

    private String flashErrorText() {
        List<WebElement> errors = driver.findElements(
            By.cssSelector(".alert-danger")
        );
        return errors.isEmpty() ? "" : errors.get(0).getText();
    }

    private ProductInfo highStockProduct() {
        return shopPage.getAvailableProducts().stream()
            .max(Comparator.comparingInt(ProductInfo::stock))
            .orElseThrow(() ->
                new IllegalStateException("No available products on shop page")
            );
    }

    private ProductInfo cheapestAvailableProduct() {
        return shopPage.getAvailableProducts().stream()
            .min(Comparator.comparingDouble(ProductInfo::price))
            .orElseThrow(() ->
                new IllegalStateException("No available products on shop page")
            );
    }
}
