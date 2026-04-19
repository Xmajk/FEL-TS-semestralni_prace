package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.model.Product;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Shop;
import cz.cvut.fel.ts.ts_semestralni_prace.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ShopService shopService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    public AdminController(ShopService shopService, ProductService productService,
                            OrderService orderService, CartService cartService, UserService userService) {
        this.shopService = shopService;
        this.productService = productService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model, HttpSession session) {
        model.addAttribute("shopCount", shopService.getAll().size());
        model.addAttribute("productCount", productService.getAll().size());
        model.addAttribute("orderCount", orderService.getAll().size());
        model.addAttribute("userCount", userService.getAll().size());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        model.addAttribute("recentOrders", orderService.getAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5).toList());
        return "admin/dashboard";
    }

    // --- SHOPS ---

    @GetMapping("/shops")
    public String shops(Model model, HttpSession session) {
        model.addAttribute("shops", shopService.getAll());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "admin/shops";
    }

    @GetMapping("/shops/new")
    public String newShop(Model model, HttpSession session) {
        model.addAttribute("shop", new Shop());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "admin/shop-form";
    }

    @GetMapping("/shops/edit/{id}")
    public String editShop(@PathVariable String id, Model model, HttpSession session) {
        return shopService.findById(id).map(shop -> {
            model.addAttribute("shop", shop);
            model.addAttribute("cartCount", cartService.getItemCount(session));
            return "admin/shop-form";
        }).orElse("redirect:/admin/shops");
    }

    @PostMapping("/shops/save")
    public String saveShop(@ModelAttribute Shop shop, RedirectAttributes redirectAttributes) {
        shopService.save(shop);
        redirectAttributes.addFlashAttribute("success", "Zmrzlinárna byla uložena.");
        return "redirect:/admin/shops";
    }

    @PostMapping("/shops/delete/{id}")
    public String deleteShop(@PathVariable String id, RedirectAttributes redirectAttributes) {
        shopService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Zmrzlinárna byla smazána.");
        return "redirect:/admin/shops";
    }

    // --- PRODUCTS ---

    @GetMapping("/products")
    public String products(Model model, HttpSession session) {
        model.addAttribute("products", productService.getAll());
        model.addAttribute("shops", shopService.getAll());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(@RequestParam(required = false) String shopId,
                              Model model, HttpSession session) {
        Product product = new Product();
        if (shopId != null) product.setShopId(shopId);
        model.addAttribute("product", product);
        model.addAttribute("shops", shopService.getAll());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable String id, Model model, HttpSession session) {
        return productService.findById(id).map(product -> {
            model.addAttribute("product", product);
            model.addAttribute("shops", shopService.getAll());
            model.addAttribute("cartCount", cartService.getItemCount(session));
            return "admin/product-form";
        }).orElse("redirect:/admin/products");
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.save(product);
        redirectAttributes.addFlashAttribute("success", "Produkt byl uložen.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable String id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Produkt byl smazán.");
        return "redirect:/admin/products";
    }

    // --- ORDERS ---

    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) {
        model.addAttribute("orders", orderService.getAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList());
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "admin/orders";
    }

    @PostMapping("/orders/status/{id}")
    public String updateOrderStatus(@PathVariable String id, @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        try {
            cz.cvut.fel.ts.ts_semestralni_prace.model.OrderStatus newStatus =
                    cz.cvut.fel.ts.ts_semestralni_prace.model.OrderStatus.valueOf(status);
            orderService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", "Status objednávky byl aktualizován.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Neplatný status: " + status);
        }
        return "redirect:/admin/orders";
    }
}
