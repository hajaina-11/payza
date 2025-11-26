package io.granix.notification.infrastructure.config;

import io.granix.notification.domain.model.NotificationChannel;
import io.granix.notification.domain.model.NotificationType;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class NotificationConfig {

    @ConfigProperty(name = "payza.notification.sms.enabled", defaultValue = "true")
    boolean smsEnabled;

    @ConfigProperty(name = "payza.notification.email.enabled", defaultValue = "false")
    boolean emailEnabled;

    @ConfigProperty(name = "payza.notification.push.enabled", defaultValue = "false")
    boolean pushEnabled;

    private final Map<NotificationType, Set<NotificationChannel>> channelMapping = Map.of(
            NotificationType.USER_WELCOME, Set.of(NotificationChannel.SMS, NotificationChannel.EMAIL),
            NotificationType.WALLET_CREATED, Set.of(NotificationChannel.SMS),
            NotificationType.SECURITY_ALERT, Set.of(NotificationChannel.SMS, NotificationChannel.PUSH, NotificationChannel.EMAIL),
            NotificationType.LOGIN_ATTEMPT, Set.of(NotificationChannel.PUSH)
    );

    public Set<NotificationChannel> getAllowedChannels(NotificationType type) {
        return channelMapping.getOrDefault(type, Set.of(NotificationChannel.SMS));
    }

    public boolean isChannelEnabled(NotificationChannel channel) {
        return switch (channel) {
            case SMS -> smsEnabled;
            case EMAIL -> emailEnabled;
            case PUSH -> pushEnabled;
            case IN_APP -> true;
        };
    }
}