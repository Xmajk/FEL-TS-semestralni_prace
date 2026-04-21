package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
