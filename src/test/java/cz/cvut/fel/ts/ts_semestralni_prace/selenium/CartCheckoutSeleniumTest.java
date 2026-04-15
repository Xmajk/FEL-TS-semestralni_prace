package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CartPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.CheckoutPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ConfiguratorPage;
import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.OrderConfirmationPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium testy pro košík a celý tok objednávky (E2E).
 */
class CartCheckoutSeleniumTest extends SeleniumBaseTest {

    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private OrderConfirmationPage confirmationPage;

    @BeforeEach
    void setUpPages() {
        loginAs("jan", "jan123");
        cartPage         = new CartPage(driver);
        checkoutPage     = new CheckoutPage(driver);
        confirmationPage = new OrderConfirmationPage(driver);
    }

    /** Přidá jednu zmrzlinu z konfigurátoru do košíku a přejde na stránku košíku. */
    private void addOneIceCreamToCart() {
        ConfiguratorPage configuratorPage = new ConfiguratorPage(driver);
        configuratorPage.open(baseUrl());
        // Výchozí stav: kornout + 1 kopeček – rovnou odeslat
        configuratorPage.submit();
        // Controller redirectuje zpět na /configurator (nikoli /cart) s flash zprávou
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> configuratorPage.isSuccessDisplayed()
                        || configuratorPage.isErrorDisplayed());
        // Přejít do košíku
        driver.get(baseUrl() + "/cart");
    }

    @Test
    void emptyCart_showsEmptyMessage() {
        // Nejdříve vyprázdnit košík
        cartPage.open(baseUrl());
        if (!cartPage.isEmpty()) {
            cartPage.clearCart();
        }
        cartPage.open(baseUrl());

        assertThat(cartPage.isEmpty()).isTrue();
    }

    @Test
    void addIceCream_appearsInCart() {
        // Vyčistit košík
        cartPage.open(baseUrl());
        if (!cartPage.isEmpty()) {
            cartPage.clearCart();
        }

        addOneIceCreamToCart();

        assertThat(cartPage.getCartItemCount()).isGreaterThan(0);
    }

    @Test
    void clearCart_emptiesCart() {
        addOneIceCreamToCart();

        cartPage.clearCart();
        cartPage.open(baseUrl());

        assertThat(cartPage.isEmpty()).isTrue();
    }

    @Test
    void fullCheckoutFlow_placesOrderSuccessfully() {
        // 1. Přidat zmrzlinu do košíku
        cartPage.open(baseUrl());
        if (!cartPage.isEmpty()) {
            cartPage.clearCart();
        }
        addOneIceCreamToCart();

        // 2. Přejít na pokladnu
        cartPage.proceedToCheckout();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/order/checkout"));

        // 3. Vyplnit kontaktní údaje
        checkoutPage.fillContactDetails("Jan", "Novák", "jan@example.cz", "+420777123456");
        checkoutPage.submit();

        // 4. Ověřit přesměrování na potvrzení
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> confirmationPage.isOnConfirmationPage());

        assertThat(confirmationPage.isOnConfirmationPage()).isTrue();
    }

    @Test
    void checkoutWithoutCart_redirectsToCart() {
        // Vyčistit košík
        cartPage.open(baseUrl());
        if (!cartPage.isEmpty()) {
            cartPage.clearCart();
        }

        // Pokus o přístup na pokladnu bez položek v košíku
        driver.get(baseUrl() + "/order/checkout");

        // Aplikace by měla přesměrovat zpět do košíku
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/cart")
                        || d.getCurrentUrl().contains("/checkout"));

        // Buď jsme přesměrováni do košíku, nebo zobrazena chybová zpráva
        assertThat(
                driver.getCurrentUrl().contains("/cart")
                        || driver.getPageSource().contains("prázdný")
        ).isTrue();
    }
}
