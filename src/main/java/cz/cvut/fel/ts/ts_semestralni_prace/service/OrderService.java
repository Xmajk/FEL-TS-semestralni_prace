package cz.cvut.fel.ts.ts_semestralni_prace.service;

import cz.cvut.fel.ts.ts_semestralni_prace.model.*;
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

    public OrderService(FileStorageService fileStorageService, ProductService productService) {
        this.fileStorageService = fileStorageService;
        this.productService = productService;
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

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .shopId(shopId)
                .shopName(shopName)
                .items(orderItems)
                .customerDetails(customerDetails)
                .status("CONFIRMED")
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

    public void updateStatus(String orderId, String status) {
        List<Order> orders = getAll();
        orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .ifPresent(o -> o.setStatus(status));
        fileStorageService.writeList(FILENAME, orders);
    }
}
