package io.granix.notification.domain.service;

import io.granix.notification.domain.model.NotificationType;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Map;

@ApplicationScoped
public class NotificationTemplateEngine {

    @ConfigProperty(name = "payza.notification.template.welcome.sms",
            defaultValue = "🎉 Bienvenue sur Payza! Votre compte a été créé avec succès.")
    String welcomeSmsTemplate;

    private final Map<NotificationType, Map<String, String>> templateMapping = Map.of(
            NotificationType.USER_WELCOME, Map.of(
                    "SMS", welcomeSmsTemplate,
                    "EMAIL", "Bienvenue sur Payza! Votre compte a été créé avec succès."
            ),
            NotificationType.WALLET_CREATED, Map.of(
                    "SMS", "💰 Votre wallet est maintenant actif!"
            ),
            NotificationType.SECURITY_ALERT, Map.of(
                    "SMS", "🚨 Alerte sécurité: {message}"
            )
    );

    public String processBody(String body, NotificationType type, String channel) {
        String template = getTemplate(type, channel);
        if (template != null) {
            return template.replace("{message}", body);
        }
        return body;
    }

    public String getTemplate(NotificationType type, String channel) {
        Map<String, String> channelTemplates = templateMapping.get(type);
        return channelTemplates != null ? channelTemplates.get(channel) : null;
    }
}