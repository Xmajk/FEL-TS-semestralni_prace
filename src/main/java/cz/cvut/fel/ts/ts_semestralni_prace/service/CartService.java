package cz.cvut.fel.ts.ts_semestralni_prace.service;

import cz.cvut.fel.ts.ts_semestralni_prace.model.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final String CART_KEY = "cart";

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    /**
     * @throws IllegalStateException if cart already contains items from a different shop
     */
    public void addItem(HttpSession session, CartItem newItem) {
        List<CartItem> cart = getCart(session);
        if (!cart.isEmpty() && !cart.get(0).getShopId().equals(newItem.getShopId())) {
            throw new IllegalStateException("different_shop");
        }
        Optional<CartItem> existing = cart.stream()
                .filter(i -> i.getProductId().equals(newItem.getProductId()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
        } else {
            cart.add(newItem);
        }
        session.setAttribute(CART_KEY, cart);
    }

    public void removeItem(HttpSession session, String productId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(i -> i.getProductId().equals(productId));
        session.setAttribute(CART_KEY, cart);
    }

    public void updateQuantity(HttpSession session, String productId, int quantity) {
        if (quantity <= 0) {
            removeItem(session, productId);
            return;
        }
        getCart(session).stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }

    public BigDecimal getTotal(HttpSession session) {
        return getCart(session).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount(HttpSession session) {
        return getCart(session).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String getCartShopId(HttpSession session) {
        List<CartItem> cart = getCart(session);
        return cart.isEmpty() ? null : cart.get(0).getShopId();
    }
}
