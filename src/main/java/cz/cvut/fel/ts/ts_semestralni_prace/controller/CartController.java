package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.model.CartItem;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import cz.cvut.fel.ts.ts_semestralni_prace.service.CartService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ProductService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final ShopService shopService;

    public CartController(CartService cartService, ProductService productService, ShopService shopService) {
        this.cartService = cartService;
        this.productService = productService;
        this.shopService = shopService;
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        model.addAttribute("cartItems", cartService.getCart(session));
        model.addAttribute("total", cartService.getTotal(session));
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "cart/view";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable String productId,
                             @RequestParam(defaultValue = "1") int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Product product = productService.findById(productId).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Produkt nenalezen.");
            return "redirect:/";
        }

        if (!product.isAvailable() || product.getStockQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Nedostatek zásob.");
            return "redirect:/shop/" + product.getShopId();
        }

        String shopName = shopService.findById(product.getShopId())
                .map(s -> s.getName()).orElse("Neznámá zmrzlinárna");

        CartItem cartItem = CartItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .shopId(product.getShopId())
                .shopName(shopName)
                .quantity(quantity)
                .price(product.getPrice())
                .build();

        try {
            cartService.addItem(session, cartItem);
            redirectAttributes.addFlashAttribute("success", "\"" + product.getName() + "\" přidáno do košíku!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Košík obsahuje produkty z jiné zmrzlinárny. Nejprve vyprázdněte košík.");
        }
        return "redirect:/shop/" + product.getShopId();
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable String productId, HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable String productId,
                                  @RequestParam int quantity,
                                  HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }
}
