package io.granix.notification.interfaces.dto;

import io.granix.notification.domain.model.NotificationChannel;
import io.granix.notification.domain.model.NotificationType;
import java.util.Map;
import java.util.UUID;

public class SendNotificationRequest {
    public UUID userId;
    public NotificationType type;
    public NotificationChannel channel;
    public String title;
    public String message;
    public Map<String, String> metadata;

    public SendNotificationRequest() {}

    public SendNotificationRequest(UUID userId, NotificationType type,
                                   NotificationChannel channel, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
    }
}