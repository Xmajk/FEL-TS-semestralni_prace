package cz.cvut.fel.ts.ts_semestralni_prace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    private String id;
    private String shopId;
    private String name;
    private String description;
    private String flavor;
    private BigDecimal price;
    private int stockQuantity;
    private boolean available;
}
