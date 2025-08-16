package com.awesomepizza.orderingservice.model.enums;

public enum OrderStatus {
    PENDING("In attesa"),
    IN_PREPARATION("In preparazione"),
    READY("Pronto"),
    COMPLETED("Completato");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == IN_PREPARATION;
            case IN_PREPARATION -> newStatus == READY;
            case READY -> newStatus == COMPLETED;
            case COMPLETED -> false;
        };
    }
}
