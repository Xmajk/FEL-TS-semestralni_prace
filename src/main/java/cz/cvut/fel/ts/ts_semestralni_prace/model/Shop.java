package cz.cvut.fel.ts.ts_semestralni_prace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
    private String id;
    private String name;
    private String address;
    private String city;
    private String description;
    private String imageEmoji;
    private boolean active;
}
