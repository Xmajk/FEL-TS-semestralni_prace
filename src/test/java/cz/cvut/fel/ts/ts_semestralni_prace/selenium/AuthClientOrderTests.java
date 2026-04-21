package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CartPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CheckoutPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.MyOrdersPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.OrderConfirmationPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ShopDetailPage;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ProductService;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthClientOrderTests extends SeleniumBaseTest {

    private static final String SHOP_A = "shop-1";
    private static final int MIN_ORDER_TOTAL = 50;

    private static final String JAN_FIRST_NAME = "Jan";
    private static final String JAN_LAST_NAME = "Novák";
    private static final String JAN_EMAIL = "jan@example.cz";
    private static final String JAN_PHONE = "+420777000000";

    @Autowired
    private ProductService productService;

    private ShopDetailPage shopPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private OrderConfirmationPage confirmationPage;
    private MyOrdersPage myOrdersPage;

    @BeforeEach
    void setUpPages() {
        loginAsUser();
        shopPage = new ShopDetailPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        confirmationPage = new OrderConfirmationPage(driver);
        myOrdersPage = new MyOrdersPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName(
        "Objednání produktů jako přihlášený uživatel — zobrazení potvrzení objednávky"
    )
    void placeOrder_asLoggedInUser_confirmationShown() {
        addSufficientProductToCart();
        fillAndSubmitCheckout();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.urlContains("/order/confirmation")
        );
        assertThat(confirmationPage.isOnConfirmationPage())
            .as("Confirmation page must be shown after placing order")
            .isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Předvyplnění kontaktních údajů z účtu na pokladně")
    void checkout_prefillFromAccount_fieldsPopulatedFromUserAccount() {
        addSufficientProductToCart();
        driver.get(baseUrl() + "/order/checkout");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );

        driver.findElement(By.cssSelector("button.btn-outline-ice")).click();

        assertThat(driver.findElement(By.id("firstName")).getAttribute("value"))
            .as("firstName must be pre-filled from user account")
            .isEqualTo(JAN_FIRST_NAME);
        assertThat(driver.findElement(By.id("lastName")).getAttribute("value"))
            .as("lastName must be pre-filled from user account")
            .isEqualTo(JAN_LAST_NAME);
        assertThat(driver.findElement(By.id("email")).getAttribute("value"))
            .as("email must be pre-filled from user account")
            .isEqualTo(JAN_EMAIL);
    }

    @Test
    @Order(3)
    @DisplayName("Zobrazení provedených objednávek na stránce Moje objednávky")
    void placeOrder_thenViewInMyOrders_orderIsVisible() {
        addSufficientProductToCart();
        fillAndSubmitCheckout();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.urlContains("/order/confirmation")
        );

        myOrdersPage.open(baseUrl());

        assertThat(myOrdersPage.hasOrders())
            .as("My-orders page must list the placed order")
            .isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Zrušení objednávky ve stavu Nová — úspěšné zrušení")
    void placeOrder_thenCancelInMyOrders_successMessageShown() {
        addSufficientProductToCart();
        fillAndSubmitCheckout();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.urlContains("/order/confirmation")
        );

        myOrdersPage.open(baseUrl());
        assertThat(myOrdersPage.hasCancelButton())
            .as("Newly placed order must show cancel button")
            .isTrue();

        myOrdersPage.cancelFirstOrder();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.urlContains("/order/moje")
        );
        assertThat(myOrdersPage.isSuccessDisplayed())
            .as("Success alert must appear after cancellation")
            .isTrue();
        assertThat(myOrdersPage.getSuccessText())
            .as("Success message must confirm cancellation")
            .contains("zrušena");
    }

    private void addSufficientProductToCart() {
        ProductAndQty pq = productAndQtyForMinTotal(SHOP_A, MIN_ORDER_TOTAL);
        shopPage.open(baseUrl(), SHOP_A);
        shopPage.addProductByName(pq.product().getName(), pq.qty());
    }

    private void fillAndSubmitCheckout() {
        driver.get(baseUrl() + "/order/checkout");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        checkoutPage.fillContactDetails(
            JAN_FIRST_NAME,
            JAN_LAST_NAME,
            JAN_EMAIL,
            JAN_PHONE
        );
        checkoutPage.submit();
    }

    private ProductAndQty productAndQtyForMinTotal(
        String shopId,
        int minTotal
    ) {
        return availableProducts(shopId)
            .stream()
            .map(p -> {
                int qty = (int) Math.ceil(
                    minTotal / p.getPrice().doubleValue()
                );
                return new ProductAndQty(p, qty);
            })
            .filter(pq -> pq.product().getStockQuantity() >= pq.qty())
            .min(Comparator.comparingInt(ProductAndQty::qty))
            .orElseThrow(() ->
                new IllegalStateException(
                    "No product in " +
                        shopId +
                        " can reach min total " +
                        minTotal
                )
            );
    }

    private List<Product> availableProducts(String shopId) {
        return productService
            .getAll()
            .stream()
            .filter(p -> shopId.equals(p.getShopId()))
            .filter(Product::isAvailable)
            .filter(p -> p.getStockQuantity() > 0)
            .toList();
    }

    private record ProductAndQty(Product product, int qty) {}
}
