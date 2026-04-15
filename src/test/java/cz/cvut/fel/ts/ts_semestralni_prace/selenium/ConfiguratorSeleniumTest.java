package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ConfiguratorPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium testy pro konfigurátor zmrzliny.
 */
class ConfiguratorSeleniumTest extends SeleniumBaseTest {

    private ConfiguratorPage configuratorPage;

    @BeforeEach
    void setUpPage() {
        loginAs("jan", "jan123");
        configuratorPage = new ConfiguratorPage(driver);
        configuratorPage.open(baseUrl());
    }

    @Test
    void pageLoads_withDefaultOneScoop() {
        // Stránka inicializuje 1 kopeček přes JavaScript
        assertThat(configuratorPage.getScoopCount()).isEqualTo(1);
    }

    @Test
    void addScoop_increasesScoopCount() {
        int before = configuratorPage.getScoopCount();
        configuratorPage.addScoop();

        assertThat(configuratorPage.getScoopCount()).isEqualTo(before + 1);
    }

    @Test
    void totalPrice_recalculatesAfterSelectingCup() {
        // Výchozí stav: kornout (15 Kč) + 1 kopeček (25 Kč) = 40 Kč
        String priceBefore = configuratorPage.getTotalPriceText();

        configuratorPage.selectCup();

        // Po přepnutí na kelímek (10 Kč): 10 + 25 = 35 Kč
        String priceAfter = configuratorPage.getTotalPriceText();
        assertThat(priceAfter).isNotEqualTo(priceBefore);
    }

    @Test
    void totalPrice_recalculatesAfterAddingScoop() {
        String priceBefore = configuratorPage.getTotalPriceText();
        configuratorPage.addScoop();
        String priceAfter = configuratorPage.getTotalPriceText();

        assertThat(priceAfter).isNotEqualTo(priceBefore);
    }

    @Test
    void submitForm_addsConeWithOneScoopToCart() {
        // Formulář je ve výchozím stavu (kornout, 1 kopeček) – rovnou odeslat
        configuratorPage.submit();

        // Controller redirectuje zpět na /configurator s flash zprávou
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> configuratorPage.isSuccessDisplayed()
                        || configuratorPage.isErrorDisplayed());

        assertThat(configuratorPage.isSuccessDisplayed()).isTrue();
    }

    @Test
    void submitForm_withToppingAndCookie_addsToCart() {
        configuratorPage.selectTopping("Čokoládová poleva");
        configuratorPage.selectCookie("Oplatka");

        configuratorPage.submit();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> configuratorPage.isSuccessDisplayed()
                        || configuratorPage.isErrorDisplayed());

        assertThat(configuratorPage.isSuccessDisplayed()).isTrue();
    }

    @Test
    void maximumFiveScoops_addScoopButtonDisabled() {
        // Přidáme 4 kopečky navíc (1 je výchozí, max je 5)
        for (int i = 0; i < 4; i++) {
            configuratorPage.addScoop();
        }

        assertThat(configuratorPage.getScoopCount()).isEqualTo(5);

        // Tlačítko by mělo být disabled
        boolean disabled = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.getElementById('addScoopBtn').disabled;");
        assertThat(disabled).isTrue();
    }
}
