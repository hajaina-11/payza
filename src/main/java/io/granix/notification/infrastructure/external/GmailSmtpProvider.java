package io.granix.notification.infrastructure.external;

import io.granix.notification.domain.model.Notification;
import io.granix.user.database.entity.UserEntity;
import io.granix.common.security.CryptoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// ⭐ IMPORTS JAKARTA MAIL MODERNES ⭐
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.UUID;

@ApplicationScoped
public class GmailSmtpProvider {

    @ConfigProperty(name = "payza.notification.email.gmail.address")
    String gmailUsername;

    @ConfigProperty(name = "payza.notification.email.gmail.password")
    String gmailPassword;

    @Inject
    CryptoService cryptoService;

    public boolean sendRealEmail(Notification notification) {
        System.out.println("📧 [SMTP AUTOMATIQUE] Envoi pour nouvelle inscription...");

        try {
            String toEmail = resolveRealEmail(notification.getUserId());
            String subject = notification.getTitle();
            String body = notification.getBody();

            System.out.println("📍 Destinataire: " + toEmail);
            System.out.println("📋 Sujet: " + subject);

            // Configuration SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(gmailUsername, gmailPassword);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);

            // De
            message.setFrom(new InternetAddress(gmailUsername, "Payza"));

            // À
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            // Sujet
            message.setSubject("Payza - " + subject);

            // Corps du message
            message.setText(body);

            // Envoi RÉEL
            System.out.println("🚀 Envoi EMAIL AUTOMATIQUE via SMTP...");
            Transport.send(message);

            System.out.println("✅ EMAIL RÉEL ENVOYÉ AUTOMATIQUEMENT à: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ ERREUR ENVOI SMTP AUTOMATIQUE: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String resolveRealEmail(UUID userId) {
        try {
            UserEntity user = UserEntity.findById(userId);

            if (user == null) {
                System.err.println("❌ Utilisateur non trouvé pour email: " + userId);
                return "fallback@payza.mg";
            }

            if (user.emailEncrypted == null || user.emailEncrypted.trim().isEmpty()) {
                System.err.println("❌ Email crypté non trouvé pour: " + userId);
                return "fallback@payza.mg";
            }

            // ⭐ DÉCRYPTAGE RÉEL DE L'EMAIL ⭐
            String decryptedEmail = cryptoService.decrypt(user.emailEncrypted);
            System.out.println("✅ Email décrypté pour envoi automatique: " + decryptedEmail);

            return decryptedEmail;

        } catch (Exception e) {
            System.err.println("❌ Erreur décryptage email automatique: " + e.getMessage());
            return "fallback@payza.mg";
        }
    }
}