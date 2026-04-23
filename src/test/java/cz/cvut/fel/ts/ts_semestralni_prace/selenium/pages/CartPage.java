package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {

    private final WebDriver driver;

    private final By cartItems      = By.cssSelector(".cart-item");
    private final By emptyCartMsg   = By.cssSelector(".display-1");
    private final By checkoutLink   = By.cssSelector("a[href*='/order/checkout']");
    private final By clearCartBtn   = By.cssSelector("form[action*='/cart/clear'] button");
    private final By totalPriceEl   = By.cssSelector(".col-lg-4 .text-ice");
    private final By successAlert   = By.cssSelector(".alert-success");
    private final By errorAlert     = By.cssSelector(".alert-danger");

    public CartPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/cart");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.urlContains("/cart"));
    }

    public int getCartItemCount() {
        return driver.findElements(cartItems).size();
    }

    public boolean isEmpty() {
        return !driver.findElements(emptyCartMsg).isEmpty()
                && driver.findElements(cartItems).isEmpty();
    }

    public void proceedToCheckout() {
        driver.findElement(checkoutLink).click();
    }

    public void clearCart() {
        driver.findElement(clearCartBtn).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> d.findElements(cartItems).isEmpty());
    }

    public String getTotalPriceText() {
        return driver.findElement(totalPriceEl).getText();
    }

    public boolean isSuccessDisplayed() {
        return !driver.findElements(successAlert).isEmpty();
    }

    public boolean isErrorDisplayed() {
        return !driver.findElements(errorAlert).isEmpty();
    }
}
