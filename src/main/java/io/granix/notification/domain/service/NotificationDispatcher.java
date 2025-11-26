package io.granix.notification.domain.service;

import io.granix.notification.domain.model.Notification;
import io.granix.notification.domain.model.NotificationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class NotificationDispatcher {

    public Optional<Notification> prepareForDispatch(Notification notification) {
        try {
            System.out.println("🔍 DEBUG: Début prepareForDispatch");
            System.out.println("🔍 DEBUG: Notification reçue - " + notification);

            if (!isValidForDispatch(notification)) {
                System.err.println("❌ Notification invalide pour envoi");
                return Optional.empty();
            }

            System.out.println("🔍 DEBUG: Notification valide, préparation...");

            // VERSION ULTRA SIMPLE - Pas de modification du body
            Notification readyNotification = notification.markAsProcessing();

            System.out.println("🔍 DEBUG: Notification préparée avec succès");
            System.out.println("✅ Notification préparée: " + readyNotification.getType());

            return Optional.of(readyNotification);

        } catch (Exception e) {
            System.err.println("💥 Erreur critique préparation notification: " + e.getMessage());
            e.printStackTrace(); // ← AFFICHE LA STACK TRACE COMPLÈTE
            return Optional.empty();
        }
    }

    private boolean isValidForDispatch(Notification notification) {
        boolean valid = notification.getUserId() != null &&
                notification.getBody() != null &&
                !notification.getBody().trim().isEmpty();

        System.out.println("🔍 DEBUG: isValidForDispatch = " + valid);
        System.out.println("🔍 DEBUG: userId = " + notification.getUserId());
        System.out.println("🔍 DEBUG: body = " + notification.getBody());

        return valid;
    }
}