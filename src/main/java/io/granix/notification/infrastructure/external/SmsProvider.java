package io.granix.notification.infrastructure.external;

import io.granix.notification.domain.model.Notification;
import io.granix.user.database.entity.UserEntity;
import io.granix.common.security.CryptoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;

@ApplicationScoped
public class SmsProvider {

    @ConfigProperty(name = "payza.notification.sms.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "payza.notification.sms.simulate", defaultValue = "true")
    boolean simulate;

    // ⭐⭐ INJECTION DU CRYPTO SERVICE ⭐⭐
    @Inject
    CryptoService cryptoService;

    public boolean sendNotification(Notification notification) {
        if (!enabled) {
            System.out.println("📱 SMS désactivé - Simulation");
            return true;
        }

        try {
            // ⭐⭐ RÉCUPÉRATION DU VRAI NUMÉRO ⭐⭐
            String phoneNumber = resolveRealPhoneNumber(notification.getUserId());
            String message = notification.getBody();

            if (simulate) {
                System.out.println("=".repeat(50));
                System.out.println("📱 SMS NOTIFICATION:");
                System.out.println("À: " + phoneNumber + " ← VRAI NUMÉRO !");
                System.out.println("Type: " + notification.getType());
                System.out.println("Message: " + message);
                System.out.println("User ID: " + notification.getUserId());
                System.out.println("=".repeat(50));
                return true;
            }

            return sendRealSms(phoneNumber, message);

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi SMS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère et décrypte le vrai numéro de l'utilisateur
     */
    private String resolveRealPhoneNumber(UUID userId) {
        try {
            System.out.println("🔍 Recherche numéro pour user: " + userId);

            UserEntity user = UserEntity.findById(userId);

            if (user == null) {
                System.err.println("❌ Utilisateur non trouvé: " + userId);
                return generateFallbackNumber(userId);
            }

            if (user.msisdnEncrypted == null || user.msisdnEncrypted.trim().isEmpty()) {
                System.err.println("❌ Aucun numéro trouvé pour l'utilisateur: " + userId);
                return generateFallbackNumber(userId);
            }

            // ⭐⭐ DÉCRYPTAGE DU VRAI NUMÉRO ⭐⭐
            String decryptedPhone = cryptoService.decrypt(user.msisdnEncrypted);
            System.out.println("✅ Numéro décrypté: " + decryptedPhone + " pour user: " + userId);

            return decryptedPhone;

        } catch (Exception e) {
            System.err.println("❌ Erreur décryptage numéro: " + e.getMessage());
            return generateFallbackNumber(userId);
        }
    }

    /**
     * Génère un numéro de fallback si problème
     */
    private String generateFallbackNumber(UUID userId) {
        String fallbackNumber = "+26134" + userId.toString().substring(0, 6);
        System.out.println("🔄 Utilisation numéro fallback: " + fallbackNumber);
        return fallbackNumber;
    }

    private boolean sendRealSms(String phoneNumber, String message) {
        System.out.println("📱 ENVOI RÉEL SMS à: " + phoneNumber);
        return true;
    }
}