package cz.cvut.fel.ts.ts_semestralni_prace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private String id;
    private String userId; // null for guest orders
    private String shopId;
    private String shopName;
    private List<OrderItem> items;
    private CustomerDetails customerDetails;
    private String status; // CONFIRMED, READY, CANCELLED
    private BigDecimal totalPrice;
    private String createdAt;
}
