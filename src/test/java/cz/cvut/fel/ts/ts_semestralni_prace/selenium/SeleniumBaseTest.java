package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.LoginPage;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

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
        options.addArguments(
            "--disable-features=PasswordLeakDetection,AutofillServerCommunication,PasswordManagerOnboarding,PasswordCheck"
        );
        options.addArguments("--disable-save-password-bubble");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        options.setExperimentalOption(
            "excludeSwitches",
            new String[] { "enable-automation" }
        );
        driver = new ChromeDriver(options);

        this.loginPage = new LoginPage(driver);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void loginAs(String username, String password) {
        this.loginPage.open(this.baseUrl());
        this.loginPage.loginAs(username, password);
        //try {
        //    this.driver.wait(1_000);
        //}catch (Exception e){}
    }

    protected void loginAsAdmin() {
        this.loginAs("admin", "admin123");
    }

    protected void loginAsUser() {
        this.loginAs("jan", "jan123");
    }
}
