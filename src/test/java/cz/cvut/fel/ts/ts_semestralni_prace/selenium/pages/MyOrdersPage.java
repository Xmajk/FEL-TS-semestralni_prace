package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MyOrdersPage {

    private final WebDriver driver;

    private final By orderCards   = By.cssSelector(".card.shadow-sm.border-0.rounded-4");
    private final By cancelBtn    = By.cssSelector("button.btn-outline-danger");
    private final By successAlert = By.cssSelector(".alert-success");
    private final By errorAlert   = By.cssSelector(".alert-danger");

    public MyOrdersPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/order/moje");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/order/moje"));
    }

    public int getOrderCount() {
        return driver.findElements(orderCards).size();
    }

    public boolean hasOrders() {
        return !driver.findElements(orderCards).isEmpty();
    }

    public boolean hasCancelButton() {
        return !driver.findElements(cancelBtn).isEmpty();
    }

    public void cancelFirstOrder() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.confirm = () => true;");
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(cancelBtn));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try {
            btn.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", btn);
        }
    }

    public boolean isSuccessDisplayed() {
        return !driver.findElements(successAlert).isEmpty();
    }

    public boolean isErrorDisplayed() {
        return !driver.findElements(errorAlert).isEmpty();
    }

    public String getSuccessText() {
        List<WebElement> alerts = driver.findElements(successAlert);
        return alerts.isEmpty() ? "" : alerts.get(0).getText();
    }

    public String getErrorText() {
        List<WebElement> alerts = driver.findElements(errorAlert);
        return alerts.isEmpty() ? "" : alerts.get(0).getText();
    }
}
