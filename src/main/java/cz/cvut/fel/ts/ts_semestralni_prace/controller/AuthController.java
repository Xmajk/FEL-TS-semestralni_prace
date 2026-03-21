package cz.cvut.fel.ts.ts_semestralni_prace.controller;

import cz.cvut.fel.ts.ts_semestralni_prace.service.CartService;
import cz.cvut.fel.ts.ts_semestralni_prace.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final CartService cartService;

    public AuthController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model, HttpSession session) {
        model.addAttribute("cartCount", cartService.getItemCount(session));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String passwordConfirm,
                            @RequestParam String email,
                            @RequestParam String firstName,
                            @RequestParam String lastName,
                            RedirectAttributes redirectAttributes) {
        if (!password.equals(passwordConfirm)) {
            redirectAttributes.addFlashAttribute("error", "Hesla se neshodují.");
            return "redirect:/register";
        }
        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Uživatelské jméno \"" + username + "\" je již obsazeno.");
            return "redirect:/register";
        }
        userService.register(username, password, email, firstName, lastName);
        redirectAttributes.addFlashAttribute("success", "Registrace proběhla úspěšně! Nyní se přihlaste.");
        return "redirect:/login";
    }
}
