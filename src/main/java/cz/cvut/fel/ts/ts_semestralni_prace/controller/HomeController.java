package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.service.CartService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ShopService shopService;
    private final CartService cartService;

    public HomeController(ShopService shopService, CartService cartService) {
        this.shopService = shopService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("shops", shopService.getActive());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "index";
    }
}
