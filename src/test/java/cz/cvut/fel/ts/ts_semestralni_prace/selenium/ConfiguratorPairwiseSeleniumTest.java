package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.ConfiguratorPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguratorPairwiseSeleniumTest extends SeleniumBaseTest {

    private static final List<String> TOPPINGS = List.of(
            "Čokoládový posyp", "Barevný posyp", "Kokosové vločky",
            "Ořechový posyp", "Karamelová poleva", "Čokoládová poleva"
    );

    private static final List<String> COOKIES = List.of(
            "Oplatka", "Čokoládová tyčinka", "Trubička", "Sušenka", "Lžička z čokolády"
    );

    private ConfiguratorPage configuratorPage;

    @BeforeEach
    void setUpPage() {
        this.loginAsAdmin();
        configuratorPage = new ConfiguratorPage(driver);
        configuratorPage.open(baseUrl());
    }

    static Stream<Arguments> pairwiseCases() {
        return Stream.of(
                Arguments.of("PW1", "kornout", 1, 0, 0, "40,00 Kč"),
                Arguments.of("PW2", "kornout", 3, 3, 2, "150,00 Kč"),
                Arguments.of("PW3", "kornout", 5, 6, 5, "275,00 Kč"),
                Arguments.of("PW4", "kelimek", 1, 3, 5, "140,00 Kč"),
                Arguments.of("PW5", "kelimek", 3, 6, 0, "145,00 Kč"),
                Arguments.of("PW6", "kelimek", 5, 0, 2, "165,00 Kč"),
                Arguments.of("PW7", "kornout", 1, 6, 2, "130,00 Kč"),
                Arguments.of("PW8", "kelimek", 3, 0, 5, "160,00 Kč"),
                Arguments.of("PW9", "kornout", 5, 3, 0, "170,00 Kč")
        );
    }

    @ParameterizedTest(name = "{0}: {1}, {2} kopečků, {3} posypek, {4} sušenek → {5}")
    @MethodSource("pairwiseCases")
    void pairwise_configuratorSubmitsAndPriceMatches(
            String id, String container, int scoops, int toppingCount, int cookieCount, String expectedPrice) {

        // 1. Výběr obalu
        if ("kelimek".equals(container)) {
            configuratorPage.selectCup();
        } else {
            configuratorPage.selectCone();
        }

        // 2. Přidání kopečků (1 je výchozí, přidáme zbytek)
        for (int i = 1; i < scoops; i++) {
            configuratorPage.addScoopAndWait();
        }

        // 3. Výběr posypek (prvních N z dostupných)
        for (int i = 0; i < toppingCount; i++) {
            configuratorPage.selectTopping(TOPPINGS.get(i));
        }

        // 4. Výběr sušenek (prvních N z dostupných)
        for (int i = 0; i < cookieCount; i++) {
            configuratorPage.selectCookie(COOKIES.get(i));
        }

        // 5. Ověření ceny zobrazené na UI před odesláním
        assertThat(configuratorPage.getTotalPriceText())
                .as("%s — cena zobrazená v UI před odesláním", id)
                .isEqualTo(expectedPrice);

        // 6. Odeslání formuláře
        configuratorPage.submit();

        // 7. Ověření úspěšného přidání do košíku
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> configuratorPage.isSuccessDisplayed()
                        || configuratorPage.isErrorDisplayed());

        assertThat(configuratorPage.isSuccessDisplayed())
                .as("%s — formulář by měl být úspěšně odeslán", id)
                .isTrue();
    }
}
