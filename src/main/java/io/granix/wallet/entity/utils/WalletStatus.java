package io.granix.wallet.entity.utils;

public enum WalletStatus {
    // Statuts initiaux
    PENDING_VERIFICATION("Pending verification"),
    PENDING_ACTIVATION("Pending activation"),

    // Statuts actifs
    ACTIVE("Active"),
    VERIFIED("Verified"), // KYC complet

    // Statuts de restriction
    LIMITED("Limited"), // Fonctionnalités restreintes
    SUSPENDED("Suspended"), // Suspension temporaire
    FROZEN("Frozen"), // Gel des fonds pour enquête

    // Statuts de fermeture
    CLOSING("Closing"),
    CLOSED("Closed"),
    DORMANT("Dormant"), // Inactif depuis longtemps

    // Statuts d'exception
    BLOCKED("Blocked"), // Fraude détectée
    COMPROMISED("Compromised"); // Sécurité compromise

    private final String description;

    WalletStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
