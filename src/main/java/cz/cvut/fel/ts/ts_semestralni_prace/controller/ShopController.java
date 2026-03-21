package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.service.CartService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ProductService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;
    private final ProductService productService;
    private final CartService cartService;

    public ShopController(ShopService shopService, ProductService productService, CartService cartService) {
        this.shopService = shopService;
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/{shopId}")
    public String shopDetail(@PathVariable String shopId, Model model, HttpSession session) {
        return shopService.findById(shopId).map(shop -> {
            model.addAttribute("shop", shop);
            model.addAttribute("products", productService.getByShopId(shopId));
            model.addAttribute("cartCount", cartService.getItemCount(session));
            model.addAttribute("cartShopId", cartService.getCartShopId(session));
            return "shop/detail";
        }).orElse("redirect:/");
    }
}
