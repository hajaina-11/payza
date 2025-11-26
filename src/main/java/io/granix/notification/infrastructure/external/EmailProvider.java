package io.granix.notification.infrastructure.external;

import io.granix.notification.domain.model.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailProvider {

    @ConfigProperty(name = "payza.notification.email.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "payza.notification.email.simulate", defaultValue = "false")
    boolean simulate;

    @Inject
    GmailSmtpProvider smtpProvider;

    public boolean sendNotification(Notification notification) {
        if (!enabled) {
            System.out.println("📧 Email désactivé");
            return false;
        }

        System.out.println("🎯 EmailProvider: Envoi AUTOMATIQUE déclenché");

        if (simulate) {
            System.out.println("📧 [SIMULATION] Email serait envoyé pour: " + notification.getUserId());
            return true;
        }

        // ⭐ ENVOI RÉEL AUTOMATIQUE ⭐
        boolean success = smtpProvider.sendRealEmail(notification);

        if (success) {
            System.out.println("✅ EmailProvider: Email RÉEL envoyé AUTOMATIQUEMENT");
        } else {
            System.err.println("❌ EmailProvider: Échec envoi email AUTOMATIQUE");
        }

        return success;
    }
}