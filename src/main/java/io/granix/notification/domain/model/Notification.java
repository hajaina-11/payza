package io.granix.notification.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Notification {
    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final NotificationChannel channel;
    private final String title;
    private final String body;
    private final String template;
    private final NotificationStatus status;
    private final Map<String, String> metadata;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Notification(UUID id, UUID userId, NotificationType type,
                        NotificationChannel channel, String title, String body, String template,
                        NotificationStatus status, Map<String, String> metadata,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.type = Objects.requireNonNull(type);
        this.channel = Objects.requireNonNull(channel);
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
        this.template = Objects.requireNonNull(template);
        this.status = Objects.requireNonNull(status);
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Notification create(UUID userId, NotificationType type,
                                      NotificationChannel channel, String title, String body) {
        return new Notification(
                UUID.randomUUID(),
                userId,
                type,
                channel,
                title,
                body,
                "default",
                NotificationStatus.PENDING,
                new HashMap<>(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public Notification markAsProcessing() {
        return new Notification(
                id, userId, type, channel, title, body, template,
                NotificationStatus.PROCESSING,
                metadata,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Notification markAsSent() {
        Map<String, String> newMetadata = new HashMap<>(metadata);
        newMetadata.put("sentAt", LocalDateTime.now().toString());

        return new Notification(
                id, userId, type, channel, title, body, template,
                NotificationStatus.SENT,
                newMetadata,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Notification markAsFailed(String errorMessage) {
        Map<String, String> newMetadata = new HashMap<>(metadata);
        newMetadata.put("error", errorMessage);
        newMetadata.put("failedAt", LocalDateTime.now().toString());

        int retryCount = Integer.parseInt(metadata.getOrDefault("retryCount", "0"));
        newMetadata.put("retryCount", String.valueOf(retryCount + 1));

        return new Notification(
                id, userId, type, channel, title, body, template,
                NotificationStatus.FAILED,
                newMetadata,
                createdAt,
                LocalDateTime.now()
        );
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public NotificationChannel getChannel() { return channel; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getTemplate() { return template; }
    public NotificationStatus getStatus() { return status; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return String.format("Notification[%s, %s, %s, %s]", id, type, channel, status);
    }
}