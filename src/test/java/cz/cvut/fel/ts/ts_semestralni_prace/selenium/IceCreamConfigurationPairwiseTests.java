package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ConfiguratorPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IceCreamConfigurationPairwiseTests extends SeleniumBaseTest {

    private static final List<String> TOPPINGS = List.of(
            "Čokoládový posyp", "Barevný posyp", "Kokosové vločky",
            "Ořechový posyp", "Karamelová poleva", "Čokoládová poleva"
    );

    private static final List<String> COOKIES = List.of(
            "Oplatka", "Čokoládová tyčinka", "Trubička", "Sušenka", "Lžička z čokolády"
    );

    private static final String CONTAINER_CONE = "kornout";
    private static final String CONTAINER_CUP  = "kelimek";

    private ConfiguratorPage configuratorPage;

    @BeforeEach
    void setUpPage() {
        loginAsAdmin();
        configuratorPage = new ConfiguratorPage(driver);
        configuratorPage.open(baseUrl());
    }

    @ParameterizedTest(name = "{0}: {1}, {2} scoops, {3} toppings, {4} cookies -> {5} CZK")
    @CsvFileSource(
            resources = "/pairwise/ice-cream-pairwise.csv",
            numLinesToSkip = 1
    )
    void pairwise_configuratorBuildsExpectedPrice(
            String id,
            String container,
            int scoops,
            int toppingCount,
            int cookieCount,
            int expectedPrice) {

        configureIceCream(container, scoops, toppingCount, cookieCount);

        String expectedPriceText = formatPrice(expectedPrice);

        assertThat(configuratorPage.getTotalPriceText())
                .as("%s — UI price before submit", id)
                .isEqualTo(expectedPriceText);

        configuratorPage.submit();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> configuratorPage.isSuccessDisplayed()
                        || configuratorPage.isErrorDisplayed());

        assertThat(configuratorPage.isSuccessDisplayed())
                .as("%s — server returned a successful redirect (HTTP 3xx, success flash)", id)
                .isTrue();
        assertThat(configuratorPage.isErrorDisplayed())
                .as("%s — server must not return an error flash", id)
                .isFalse();
    }

    private void configureIceCream(String container, int scoops, int toppingCount, int cookieCount) {
        selectContainer(container);
        selectScoops(scoops);
        selectFirstN(toppingCount, TOPPINGS, configuratorPage::selectTopping);
        selectFirstN(cookieCount, COOKIES, configuratorPage::selectCookie);
    }

    private void selectContainer(String container) {
        switch (container) {
            case CONTAINER_CONE -> configuratorPage.selectCone();
            case CONTAINER_CUP  -> configuratorPage.selectCup();
            default -> throw new IllegalArgumentException("Unknown container: " + container);
        }
    }

    private void selectScoops(int scoops) {
        if (scoops < 1) {
            throw new IllegalArgumentException("At least 1 scoop required, got: " + scoops);
        }
        for (int i = 1; i < scoops; i++) {
            configuratorPage.addScoopAndWait();
        }
    }

    private void selectFirstN(int count, List<String> source, java.util.function.Consumer<String> action) {
        if (count > source.size()) {
            throw new IllegalArgumentException(
                    "Requested " + count + " items, only " + source.size() + " available");
        }
        for (int i = 0; i < count; i++) {
            action.accept(source.get(i));
        }
    }

    private static String formatPrice(int priceCzk) {
        return String.format("%d,00 Kč", priceCzk);
    }
}
