package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ShopDetailPage {

    public record ProductInfo(String id, String name, double price, int stock) {}

    private final WebDriver driver;

    private final By productCards = By.cssSelector(".product-card");
    private final By successAlert = By.cssSelector(".alert-success");
    private final By errorAlert   = By.cssSelector(".alert-danger");

    public ShopDetailPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl, String shopId) {
        driver.get(baseUrl + "/shop/" + shopId);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/shop/" + shopId));
        waitForProductsRendered();
    }

    private void waitForProductsRendered() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                !d.findElements(productCards).isEmpty()
                        || !d.findElements(By.cssSelector(".text-center.py-5")).isEmpty());
    }

    public void addProductByName(String productName, int quantity) {
        WebElement card = findCardByName(productName);
        WebElement form = card.findElement(By.cssSelector("form[action*='/cart/add/']"));
        WebElement qtyInput = form.findElement(By.name("quantity"));
        qtyInput.clear();
        qtyInput.sendKeys(Integer.toString(quantity));
        WebElement submit = form.findElement(By.cssSelector("button[type=submit]"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", submit);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !d.findElements(successAlert).isEmpty()
                        || !d.findElements(errorAlert).isEmpty());
    }

    public boolean isSuccessDisplayed() {
        return !driver.findElements(successAlert).isEmpty();
    }

    public boolean isErrorDisplayed() {
        return !driver.findElements(errorAlert).isEmpty();
    }

    public String getErrorText() {
        return driver.findElements(errorAlert).isEmpty()
                ? ""
                : driver.findElement(errorAlert).getText();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean hasAddToCartButton(String productName) {
        WebElement card = findCardByName(productName);
        return !card.findElements(By.cssSelector("form[action*='/cart/add/']")).isEmpty();
    }

    public boolean hasCrossShopLockIcon(String productName) {
        WebElement card = findCardByName(productName);
        return !card.findElements(By.cssSelector("i.bi-lock")).isEmpty();
    }

    public List<ProductInfo> getAvailableProducts() {
        waitForProductsRendered();
        List<ProductInfo> result = new ArrayList<>();
        for (WebElement card : driver.findElements(productCards)) {
            List<WebElement> forms = card.findElements(By.cssSelector("form[action*='/cart/add/']"));
            if (forms.isEmpty()) continue;
            WebElement form = forms.get(0);
            String action = form.getAttribute("action");
            String id = action.substring(action.lastIndexOf('/') + 1);
            String name = card.findElement(By.cssSelector(".card-title")).getText().trim();
            double price = parsePrice(card.findElement(By.cssSelector(".text-ice.fs-5")).getText());
            int stock = Integer.parseInt(form.findElement(By.name("quantity")).getAttribute("max"));
            result.add(new ProductInfo(id, name, price, stock));
        }
        return result;
    }

    private static double parsePrice(String text) {
        String digits = text.replace("Kč", "").replace(" ", "").replace(" ", "").replace(",", ".").trim();
        return Double.parseDouble(digits);
    }

    private WebElement findCardByName(String productName) {
        List<WebElement> cards = driver.findElements(productCards);
        for (WebElement card : cards) {
            WebElement title = card.findElement(By.cssSelector(".card-title"));
            if (productName.equals(title.getText().trim())) {
                return card;
            }
        }
        throw new NoSuchElementException("Product not found on page: " + productName);
    }
}
