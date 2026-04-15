package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object pro stránku potvrzení objednávky (/order/confirmation).
 */
public class OrderConfirmationPage {

    private final WebDriver driver;

    private final By orderNumber = By.cssSelector(".text-ice, .badge");
    private final By heading     = By.tagName("h1");

    public OrderConfirmationPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isOnConfirmationPage() {
        return driver.getCurrentUrl().contains("/order/confirmation")
                || driver.getCurrentUrl().contains("/order/");
    }

    public String getHeadingText() {
        return driver.findElement(heading).getText();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
