package io.granix.notification.interfaces.dto;

import io.granix.notification.domain.model.NotificationChannel;
import io.granix.notification.domain.model.NotificationStatus;
import io.granix.notification.domain.model.NotificationType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class NotificationResponse {
    public UUID id;
    public UUID userId;
    public NotificationType type;
    public NotificationChannel channel;
    public String title;
    public String body;
    public NotificationStatus status;
    public Map<String, String> metadata;
    public LocalDateTime createdAt;
    public LocalDateTime sentAt;
    public LocalDateTime deliveredAt;

    public NotificationResponse() {}

    public NotificationResponse(UUID id, UUID userId, NotificationType type,
                                NotificationChannel channel, String title, String body,
                                NotificationStatus status, Map<String, String> metadata,
                                LocalDateTime createdAt, LocalDateTime sentAt,
                                LocalDateTime deliveredAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.body = body;
        this.status = status;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.deliveredAt = deliveredAt;
    }
}