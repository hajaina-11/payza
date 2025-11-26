package io.granix.notification.infrastructure.external;

import io.granix.notification.domain.model.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.UUID;

@ApplicationScoped
public class PushNotificationProvider {

    @ConfigProperty(name = "payza.notification.push.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "payza.notification.push.simulate", defaultValue = "true")
    boolean simulate;

    public boolean sendNotification(Notification notification) {
        if (!enabled) {
            System.out.println("📱 Push désactivé - Simulation");
            return true;
        }

        try {
            String deviceToken = resolveDeviceToken(notification.getUserId());

            if (simulate) {
                System.out.println("=".repeat(50));
                System.out.println("📱 PUSH NOTIFICATION:");
                System.out.println("User: " + notification.getUserId());
                System.out.println("Device: " + deviceToken);
                System.out.println("Titre: " + notification.getTitle());
                System.out.println("Corps: " + notification.getBody());
                System.out.println("Type: " + notification.getType());
                System.out.println("=".repeat(50));
                return true;
            }

            // TODO: Implémentation FCM (Firebase) ou APNS (Apple)
            return sendRealPush(deviceToken, notification);

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi push: " + e.getMessage());
            return false;
        }
    }

    private String resolveDeviceToken(UUID userId) {
        // TODO: Intégration avec UserService/DeviceService
        // Pour l'instant, simulation
        return "device-token-simulated-" + userId.toString().substring(0, 8);
    }

    private boolean sendRealPush(String deviceToken, Notification notification) {
        // Implémentation FCM/APNS
        System.out.println("📱 ENVOI RÉEL PUSH à: " + deviceToken);
        return true;
    }
}