package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object pro stránku pokladny (/order/checkout).
 */
public class CheckoutPage {

    private final WebDriver driver;

    private final By firstNameField = By.id("firstName");
    private final By lastNameField  = By.id("lastName");
    private final By emailField     = By.id("email");
    private final By phoneField     = By.id("phone");
    private final By noteField      = By.id("note");
    private final By submitButton   = By.cssSelector("button[type=submit]");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
    }

    public void fillContactDetails(String firstName, String lastName,
                                   String email, String phone) {
        driver.findElement(firstNameField).clear();
        driver.findElement(firstNameField).sendKeys(firstName);
        driver.findElement(lastNameField).clear();
        driver.findElement(lastNameField).sendKeys(lastName);
        driver.findElement(emailField).clear();
        driver.findElement(emailField).sendKeys(email);
        driver.findElement(phoneField).clear();
        driver.findElement(phoneField).sendKeys(phone);
    }

    public void setNote(String note) {
        driver.findElement(noteField).clear();
        driver.findElement(noteField).sendKeys(note);
    }

    public void submit() {
        WebElement btn = driver.findElement(submitButton);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
