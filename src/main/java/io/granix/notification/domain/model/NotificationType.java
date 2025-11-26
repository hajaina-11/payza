package io.granix.notification.domain.model;

public enum NotificationType {
    USER_WELCOME("user.welcome", "Message de Bienvenue"),
    USER_PROFILE_UPDATED("user.profile.updated", "Profil Mis à Jour"),
    WALLET_CREATED("wallet.created", "Création de Wallet"),
    WALLET_BALANCE_ALERT("wallet.balance.alert", "Alerte de Solde"),
    SECURITY_ALERT("security.alert", "Alerte de Sécurité"),
    LOGIN_ATTEMPT("login.attempt", "Tentative de Connexion"),

    ACCOUNT_CREATED("account.created", "Création de Compte Complet");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}