package io.granix.notification.domain.model;

public enum NotificationStatus {
    PENDING("En attente"),
    PROCESSING("En cours de traitement"),
    SENT("Envoyée"),
    DELIVERED("Livrée"),
    FAILED("Échouée"),
    CANCELLED("Annulée");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}