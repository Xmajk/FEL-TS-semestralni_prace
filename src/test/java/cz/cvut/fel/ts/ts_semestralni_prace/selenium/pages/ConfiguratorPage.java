package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object pro konfigurátor zmrzliny (/configurator).
 */
public class ConfiguratorPage {

    private final WebDriver driver;

    private final By addScoopBtn     = By.id("addScoopBtn");
    private final By scoopSelects    = By.cssSelector("#scoopFlavors select[name=flavors]");
    private final By containerCone   = By.id("containerCone");
    private final By containerCup    = By.id("containerCup");
    private final By totalPriceEl    = By.id("totalPrice");
    private final By submitButton    = By.cssSelector("#configuratorForm button[type=submit]");
    private final By successAlert    = By.cssSelector(".alert-success");
    private final By errorAlert      = By.cssSelector(".alert-danger");

    public ConfiguratorPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/configurator");
        // Počkat, dokud formulář není přítomen v DOM (stránka se plně načetla)
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("configurator"));
    }

    /** Klikne na tlačítko „Přidat kopeček" (čeká se JavaScript inicializace 1. kopečku). */
    public void addScoop() {
        driver.findElement(addScoopBtn).click();
    }

    /** Přidá kopeček a počká, dokud se v DOM neobjeví nový &lt;select&gt;. */
    public void addScoopAndWait() {
        int before = getScoopCount();
        clickViaJs(driver.findElement(addScoopBtn));
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> getScoopCount() == before + 1);
    }

    /** Nastaví příchuť kopečku (0-based index) na zadanou hodnotu. */
    public void setScoopFlavor(int scoopIndex, String flavor) {
        List<WebElement> selects = driver.findElements(scoopSelects);
        new Select(selects.get(scoopIndex)).selectByValue(flavor);
    }

    public void selectCone() {
        clickViaJs(driver.findElement(containerCone));
    }

    public void selectCup() {
        clickViaJs(driver.findElement(containerCup));
    }

    /** Zaškrtne posypku podle hodnoty atributu value. */
    public void selectTopping(String topping) {
        String id = "topping-" + topping.replace(" ", "-");
        clickViaJs(driver.findElement(By.id(id)));
    }

    /** Zaškrtne sušenku podle hodnoty atributu value. */
    public void selectCookie(String cookie) {
        String id = "cookie-" + cookie.replace(" ", "-");
        clickViaJs(driver.findElement(By.id(id)));
    }

    public String getTotalPriceText() {
        return driver.findElement(totalPriceEl).getText();
    }

    public void submit() {
        // Explicitní wait – zaručí, že tlačítko je v DOM a klikatelné
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isSuccessDisplayed() {
        return !driver.findElements(successAlert).isEmpty();
    }

    public boolean isErrorDisplayed() {
        return !driver.findElements(errorAlert).isEmpty();
    }

    public int getScoopCount() {
        return driver.findElements(scoopSelects).size();
    }

    /** Klikne na radio / checkbox přes JavaScript – obchází Bootstrap btn-check, které má display:none. */
    private void clickViaJs(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
