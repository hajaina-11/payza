package io.granix.user.resource;

import io.granix.user.service.UserService;
import io.granix.user.dto.request.UserAuthenticationRequest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import jakarta.ws.rs.core.HttpHeaders;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/user")
public class AuthenticationResource {

    @Inject
    UserService service;

    @POST
    @Path("/auth/token")
    public Map<String, Object> authenticationToken(UserAuthenticationRequest request, @Context HttpHeaders headers)
    {
        var response = new HashMap<String, Object>();
        try {


            var user = service.authenticate(
                    request.msisdn,
                    request.password
            );

            System.out.println("✅ Connexion réussie pour: " + user.firstname);

            triggerLoginNotification(user, headers);

            var groups = user.roles.stream()
                    .map(r -> r.name.name()) // Transforme enum -> String (e.g. "ADMIN")
                    .collect(Collectors.toSet());

            String token = Jwt.issuer("granix-payza")
                    .upn(service.decryptPhone(user))
                    .groups(groups)
                    .claim("userId", user.id.toString())
                    .expiresIn(3600)
                    .sign();

            response.put("token", token);
            response.put("expiration", 3600);
            response.put("user_id", user.id);
            response.put("status", "success");
            response.put("message", "Connexion réussie");


            return response;
        } catch (Exception e) {
            System.err.println("❌ Connexion échouée: " + e.getMessage());

            // ⭐ NOTIFICATION D'ÉCHEC
            triggerFailedLoginNotification(request.msisdn, e.getMessage(), headers);

            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * ⭐ MÉTHODE POUR DÉCLENCHER LA NOTIFICATION MANUELLEMENT
     */
    private void triggerLoginNotification(io.granix.user.database.entity.UserEntity user, HttpHeaders headers) {
        try {
            // Récupération SIMPLIFIÉE des infos
            String ipAddress = "196.168.1.100"; // IP fixe pour Madagascar
            String userAgent = "Payza-App";     // User-Agent fixe

            System.out.println("🔔 Déclenchement notification de sécurité");

            // Injection manuelle du NotificationOrchestrator
            var orchestrator = jakarta.enterprise.inject.spi.CDI.current()
                    .select(io.granix.notification.application.service.NotificationOrchestrator.class)
                    .get();

            // Création de la notification
            var notification = io.granix.notification.domain.model.Notification.create(
                    user.id,
                    io.granix.notification.domain.model.NotificationType.LOGIN_ATTEMPT,
                    io.granix.notification.domain.model.NotificationChannel.EMAIL,
                    "✅ Connexion réussie à votre compte Payza",
                    buildLoginMessage(user.firstname, ipAddress)
            );

            // Envoi de la notification
            orchestrator.processNotification(notification);

            System.out.println("📧 Notification de sécurité envoyée à: " + user.firstname);

        } catch (Exception e) {
            System.err.println("⚠️ Erreur notification: " + e.getMessage());
            // Ne pas bloquer l'authentification
        }
    }

    /**
     * ⭐ CONSTRUCTION DU MESSAGE
     */
    private String buildLoginMessage(String userName, String ipAddress) {
        return String.format("""
            Bonjour %s,
            
            Une connexion à votre compte Payza a été effectuée avec succès.
            
            📍 Localisation: Madagascar
            🌍 IP: %s
            📅 Date: %s
            
            Si vous n'êtes pas à l'origine de cette connexion, 
            veuillez contacter immédiatement le support.
            
            Cordialement,
            L'équipe Payza
            """,
                userName,
                ipAddress,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
        );
    }
    private void triggerFailedLoginNotification(String msisdn, String errorMessage, HttpHeaders headers) {
        try {
            System.out.println("🚨 Tentative échouée pour: " + msisdn);

            var orchestrator = jakarta.enterprise.inject.spi.CDI.current()
                    .select(io.granix.notification.application.service.NotificationOrchestrator.class)
                    .get();

            // ⭐ TROUVER SEULEMENT L'ID (PLUS SIMPLE)
            java.util.UUID userId = findUserIdByMsisdn(msisdn);

            if (userId != null) {
                var notification = io.granix.notification.domain.model.Notification.create(
                        userId,
                        io.granix.notification.domain.model.NotificationType.SECURITY_ALERT,
                        io.granix.notification.domain.model.NotificationChannel.EMAIL,
                        "🚨 Tentative de connexion échouée sur votre compte Payza",
                        buildGenericFailedLoginMessage(msisdn, errorMessage)
                );

                orchestrator.processNotification(notification);
                System.out.println("📧 Alerte de sécurité envoyée");
            } else {
                System.out.println("ℹ️  Aucun utilisateur trouvé - pas d'email envoyé pour: " + msisdn);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur notification échec: " + e.getMessage());
        }
    }

    private String buildGenericFailedLoginMessage(String msisdn, String errorMessage) {
        return String.format("""
        Cher client Payza,
        
        🚨 Une tentative de connexion échouée a été détectée sur votre compte.
        
        📞 Votre numéro: %s
        📅 Date: %s
        ❌ Raison: %s
        
        🔒 La tentative a été bloquée pour des raisons de sécurité.
        
        ✅ Si c'était vous :
        - Vérifiez votre mot de passe
        
        🚨 Si ce n'était pas vous :
        - Votre compte est sécurisé
        - Aucune action requise
        
        Pour toute question, contactez-nous à support@payza.mg
        
        Cordialement,
        L'équipe de sécurité Payza
        """,
                msisdn,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                errorMessage
        );
    }

    private java.util.UUID findUserIdByMsisdn(String msisdn) {
        try {

            java.util.List<io.granix.user.database.entity.UserEntity> allUsers =
                    io.granix.user.database.entity.UserEntity.listAll();

            System.out.println("🔍 Recherche parmi " + allUsers.size() + " utilisateurs");

            for (io.granix.user.database.entity.UserEntity user : allUsers) {
                try {

                    if (user.msisdnEncrypted == null || user.msisdnEncrypted.trim().isEmpty()) {
                        continue;
                    }

                    String decryptedMsisdn = service.decryptPhone(user);

                    if (msisdn.equals(decryptedMsisdn)) {
                        System.out.println("✅ ID utilisateur trouvé: " + user.id);  // ⭐ MAINTENANT user.id FONCTIONNE
                        return user.id;
                    }
                } catch (Exception e) {
                    System.out.println("⚠️  Erreur décryptage pour user " + user.id + ": " + e.getMessage());
                    continue;
                }
            }

            System.out.println("❌ Aucun utilisateur trouvé pour: " + msisdn);
            return null;

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche utilisateur: " + e.getMessage());
            return null;
        }
    }
    private String getSafeUserName(io.granix.user.database.entity.UserEntity user) {
        try {
            // Vérifier si les champs existent et ne sont pas null
            if (user.firstname != null && user.lastname != null) {
                return user.firstname + " " + user.lastname;
            } else if (user.firstname != null) {
                return user.firstname;
            } else {
                return "Utilisateur Payza";
            }
        } catch (Exception e) {
            return "Utilisateur Payza";
        }
    }

}
