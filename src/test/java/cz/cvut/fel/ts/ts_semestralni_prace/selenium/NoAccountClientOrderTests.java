package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CartPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CheckoutPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.HomePage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.OrderConfirmationPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ShopDetailPage;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ProductService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoAccountClientOrderTests extends SeleniumBaseTest {

    private static final String SHOP_A = "shop-1";
    private static final String SHOP_B = "shop-2";
    private static final int    MIN_ORDER_TOTAL = 50;

    private static final String CUSTOMER_FIRST_NAME = "Jana";
    private static final String CUSTOMER_LAST_NAME  = "Horáková";
    private static final String CUSTOMER_EMAIL      = "jana.horakova@example.com";
    private static final String CUSTOMER_PHONE      = "+420601234567";

    @Autowired
    private ProductService productService;

    private HomePage homePage;
    private ShopDetailPage shopPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private OrderConfirmationPage confirmationPage;

    @BeforeEach
    void setUpPages() {
        homePage     = new HomePage(driver);
        shopPage     = new ShopDetailPage(driver);
        cartPage     = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        confirmationPage = new OrderConfirmationPage(driver);
    }

    @ParameterizedTest(name = "Kvíz: {0}+{1}+{2} → {3}")
    @CsvFileSource(resources = "/quiz/quiz-combinations.csv", numLinesToSkip = 1)
    @Order(1)
    @DisplayName("Kvíz — správný výsledek pro kombinaci odpovědí")
    void quiz_correctResultForCombination(
            String step1, String step2, String step3, String expectedTitle) {
        homePage.open(baseUrl());
        String actualTitle = homePage.takeQuiz(step1, step2, step3);
        assertThat(actualTitle)
                .as("Quiz result title for (%s, %s, %s)", step1, step2, step3)
                .isEqualTo(expectedTitle);
    }

    @Test
    @Order(2)
    @DisplayName("Přidání produktu do košíku jako nepřihlášený uživatel — úspěch")
    void addNewProduct_asGuest_succeeds() {
        Product product = highStockProduct(SHOP_A);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(product.getName(), 1);
        assertThat(shopPage.isSuccessDisplayed())
                .as("Success alert must appear after adding product")
                .isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("Produkty z jiné zmrzlinárny — tlačítko přidat je skryto a zobrazí se zámek")
    void addProductFromDifferentShop_asGuest_buttonIsHidden() {
        Product productA = availableProducts(SHOP_A).get(0);
        Product productB = availableProducts(SHOP_B).get(0);

        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(productA.getName(), 1);

        shopPage.open(baseUrl(), SHOP_B);
        assertThat(shopPage.hasAddToCartButton(productB.getName()))
                .as("Add-to-cart button must be hidden for products from another shop")
                .isFalse();
        assertThat(shopPage.hasCrossShopLockIcon(productB.getName()))
                .as("Lock icon must be shown for products from another shop")
                .isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Přidání více kusů produktu — košík odráží správné množství")
    void addMultipleQuantity_asGuest_cartReflectsOneItemRow() {
        Product product = highStockProduct(SHOP_A);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(product.getName(), 3);

        cartPage.open(baseUrl());
        assertThat(cartPage.getCartItemCount())
                .as("Cart must contain exactly one row for the added product")
                .isEqualTo(1);
    }

    @Test
    @Order(5)
    @DisplayName("Objednávka pod minimem 50 Kč jako nepřihlášený — chyba \"Minimální hodnota\"")
    void orderBelowMinimum_asGuest_checkoutError() {
        Product cheapest = cheapestAvailableProduct(SHOP_A);
        assertThat(cheapest.getPrice().intValue())
                .as("Precondition: cheapest product must be below the 50 Kč minimum")
                .isLessThan(MIN_ORDER_TOTAL);

        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(cheapest.getName(), 1);

        fillAndSubmitCheckout();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.urlContains("/cart"));
        assertThat(flashErrorText())
                .as("Error must indicate minimum order value")
                .contains("Minimální hodnota");
    }

    @Test
    @Order(6)
    @DisplayName("Dokončení objednávky jako nepřihlášený uživatel — potvrzení objednávky")
    void completeOrder_asGuest_confirmationShown() {
        ProductAndQty pq = productAndQtyForMinTotal(SHOP_A, MIN_ORDER_TOTAL);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(pq.product().getName(), pq.qty());

        fillAndSubmitCheckout();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.urlContains("/order/confirmation"));
        assertThat(confirmationPage.isOnConfirmationPage())
                .as("Confirmation page must be shown after successful guest order")
                .isTrue();
    }

    private void fillAndSubmitCheckout() {
        driver.get(baseUrl() + "/order/checkout");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        checkoutPage.fillContactDetails(
                CUSTOMER_FIRST_NAME, CUSTOMER_LAST_NAME, CUSTOMER_EMAIL, CUSTOMER_PHONE);
        checkoutPage.submit();
    }

    private String flashErrorText() {
        List<WebElement> errors = driver.findElements(By.cssSelector(".alert-danger"));
        return errors.isEmpty() ? "" : errors.get(0).getText();
    }

    private Product highStockProduct(String shopId) {
        return availableProducts(shopId).stream()
                .max(Comparator.comparingInt(Product::getStockQuantity))
                .orElseThrow(() -> new IllegalStateException("No products in shop " + shopId));
    }

    private Product cheapestAvailableProduct(String shopId) {
        return availableProducts(shopId).stream()
                .min(Comparator.comparing(Product::getPrice))
                .orElseThrow(() -> new IllegalStateException("No products in shop " + shopId));
    }

    private ProductAndQty productAndQtyForMinTotal(String shopId, int minTotal) {
        return availableProducts(shopId).stream()
                .map(p -> {
                    int qty = (int) Math.ceil(minTotal / p.getPrice().doubleValue());
                    return new ProductAndQty(p, qty);
                })
                .filter(pq -> pq.product().getStockQuantity() >= pq.qty())
                .min(Comparator.comparingInt(ProductAndQty::qty))
                .orElseThrow(() ->
                        new IllegalStateException("No product in " + shopId + " can reach min total " + minTotal));
    }

    private List<Product> availableProducts(String shopId) {
        return productService.getAll().stream()
                .filter(p -> shopId.equals(p.getShopId()))
                .filter(Product::isAvailable)
                .filter(p -> p.getStockQuantity() > 0)
                .toList();
    }

    private record ProductAndQty(Product product, int qty) {}
}
