package cz.cvut.fel.ts.ts_semestralni_prace.config;

import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Shop;
import cz.cvut.fel.ts.ts_semestralni_prace.model.User;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ProductService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {

    private final ShopService shopService;
    private final ProductService productService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ShopService shopService, ProductService productService,
                            UserService userService, PasswordEncoder passwordEncoder) {
        this.shopService = shopService;
        this.productService = productService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        initUsers();
        initShops();
        initProducts();
    }

    private void initUsers() {
        if (!userService.existsByUsername("admin")) {
            userService.save(User.builder()
                    .id(UUID.randomUUID().toString())
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@zmrzlina.cz")
                    .firstName("Admin")
                    .lastName("Systém")
                    .role("ROLE_ADMIN")
                    .build());
        }
        if (!userService.existsByUsername("jan")) {
            userService.save(User.builder()
                    .id(UUID.randomUUID().toString())
                    .username("jan")
                    .password(passwordEncoder.encode("jan123"))
                    .email("jan@example.cz")
                    .firstName("Jan")
                    .lastName("Novák")
                    .role("ROLE_USER")
                    .build());
        }
    }

    private void initShops() {
        if (!shopService.getAll().isEmpty()) return;

        shopService.save(Shop.builder()
                .id("shop-1")
                .name("Zmrzlinárna Centrum")
                .address("Václavské náměstí 1")
                .city("Praha")
                .description("Tradiční zmrzlinárna v samém srdci Prahy s výběrem přes 20 příchutí.")
                .imageEmoji("🍦")
                .active(true)
                .build());

        shopService.save(Shop.builder()
                .id("shop-2")
                .name("Gelato Brno")
                .address("Náměstí Svobody 5")
                .city("Brno")
                .description("Autentická italská gelaterija s čerstvými surovinami každý den.")
                .imageEmoji("🍨")
                .active(true)
                .build());

        shopService.save(Shop.builder()
                .id("shop-3")
                .name("Frutteria Ostrava")
                .address("Masarykovo náměstí 10")
                .city("Ostrava")
                .description("Ovocné sorbety a osvěžující zmrzlinové poháry pro celou rodinu.")
                .imageEmoji("🍧")
                .active(true)
                .build());

        shopService.save(Shop.builder()
                .id("shop-4")
                .name("Zmrzlinárna Plzeň")
                .address("náměstí Republiky 3")
                .city("Plzeň")
                .description("Domácí receptury a sezónní speciality z lokálních ingrediencí.")
                .imageEmoji("🧁")
                .active(true)
                .build());
    }

    private void initProducts() {
        if (!productService.getAll().isEmpty()) return;

        // Praha - shop-1
        addProduct("shop-1", "Vanilková klasika", "Krémová vanilková zmrzlina z pravých vanilkových lusků.", "Vanilka", "38.00", 80);
        addProduct("shop-1", "Čokoládová extáze", "Intenzivní čokoládová zmrzlina z belgické čokolády.", "Čokoláda", "42.00", 60);
        addProduct("shop-1", "Jahodový sen", "Sladká zmrzlina z čerstvých jahod.", "Jahoda", "40.00", 55);
        addProduct("shop-1", "Pistáciový poklad", "Prémiová pistáciová zmrzlina z íránských pistácií.", "Pistácie", "48.00", 30);
        addProduct("shop-1", "Citronový sorbet", "Osvěžující citronový sorbet bez mléka.", "Citron", "35.00", 70);

        // Brno - shop-2
        addProduct("shop-2", "Stracciatella", "Smetanová zmrzlina s kousky čokolády.", "Čokoláda", "40.00", 50);
        addProduct("shop-2", "Tiramisu", "Zmrzlina s chutí italského tiramisu.", "Káva", "45.00", 40);
        addProduct("shop-2", "Malinový sorbet", "Svěží sorbet z čerstvých malin.", "Malina", "38.00", 65);
        addProduct("shop-2", "Kokos & Mango", "Exotická zmrzlina s kokosovým mlékem a mangem.", "Exotické ovoce", "44.00", 35);
        addProduct("shop-2", "Ořechová pralinka", "Zmrzlina s pralinkami z lískových oříšků.", "Oříšky", "46.00", 25);

        // Ostrava - shop-3
        addProduct("shop-3", "Borůvkový sorbet", "Intensivní borůvkový sorbet.", "Borůvka", "37.00", 55);
        addProduct("shop-3", "Meruňkový sen", "Sladký sorbet z moravských meruněk.", "Meruňka", "36.00", 60);
        addProduct("shop-3", "Melounová svěžest", "Lehký sorbet z červeného melounu.", "Meloun", "34.00", 70);
        addProduct("shop-3", "Mátová zmrzlina", "Osvěžující mátová zmrzlina se čokoládovými kapičkami.", "Máta", "39.00", 45);
        addProduct("shop-3", "Karamelová toffee", "Zmrzlina s karamelovou sauceou a křupavými kousky.", "Karamel", "43.00", 30);

        // Plzeň - shop-4
        addProduct("shop-4", "Smetanová klasika", "Jednoduchá smetanová zmrzlina z čerstvé smetany.", "Smetana", "36.00", 80);
        addProduct("shop-4", "Bezinkový sorbet", "Regionální sorbet z bezinkového sirupu.", "Bezinka", "38.00", 40);
        addProduct("shop-4", "Hruškový likér", "Zmrzlina inspirovaná plzeňskými hruškami.", "Hruška", "40.00", 35);
        addProduct("shop-4", "Nugátová bomba", "Bohatá nugátová zmrzlina pro milovníky sladkého.", "Nugát", "44.00", 25);
        addProduct("shop-4", "Skořicová hřejivost", "Zmrzlina se skořicí a karamelizovanými jablky.", "Skořice", "42.00", 30);
    }

    private void addProduct(String shopId, String name, String description,
                             String flavor, String price, int stock) {
        productService.save(Product.builder()
                .id(UUID.randomUUID().toString())
                .shopId(shopId)
                .name(name)
                .description(description)
                .flavor(flavor)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .available(true)
                .build());
    }
}
