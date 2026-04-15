package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Základní třída pro Selenium testy.
 * Spouští celou Spring Boot aplikaci na náhodném portu a řídí životní cyklus WebDriveru.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SeleniumBaseTest {

    @LocalServerPort
    private int port;

    protected WebDriver driver;

    protected LoginPage loginPage;

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,900");
        driver = new ChromeDriver(options);
        // Implicit wait záměrně nenastavujeme – míchání implicit a explicit waitů
        // způsobuje nepředvídatelné blokování. Každá Page Object metoda používá
        // explicitní WebDriverWait tam, kde je potřeba.

        this.loginPage = new LoginPage(driver);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Přihlásí uživatele přes přihlašovací formulář. */
    protected void loginAs(String username, String password) {
        this.loginPage.open(this.baseUrl());
        this.loginPage.loginAs(username,password);
        //try {
        //    this.driver.wait(1_000);
        //}catch (Exception e){}
    }

    protected void loginAsAdmin(){
        this.loginAs("admin","admin123");
    }

    protected void loginAsUser(){
        this.loginAs("jan", "jan123");
    }
}
