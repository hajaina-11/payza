package io.granix.notification.application.service;

import io.granix.common.event.user.UserCreatedEvent;
import io.granix.common.event.user.UserLoggedInEvent;
import io.granix.notification.domain.model.Notification;
import io.granix.notification.domain.model.NotificationChannel;
import io.granix.notification.domain.model.NotificationType;
import io.granix.notification.domain.service.NotificationDispatcher;
import io.granix.notification.infrastructure.external.PushNotificationProvider;
import io.granix.notification.infrastructure.external.SmsProvider;
import io.granix.notification.infrastructure.external.EmailProvider;
import io.granix.notification.infrastructure.persistence.NotificationEntity;
import io.granix.user.database.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationOrchestrator {

    @Inject
    NotificationDispatcher dispatcher;

    @Inject
    EmailProvider emailProvider;

    @Inject
    SmsProvider smsProvider;

    @Inject
    PushNotificationProvider pushProvider;

    /**
     * ⭐ ÉVÉNEMENT POUR L'EMAIL RETARDÉ AVEC IBAN
     */
    public static class DelayedIbanEmailEvent {
        private final UUID userId;
        public DelayedIbanEmailEvent(UUID userId) { this.userId = userId; }
        public UUID getUserId() { return userId; }
    }

    @Transactional
    public void handleUserCreated(@ObservesAsync UserCreatedEvent event) {
        System.out.println("🎯 Notification Orchestrator: Nouvel utilisateur détecté - " + event.userId());

        try {
            // ⭐ 1. ENVOYER LES NOTIFICATIONS STANDARDS IMMÉDIATEMENT
            sendStandardWelcomeNotifications(event);

            // ⭐ 2. PLANIFIER L'EMAIL AVEC IBAN (sans toucher au WalletCreator)
            scheduleDelayedIbanEmail(event.userId());

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement création utilisateur: " + e.getMessage());
        }
    }

    /**
     * ⭐ PLANIFIE L'ENVOI DE L'EMAIL AVEC IBAN
     */
    private void scheduleDelayedIbanEmail(UUID userId) {
        try {
            // Utiliser un thread séparé avec contexte CDI
            new Thread(() -> {
                try {
                    // Attendre que le WalletCreator ait fini
                    Thread.sleep(5000); // 5 secondes pour être sûr

                    // Émettre l'événement dans le contexte CDI
                    CDI.current().getBeanManager().getEvent()
                            .fireAsync(new DelayedIbanEmailEvent(userId));

                    System.out.println("⏰ Email IBAN planifié pour: " + userId);
                } catch (Exception e) {
                    System.err.println("❌ Erreur planification email IBAN: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("❌ Erreur scheduleDelayedIbanEmail: " + e.getMessage());
        }
    }

    /**
     * ⭐ GÈRE L'ÉVÉNEMENT EMAIL RETARDÉ
     */
    @Transactional
    public void handleDelayedIbanEmail(@ObservesAsync DelayedIbanEmailEvent event) {
        try {
            System.out.println("📧 Traitement email IBAN retardé pour: " + event.getUserId());
            sendWelcomeEmailWithIban(event.getUserId());
        } catch (Exception e) {
            System.err.println("❌ Erreur handleDelayedIbanEmail: " + e.getMessage());
        }
    }

    /**
     * ⭐ ENVOIE LES NOTIFICATIONS STANDARDS (SMS, PUSH, IN-APP)
     */
    private void sendStandardWelcomeNotifications(UserCreatedEvent event) {
        List<Notification> standardNotifications = List.of(
                // Notification SMS
                Notification.create(
                        event.userId(),
                        NotificationType.USER_WELCOME,
                        NotificationChannel.SMS,
                        "Bienvenue sur Payza!",
                        "Félicitations! Votre compte Payza a été créé avec succès."
                ),
                // Notification Push
                Notification.create(
                        event.userId(),
                        NotificationType.USER_WELCOME,
                        NotificationChannel.PUSH,
                        "Bienvenue sur Payza!",
                        "Votre compte a été créé avec succès"
                ),
                // Notification In-App
                Notification.create(
                        event.userId(),
                        NotificationType.USER_WELCOME,
                        NotificationChannel.IN_APP,
                        "Bienvenue sur Payza!",
                        "Découvrez toutes nos fonctionnalités"
                )
        );

        // Traiter les notifications standards
        for (Notification notification : standardNotifications) {
            processNotification(notification);
        }
    }

    /**
     * ⭐ ENVOIE L'EMAIL DE BIENVENUE AVEC IBAN
     */
    @Transactional
    public void sendWelcomeEmailWithIban(UUID userId) {
        try {
            System.out.println("📧 Préparation email de bienvenue avec IBAN pour: " + userId);

            // Récupérer les infos utilisateur
            var userInfo = getUserInfo(userId);
            if (userInfo == null) {
                System.err.println("❌ Utilisateur non trouvé pour email IBAN: " + userId);
                return;
            }

            // Essayer de récupérer le wallet et l'IBAN
            var walletInfo = getWalletInfo(userId);

            String emailBody;
            String emailSubject;

            if (walletInfo != null) {
                // ✅ Wallet trouvé - email avec IBAN
                emailBody = buildWelcomeEmailWithIban(userInfo, walletInfo);
                emailSubject = "🎉 Bienvenue sur Payza ! Votre compte est activé";
                System.out.println("✅ IBAN trouvé pour: " + userId);
            } else {
                // ❌ Wallet non trouvé - email sans IBAN
                emailBody = buildWelcomeEmailWithoutIban(userInfo);
                emailSubject = "Bienvenue sur Payza !";
                System.out.println("⚠️  Wallet non trouvé, email sans IBAN pour: " + userId);
            }

            // Créer la notification email
            Notification emailNotification = Notification.create(
                    userId,
                    NotificationType.ACCOUNT_CREATED,
                    NotificationChannel.EMAIL,
                    emailSubject,
                    emailBody
            );

            processNotification(emailNotification);

            System.out.println("✅ Email de bienvenue envoyé à: " + userInfo.email());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email avec IBAN: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void processNotification(Notification notification) {
        try {
            System.out.println("🔄 Traitement notification: " + notification.getType());

            var preparedNotification = dispatcher.prepareForDispatch(notification);
            if (preparedNotification.isEmpty()) {
                System.err.println("❌ Échec préparation notification");
                return;
            }

            Notification readyNotification = preparedNotification.get();

            // COMPLÉTEZ LE SWITCH AVEC TOUS LES CANAUX
            boolean sent = switch (readyNotification.getChannel()) {
                case SMS -> {
                    System.out.println("🔍 DEBUG: Envoi SMS...");
                    boolean result = smsProvider.sendNotification(readyNotification);
                    System.out.println("🔍 DEBUG: Résultat SMS: " + result);
                    yield result;
                }
                case EMAIL -> {
                    System.out.println("🔍 DEBUG: Envoi Email...");
                    boolean result = emailProvider.sendNotification(readyNotification);
                    System.out.println("🔍 DEBUG: Résultat Email: " + result);
                    yield result;
                }
                case PUSH -> {
                    System.out.println("🔍 DEBUG: Envoi Push...");
                    boolean result = pushProvider.sendNotification(readyNotification);
                    System.out.println("🔍 DEBUG: Résultat Push: " + result);
                    yield result;
                }
                case IN_APP -> {
                    System.out.println("🔍 DEBUG: Envoi In-App...");
                    boolean result = sendInAppNotification(readyNotification);
                    System.out.println("🔍 DEBUG: Résultat In-App: " + result);
                    yield result;
                }
            };

            if (sent) {
                Notification sentNotification = readyNotification.markAsSent();
                persistNotification(sentNotification);
                System.out.println("✅ Notification envoyée: " + sentNotification.getType() + " via " + sentNotification.getChannel());
            } else {
                System.err.println("❌ Échec envoi notification: " + readyNotification.getType() + " via " + readyNotification.getChannel());
            }

        } catch (Exception e) {
            System.err.println("💥 Erreur critique traitement notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean sendEmailNotification(Notification notification) {
        System.out.println("📧 Email simulé pour: " + notification.getUserId());
        return true;
    }

    @Transactional
    public void persistNotification(Notification notification) {
        NotificationEntity entity = new NotificationEntity(notification);
        entity.persist();
        System.out.println("💾 Notification persistée: " + entity.id);
    }

    private boolean sendInAppNotification(Notification notification) {
        try {
            System.out.println("=".repeat(50));
            System.out.println("🔔 IN-APP NOTIFICATION:");
            System.out.println("User: " + notification.getUserId());
            System.out.println("Titre: " + notification.getTitle());
            System.out.println("Corps: " + notification.getBody());
            System.out.println("Type: " + notification.getType());
            System.out.println("=".repeat(50));

            // Simulation - en réel, stocker en base pour affichage dans l'UI
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification in-app: " + e.getMessage());
            return false;
        }
    }

    //login notification

    @Transactional
    public void handleUserLoggedIn(@ObservesAsync UserLoggedInEvent event) {
        System.out.println("🎯 Notification Orchestrator: Connexion utilisateur détectée - " + event.userId());

        try {
            // Récupérer les infos de l'utilisateur
            UserEntity user = UserEntity.findById(event.userId());
            if (user == null) {
                System.err.println("❌ Utilisateur non trouvé pour notification de connexion");
                return;
            }

            String userName = user.firstname + " " + user.lastname;

            if (event.success()) {
                // ✅ Connexion réussie - Notification d'information
                createSuccessfulLoginNotification(event, user, userName);
            } else {
                // ❌ Tentative échouée - Notification de sécurité
                createFailedLoginNotification(event, user, userName);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement notification connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSuccessfulLoginNotification(UserLoggedInEvent event, UserEntity user, String userName) {
        String location = resolveLocation(event.ipAddress());

        Notification loginNotification = Notification.create(
                event.userId(),
                NotificationType.LOGIN_ATTEMPT,
                NotificationChannel.EMAIL,
                "✅ Connexion réussie à votre compte Payza",
                buildSuccessfulLoginEmailBody(userName, event.loginTime(), event.ipAddress(), location, event.userAgent())
        );

        processNotification(loginNotification);
        System.out.println("✅ Notification de connexion réussie créée pour: " + userName);
    }

    private void createFailedLoginNotification(UserLoggedInEvent event, UserEntity user, String userName) {
        String location = resolveLocation(event.ipAddress());

        Notification securityNotification = Notification.create(
                event.userId(),
                NotificationType.SECURITY_ALERT, // Utilisez SECURITY_ALERT pour les échecs
                NotificationChannel.EMAIL,
                "🚨 Tentative de connexion échouée",
                buildFailedLoginEmailBody(userName, event.loginTime(), event.ipAddress(), location, event.userAgent())
        );

        processNotification(securityNotification);
        System.out.println("🚨 Notification de tentative échouée créée pour: " + userName);
    }

    private String buildSuccessfulLoginEmailBody(String userName, LocalDateTime loginTime,
                                                 String ipAddress, String location, String userAgent) {
        return String.format("""
        Bonjour %s,
        
        Une connexion à votre compte Payza a été effectuée avec succès :
        
        📅 Date et heure : %s
        🌍 Adresse IP : %s
        📍 Localisation approximative : %s
        💻 Appareil/Navigateur : %s
        
        Si vous êtes à l'origine de cette connexion, vous pouvez ignorer cet email.
        
        ⁉️ Si vous ne reconnaissez pas cette activité, veuillez immédiatement :
        1. Changer votre mot de passe
        2. Nous contacter à support@payza.mg
        3. Vérifier vos dernières activités
        
        🔒 La sécurité de votre compte est notre priorité.
        
        Cordialement,
        L'équipe de sécurité Payza
        """,
                userName,
                loginTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                ipAddress,
                location,
                simplifyUserAgent(userAgent)
        );
    }

    private String buildFailedLoginEmailBody(String userName, LocalDateTime loginTime,
                                             String ipAddress, String location, String userAgent) {
        return String.format("""
        Bonjour %s,
        
        🚨 Une tentative de connexion échouée a été détectée sur votre compte Payza :
        
        📅 Date et heure : %s
        🌍 Adresse IP : %s
        📍 Localisation approximative : %s
        💻 Appareil/Navigateur : %s
        
        🔒 La tentative a été bloquée pour des raisons de sécurité.
        
        ✅ Si c'était vous :
        - Vérifiez votre mot de passe
        - Assurez-vous que votre compte n'est pas verrouillé
        
        🚨 Si ce n'était pas vous :
        - Votre mot de passe est sécurisé
        - Aucune action immédiate requise
        - Surveillez vos activités
        
        Pour toute question, contactez-nous à support@payza.mg
        
        Cordialement,
        L'équipe de sécurité Payza
        """,
                userName,
                loginTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                ipAddress,
                location,
                simplifyUserAgent(userAgent)
        );
    }

    private String resolveLocation(String ipAddress) {
        // Version simplifiée
        if (ipAddress == null || ipAddress.equals("IP-inconnue")) return "Non déterminée";
        if (ipAddress.startsWith("196.") || ipAddress.startsWith("41.")) return "Madagascar";
        if (ipAddress.startsWith("127.0.0.1") || ipAddress.startsWith("localhost")) return "Local (Développement)";
        return "International";
    }

    private String simplifyUserAgent(String userAgent) {
        if (userAgent == null) return "Inconnu";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Postman")) return "Postman (API Test)";
        if (userAgent.contains("Mobile")) return "Appareil Mobile";
        return "Navigateur/Appareil";
    }

    // ============================================================
    // ⭐ MÉTHODES AUXILIAIRES POUR L'EMAIL AVEC IBAN
    // ============================================================

    /**
     * ⭐ RECORD POUR LES INFOS UTILISATEUR
     */
    private record UserInfo(UUID userId, String fullName, String email, String phoneNumber) {}

    /**
     * ⭐ RECORD POUR LES INFOS WALLET
     */
    private record WalletInfo(UUID walletId, String iban, String currency) {}

    /**
     * ⭐ RÉCUPÈRE LES INFOS UTILISATEUR
     */
    @Transactional
    private UserInfo getUserInfo(UUID userId) {
        try {
            UserEntity user = UserEntity.findById(userId);
            if (user == null) {
                System.err.println("❌ Utilisateur non trouvé: " + userId);
                return null;
            }

            // Décrypter email et téléphone
            String userEmail = decryptUserEmail(user);
            String userPhone = decryptUserPhone(user);
            String userName = getUserFullName(user);

            return new UserInfo(userId, userName, userEmail, userPhone);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération infos utilisateur: " + e.getMessage());
            return null;
        }
    }

    /**
     * ⭐ RÉCUPÈRE LES INFOS WALLET
     */
    @Transactional
    private WalletInfo getWalletInfo(UUID userId) {
        try {
            // Injecter le service wallet
            var walletService = CDI.current()
                    .select(io.granix.wallet.service.WalletInformationService.class)
                    .get();

            var wallets = walletService.searchByUser(userId);
            if (wallets.isEmpty()) {
                System.out.println("⚠️  Aucun wallet trouvé pour: " + userId);
                return null;
            }

            var wallet = wallets.get(0);
            String decryptedIban = decryptWalletIban(wallet);

            if (decryptedIban == null || decryptedIban.equals("IBAN non disponible")) {
                System.out.println("⚠️  IBAN non disponible pour: " + userId);
                return null;
            }

            return new WalletInfo(wallet.id, decryptedIban, wallet.currencyCode);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération infos wallet: " + e.getMessage());
            return null;
        }
    }

    /**
     * ⭐ DÉCRYPTE L'IBAN DU WALLET
     */
    private String decryptWalletIban(Object wallet) {
        try {
            var ibanSecurity = CDI.current()
                    .select(io.granix.wallet.service.iban.IbanSecurityService.class)
                    .get();

            // Accéder au champ ibanEncrypted via réflexion
            var ibanEncryptedField = wallet.getClass().getDeclaredField("ibanEncrypted");
            ibanEncryptedField.setAccessible(true);
            String encryptedIban = (String) ibanEncryptedField.get(wallet);

            return ibanSecurity.decrypt(encryptedIban);

        } catch (Exception e) {
            System.err.println("❌ Erreur décryptage IBAN: " + e.getMessage());
            return "IBAN non disponible";
        }
    }

    /**
     * ⭐ DÉCRYPTE L'EMAIL UTILISATEUR
     */
    private String decryptUserEmail(UserEntity user) {
        try {
            var userService = CDI.current()
                    .select(io.granix.user.service.UserService.class)
                    .get();
            return userService.decryptEmail(user);
        } catch (Exception e) {
            return "email@non-disponible.mg";
        }
    }

    /**
     * ⭐ DÉCRYPTE LE TÉLÉPHONE UTILISATEUR
     */
    private String decryptUserPhone(UserEntity user) {
        try {
            var userService = CDI.current()
                    .select(io.granix.user.service.UserService.class)
                    .get();
            return userService.decryptPhone(user);
        } catch (Exception e) {
            return "+261 XXX XXX XXX";
        }
    }

    /**
     * ⭐ NOM COMPLET UTILISATEUR
     */
    private String getUserFullName(UserEntity user) {
        if (user.firstname != null && user.lastname != null) {
            return user.firstname + " " + user.lastname;
        } else if (user.firstname != null) {
            return user.firstname;
        } else {
            return "Utilisateur Payza";
        }
    }

    /**
     * ⭐ CONSTRUIT LE CORPS DE L'EMAIL AVEC IBAN
     */
    private String buildWelcomeEmailWithIban(UserInfo userInfo, WalletInfo walletInfo) {
        return String.format("""
            Bonjour %s,
            
            🎉 Félicitations ! Votre compte Payza a été créé avec succès.
            
            📋 VOS INFORMATIONS BANCAIRES :
            
            💳 POUR PAYZA :
            • Votre numéro : %s
            • Devise : %s
            
            🏦 POUR LES BANQUES :
            • Votre IBAN : %s
            
            💡 COMMENT UTILISER :
            • Dans l'application Payza : utilisez votre numéro de téléphone
            • Pour recevoir des virements : donnez votre IBAN
            • Votre Wallet est sécurisé et accessible 24h/24
            
            🔒 CONSEILS DE SÉCURITÉ :
            • Ne partagez jamais vos codes confidentiels
            • Vérifiez toujours l'expéditeur avant un virement
            • Activez la double authentification
            
            📞 ASSISTANCE :
            Si vous avez des questions, contactez-nous à support@payza.mg
            
            Bienvenue dans la famille Payza !
            
            Cordialement,
            L'équipe Payza
            """,
                userInfo.fullName(),
                userInfo.phoneNumber(),
                walletInfo.currency(),
                formatIbanForDisplay(walletInfo.iban())
        );
    }

    /**
     * ⭐ CONSTRUIT LE CORPS DE L'EMAIL SANS IBAN
     */
    private String buildWelcomeEmailWithoutIban(UserInfo userInfo) {
        return String.format("""
            Bonjour %s,
            
            🎉 Félicitations ! Votre compte Payza a été créé avec succès.
            
            📋 VOS INFORMATIONS :
            
            💳 POUR PAYZA :
            • Votre numéro : %s
            
            💡 COMMENT UTILISER :
            • Dans l'application Payza : utilisez votre numéro de téléphone
            • Votre wallet bancaire sera créé automatiquement sous peu
            • Vous recevrez vos informations bancaires par email
            
            🔒 CONSEILS DE SÉCURITÉ :
            • Ne partagez jamais vos codes confidentiels
            • Vérifiez toujours l'expéditeur avant un virement
            
            📞 ASSISTANCE :
            Si vous avez des questions, contactez-nous à support@payza.mg
            
            Bienvenue dans la famille Payza !
            
            Cordialement,
            L'équipe Payza
            """,
                userInfo.fullName(),
                userInfo.phoneNumber()
        );
    }

    /**
     * ⭐ FORMATE L'IBAN POUR L'AFFICHAGE
     */
    private String formatIbanForDisplay(String iban) {
        if (iban == null || iban.length() < 4) return iban;
        return iban.replaceAll("(.{4})", "$1 ").trim();
    }
}