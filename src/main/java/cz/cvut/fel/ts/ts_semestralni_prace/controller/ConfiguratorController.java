package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.model.CartItem;
import cz.cvut.fel.ts.ts_semestralni_prace.service.CartService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Controller
@RequestMapping("/configurator")
public class ConfiguratorController {

    private static final BigDecimal PRICE_CONE = new BigDecimal("15.00");
    private static final BigDecimal PRICE_CUP = new BigDecimal("10.00");
    private static final BigDecimal PRICE_SCOOP = new BigDecimal("25.00");
    private static final BigDecimal PRICE_TOPPING = new BigDecimal("10.00");
    private static final BigDecimal PRICE_COOKIE = new BigDecimal("15.00");

    private static final List<String> FLAVORS = List.of(
            "Vanilka", "Čokoláda", "Jahoda", "Pistácie", "Citron",
            "Malina", "Mango", "Karamel", "Máta", "Kokos",
            "Stracciatella", "Tiramisu", "Borůvka", "Meruňka", "Oříšek"
    );

    private static final List<String> TOPPINGS = List.of(
            "Čokoládový posyp", "Barevný posyp", "Kokosové vločky",
            "Ořechový posyp", "Karamelová poleva", "Čokoládová poleva"
    );

    private static final List<String> COOKIES = List.of(
            "Oplatka", "Čokoládová tyčinka", "Trubička", "Sušenka", "Lžička z čokolády"
    );

    private final CartService cartService;
    private final ShopService shopService;

    public ConfiguratorController(CartService cartService, ShopService shopService) {
        this.cartService = cartService;
        this.shopService = shopService;
    }

    @GetMapping
    public String showConfigurator(Model model, HttpSession session) {
        model.addAttribute("shops", shopService.getActive());
        model.addAttribute("flavors", FLAVORS);
        model.addAttribute("toppings", TOPPINGS);
        model.addAttribute("cookies", COOKIES);
        model.addAttribute("cartCount", cartService.getItemCount(session));

        model.addAttribute("priceCone", PRICE_CONE);
        model.addAttribute("priceCup", PRICE_CUP);
        model.addAttribute("priceScoop", PRICE_SCOOP);
        model.addAttribute("priceTopping", PRICE_TOPPING);
        model.addAttribute("priceCookie", PRICE_COOKIE);

        return "configurator/index";
    }

    @PostMapping("/add")
    public String addCustomIceCream(@RequestParam String shopId,
                                     @RequestParam String container,
                                     @RequestParam List<String> flavors,
                                     @RequestParam(required = false) List<String> selectedToppings,
                                     @RequestParam(required = false) List<String> selectedCookies,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        var shop = shopService.findById(shopId).orElse(null);
        if (shop == null) {
            redirectAttributes.addFlashAttribute("error", "Vybraná zmrzlinárna neexistuje.");
            return "redirect:/configurator";
        }

        boolean isCone = "kornout".equals(container);
        String containerName = isCone ? "Kornout" : "Kelímek";
        BigDecimal containerPrice = isCone ? PRICE_CONE : PRICE_CUP;

        BigDecimal total = containerPrice;
        total = total.add(PRICE_SCOOP.multiply(BigDecimal.valueOf(flavors.size())));

        if (selectedToppings != null) {
            total = total.add(PRICE_TOPPING.multiply(BigDecimal.valueOf(selectedToppings.size())));
        }
        if (selectedCookies != null) {
            total = total.add(PRICE_COOKIE.multiply(BigDecimal.valueOf(selectedCookies.size())));
        }

        StringJoiner description = new StringJoiner(", ");
        description.add(containerName);
        description.add(flavors.size() + "× kopeček (" + String.join(", ", flavors) + ")");
        if (selectedToppings != null && !selectedToppings.isEmpty()) {
            description.add(String.join(", ", selectedToppings));
        }
        if (selectedCookies != null && !selectedCookies.isEmpty()) {
            description.add(String.join(", ", selectedCookies));
        }

        CartItem cartItem = CartItem.builder()
                .productId("custom-" + UUID.randomUUID())
                .productName("Vlastní zmrzlina: " + description)
                .shopId(shop.getId())
                .shopName(shop.getName())
                .quantity(1)
                .price(total)
                .build();

        try {
            cartService.addItem(session, cartItem);
            redirectAttributes.addFlashAttribute("success", "Vlastní zmrzlina přidána do košíku!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Košík obsahuje produkty z jiné zmrzlinárny. Nejprve vyprázdněte košík.");
        }

        return "redirect:/configurator";
    }
}
