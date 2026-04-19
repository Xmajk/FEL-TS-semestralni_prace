package cz.cvut.fel.ts.ts_semestralni_prace.model;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    NEW,
    CONFIRMED,
    PREPARING,
    READY,
    PICKED_UP,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            NEW, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(PREPARING, CANCELLED),
            PREPARING, Set.of(READY),
            READY, Set.of(PICKED_UP),
            PICKED_UP, Set.of(),
            CANCELLED, Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public String getLabel() {
        return switch (this) {
            case NEW -> "Nová";
            case CONFIRMED -> "Potvrzená";
            case PREPARING -> "Připravuje se";
            case READY -> "Připravena";
            case PICKED_UP -> "Vyzvednuta";
            case CANCELLED -> "Zrušena";
        };
    }

    public String getBadgeClass() {
        return switch (this) {
            case NEW -> "bg-warning text-dark";
            case CONFIRMED -> "bg-success";
            case PREPARING -> "bg-info text-dark";
            case READY -> "bg-primary";
            case PICKED_UP -> "bg-secondary";
            case CANCELLED -> "bg-danger";
        };
    }
}
