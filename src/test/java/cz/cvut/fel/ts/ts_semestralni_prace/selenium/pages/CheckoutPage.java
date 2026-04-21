package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CheckoutPage {

    private final WebDriver driver;

    private final By firstNameField = By.id("firstName");
    private final By lastNameField  = By.id("lastName");
    private final By emailField     = By.id("email");
    private final By phoneField     = By.id("phone");
    private final By noteField      = By.id("note");
    private final By submitButton   = By.cssSelector("form[action*='/order/checkout'] button[type=submit]");

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
        driver.findElement(phoneField).sendKeys(Keys.ESCAPE);
    }

    public void setNote(String note) {
        driver.findElement(noteField).clear();
        driver.findElement(noteField).sendKeys(note);
    }

    public void submit() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(submitButton));
        driver.findElement(submitButton).submit();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
