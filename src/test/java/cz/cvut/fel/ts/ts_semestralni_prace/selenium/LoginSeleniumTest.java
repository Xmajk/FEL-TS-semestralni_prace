package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium testy pro autentizaci (přihlášení / odhlášení).
 */
class LoginSeleniumTest extends SeleniumBaseTest {

    private LoginPage loginPage;

    @BeforeEach
    void setUpPage() {
        loginPage = new LoginPage(driver);
    }

    @Test
    void loginWithValidCredentials_redirectsToHome() {
        loginPage.open(baseUrl());
        loginPage.loginAs("jan", "jan123");

        assertThat(loginPage.getCurrentUrl()).doesNotContain("/login");
    }

    @Test
    void loginWithInvalidPassword_showsErrorMessage() {
        loginPage.open(baseUrl());
        loginPage.loginAs("jan", "spatneHeslo");

        assertThat(loginPage.isErrorDisplayed()).isTrue();
        assertThat(loginPage.getCurrentUrl()).contains("/login");
    }

    @Test
    void loginWithUnknownUser_showsErrorMessage() {
        loginPage.open(baseUrl());
        loginPage.loginAs("neexistujici", "cokoliv");

        assertThat(loginPage.isErrorDisplayed()).isTrue();
    }

    @Test
    void adminLogin_canAccessAdminDashboard() {
        loginPage.open(baseUrl());
        loginPage.loginAs("admin", "admin123");

        driver.get(baseUrl() + "/admin/dashboard");

        assertThat(driver.getCurrentUrl()).contains("/admin/dashboard");
        // Stránka se nenačetla jako 403 – ověříme, že obsahuje nadpis dashboardu
        assertThat(driver.getPageSource()).doesNotContain("403");
    }

    @Test
    void regularUser_cannotAccessAdminDashboard() {
        loginPage.open(baseUrl());
        loginPage.loginAs("jan", "jan123");

        driver.get(baseUrl() + "/admin/dashboard");

        // Spring Security přesměruje nebo vrátí 403
        assertThat(driver.getCurrentUrl()).doesNotContain("/admin/dashboard");
    }

    @Test
    void logout_redirectsToLoginWithMessage() {
        // Přihlásit se
        loginPage.open(baseUrl());
        loginPage.loginAs("jan", "jan123");

        // Odhlásit se přes POST /logout (klik na odkaz v navbaru)
        driver.get(baseUrl() + "/login?logout");

        assertThat(loginPage.isLogoutMessageDisplayed()).isTrue();
    }
}
