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
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

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
    public String shopDetail(@PathVariable String shopId,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String flavor,
                             @RequestParam(required = false) BigDecimal minPrice,
                             @RequestParam(required = false) BigDecimal maxPrice,
                             @RequestParam(defaultValue = "false") boolean onlyAvailable,
                             Model model, HttpSession session) {
        return shopService.findById(shopId).map(shop -> {
            boolean hasFilter = (search != null && !search.isBlank())
                    || (flavor != null && !flavor.isBlank())
                    || minPrice != null || maxPrice != null || onlyAvailable;

            model.addAttribute("shop", shop);
            model.addAttribute("products", productService.filterByShopId(shopId, search, flavor, minPrice, maxPrice, onlyAvailable));
            model.addAttribute("availableFlavors", productService.getFlavorsByShopId(shopId));
            model.addAttribute("cartCount", cartService.getItemCount(session));
            model.addAttribute("cartShopId", cartService.getCartShopId(session));
            model.addAttribute("filterSearch", search);
            model.addAttribute("filterFlavor", flavor);
            model.addAttribute("filterMinPrice", minPrice);
            model.addAttribute("filterMaxPrice", maxPrice);
            model.addAttribute("filterOnlyAvailable", onlyAvailable);
            model.addAttribute("hasFilter", hasFilter);
            return "shop/detail";
        }).orElse("redirect:/");
    }
}
