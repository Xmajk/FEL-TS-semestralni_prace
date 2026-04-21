package cz.cvut.fel.ts.ts_semestralni_prace.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import cz.cvut.fel.ts.ts_semestralni_prace.model.CartItem;
import cz.cvut.fel.ts.ts_semestralni_prace.model.CustomerDetails;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Order;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Shop;
import cz.cvut.fel.ts.ts_semestralni_prace.service.OrderService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ProductService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminPageTests extends SeleniumBaseTest {

    private static final String SHOP_A = "shop-1";

    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private ShopService shopService;

    @BeforeEach
    void setUp() {
        loginAsAdmin();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Změna stavu objednávky z Nová na Potvrzená — úspěšné uložení")
    void updateOrderStatus_fromNewToConfirmed_successFlashShown() {
        String orderId = createTestOrder();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl() + "/admin/orders");

        WebElement orderForm = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("form[action*='/admin/orders/status/" + orderId + "']")));

        new Select(orderForm.findElement(By.name("status"))).selectByValue("CONFIRMED");
        ((JavascriptExecutor) driver).executeScript("arguments[0].requestSubmit();", orderForm);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertThat(flashSuccessText())
                .as("Success flash must confirm status was updated")
                .contains("Status objednávky byl aktualizován.");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Vytvoření nové zmrzlinárny — úspěšné uložení")
    void createShop_asAdmin_successFlashShown() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl() + "/admin/shops/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));

        driver.findElement(By.id("name")).sendKeys("Testovací Zmrzlinárna");
        driver.findElement(By.id("address")).sendKeys("Testovací ulice 1");
        driver.findElement(By.id("city")).sendKeys("Praha");
        driver.findElement(By.cssSelector("form[action*='/admin/shops/save'] button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertThat(flashSuccessText())
                .as("Success flash must confirm shop was saved")
                .contains("Zmrzlinárna byla uložena.");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Přidání nového produktu — úspěšné uložení")
    void addProduct_asAdmin_successFlashShown() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl() + "/admin/products/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));

        new Select(driver.findElement(By.id("shopId"))).selectByValue(SHOP_A);
        driver.findElement(By.id("name")).sendKeys("Testovací Kopečík");
        driver.findElement(By.id("flavor")).sendKeys("Jahoda");
        driver.findElement(By.id("price")).sendKeys("35");
        driver.findElement(By.id("stockQuantity")).sendKeys("20");
        driver.findElement(By.cssSelector("form[action*='/admin/products/save'] button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertThat(flashSuccessText())
                .as("Success flash must confirm product was saved")
                .contains("Produkt byl uložen.");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("Naskladnění produktu — úspěšná aktualizace počtu na skladě")
    void restockProduct_asAdmin_stockUpdated() {
        Product product = productService.getAll().stream()
                .filter(p -> SHOP_A.equals(p.getShopId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No products in " + SHOP_A));
        int newStock = product.getStockQuantity() + 50;

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl() + "/admin/products/edit/" + product.getId());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stockQuantity")));

        WebElement stockField = driver.findElement(By.id("stockQuantity"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", stockField);
        stockField.sendKeys(String.valueOf(newStock));
        driver.findElement(By.cssSelector("form[action*='/admin/products/save'] button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertThat(flashSuccessText())
                .as("Success flash must confirm product was saved")
                .contains("Produkt byl uložen.");

        int updatedStock = productService.findById(product.getId())
                .map(Product::getStockQuantity)
                .orElseThrow();
        assertThat(updatedStock)
                .as("Stock quantity must be updated to " + newStock)
                .isEqualTo(newStock);
    }

    private String createTestOrder() {
        Product p = productService.getAll().stream()
                .filter(pr -> SHOP_A.equals(pr.getShopId()) && pr.isAvailable() && pr.getStockQuantity() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available product in " + SHOP_A));
        int qty = (int) Math.ceil(50.0 / p.getPrice().doubleValue());
        Shop shop = shopService.findById(SHOP_A).orElseThrow();
        CartItem item = CartItem.builder()
                .productId(p.getId())
                .productName(p.getName())
                .shopId(SHOP_A)
                .shopName(shop.getName())
                .quantity(qty)
                .price(p.getPrice())
                .build();
        CustomerDetails cd = new CustomerDetails(
                "Test", "Admin", "admin@test.cz", "+420000000000", null, null, null, null);
        Order order = orderService.createOrder(List.of(item), cd, SHOP_A, shop.getName(), "admin");
        return order.getId();
    }

    private String flashSuccessText() {
        List<WebElement> alerts = driver.findElements(By.cssSelector(".alert-success"));
        return alerts.isEmpty() ? "" : alerts.get(0).getText();
    }
}
