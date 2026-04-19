package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.model.CartItem;
import cz.cvut.fel.ts.ts_semestralni_prace.model.CustomerDetails;
import cz.cvut.fel.ts.ts_semestralni_prace.model.Order;
import cz.cvut.fel.ts.ts_semestralni_prace.model.OrderStatus;
import cz.cvut.fel.ts.ts_semestralni_prace.service.CartService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.OrderService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.ShopService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final ShopService shopService;
    private final UserService userService;

    public OrderController(CartService cartService, OrderService orderService,
                            ShopService shopService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.shopService = shopService;
        this.userService = userService;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session,
                            Authentication authentication, RedirectAttributes redirectAttributes) {
        List<CartItem> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Košík je prázdný.");
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cart);
        model.addAttribute("total", cartService.getTotal(session));
        model.addAttribute("cartCount", cartService.getItemCount(session));
        model.addAttribute("customerDetails", new CustomerDetails());
        String shopId = cartService.getCartShopId(session);
        shopService.findById(shopId).ifPresent(s -> model.addAttribute("pickupShop", s));

        if (authentication != null && authentication.isAuthenticated()) {
            userService.findByUsername(authentication.getName())
                    .ifPresent(u -> model.addAttribute("loggedUser", u));
        }
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@ModelAttribute CustomerDetails customerDetails,
                                   HttpSession session,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        List<CartItem> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Košík je prázdný.");
            return "redirect:/cart";
        }

        String shopId = cartService.getCartShopId(session);
        String shopName = shopService.findById(shopId).map(s -> s.getName()).orElse("Neznámá zmrzlinárna");
        String userId = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;

        Order order;
        try {
            order = orderService.createOrder(cart, customerDetails, shopId, shopName, userId);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
        cartService.clearCart(session);

        return "redirect:/order/confirmation/" + order.getId();
    }

    @GetMapping("/confirmation/{orderId}")
    public String confirmation(@PathVariable String orderId, Model model, HttpSession session) {
        return orderService.findById(orderId).map(order -> {
            model.addAttribute("order", order);
            model.addAttribute("cartCount", cartService.getItemCount(session));
            shopService.findById(order.getShopId()).ifPresent(s -> model.addAttribute("pickupShop", s));
            return "order/confirmation";
        }).orElse("redirect:/");
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable String orderId, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Order order = orderService.findById(orderId).orElse(null);
        if (order == null || !authentication.getName().equals(order.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Objednávka nenalezena.");
            return "redirect:/order/moje";
        }
        try {
            orderService.updateStatus(orderId, OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Objednávka byla zrušena.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/moje";
    }

    @GetMapping("/moje")
    public String myOrders(Model model, HttpSession session, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("orders", orderService.getByUsername(authentication.getName()));
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "order/my-orders";
    }
}
