package cz.cvut.fel.ts.ts_semestralni_prace.service;

import cz.cvut.fel.ts.ts_semestralni_prace.model.*;
import cz.cvut.fel.ts.ts_semestralni_prace.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final String FILENAME = "orders.json";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final FileStorageService fileStorageService;
    private final ProductService productService;
    private final BigDecimal minOrderTotal;

    public OrderService(FileStorageService fileStorageService, ProductService productService,
                        @org.springframework.beans.factory.annotation.Value("${app.order.min-total:50}") BigDecimal minOrderTotal) {
        this.fileStorageService = fileStorageService;
        this.productService = productService;
        this.minOrderTotal = minOrderTotal;
    }

    public List<Order> getAll() {
        return fileStorageService.readList(FILENAME, Order.class);
    }

    public Optional<Order> findById(String id) {
        return getAll().stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    public List<Order> getByUsername(String username) {
        return getAll().stream()
                .filter(o -> username.equals(o.getUserId()))
                .toList();
    }

    public Order createOrder(List<CartItem> cartItems, CustomerDetails customerDetails,
                              String shopId, String shopName, String userId) {
        List<OrderItem> orderItems = cartItems.stream().map(ci -> OrderItem.builder()
                .productId(ci.getProductId())
                .productName(ci.getProductName())
                .quantity(ci.getQuantity())
                .price(ci.getPrice())
                .subtotal(ci.getSubtotal())
                .build()).toList();

        BigDecimal total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(minOrderTotal) < 0) {
            throw new IllegalStateException(
                    "Minimální hodnota objednávky je " + minOrderTotal + " Kč. Aktuální: " + total + " Kč.");
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .shopId(shopId)
                .shopName(shopName)
                .items(orderItems)
                .customerDetails(customerDetails)
                .status(OrderStatus.NEW)
                .totalPrice(total)
                .createdAt(LocalDateTime.now().format(FORMATTER))
                .build();

        for (CartItem ci : cartItems) {
            productService.reduceStock(ci.getProductId(), ci.getQuantity());
        }

        List<Order> orders = getAll();
        orders.add(order);
        fileStorageService.writeList(FILENAME, orders);
        return order;
    }

    /**
     * @throws IllegalStateException if the transition is not allowed
     */
    public void updateStatus(String orderId, OrderStatus newStatus) {
        List<Order> orders = getAll();
        Order order = orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Objednávka nenalezena: " + orderId));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Nelze změnit stav z " + order.getStatus().getLabel() + " na " + newStatus.getLabel());
        }

        order.setStatus(newStatus);
        fileStorageService.writeList(FILENAME, orders);

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                productService.restoreStock(item.getProductId(), item.getQuantity());
            }
        }
    }
}
