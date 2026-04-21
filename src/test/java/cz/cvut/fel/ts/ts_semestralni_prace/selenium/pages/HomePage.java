package cz.cvut.fel.ts.ts_semestralni_prace.selenium.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage {

    private final WebDriver driver;

    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("step-1")));
    }

    public String takeQuiz(String step1Value, String step2Value, String step3Value) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        jsClick(wait, js, 1, step1Value);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("step-2")));
        jsClick(wait, js, 2, step2Value);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("step-3")));
        jsClick(wait, js, 3, step3Value);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("step-result")));
        return driver.findElement(By.id("quiz-result-title")).getText();
    }

    private void jsClick(WebDriverWait wait, JavascriptExecutor js, int step, String value) {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-step='" + step + "'][data-value='" + value + "']")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        js.executeScript("arguments[0].click();", btn);
    }
}
