package io.granix.notification.infrastructure.persistence;

import io.granix.notification.domain.model.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "pz_notification")
public class NotificationEntity extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(nullable = false, name = "user_id")
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    public NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public NotificationChannel channel;

    @Column(nullable = false, length = 200)
    public String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String body;

    @Column(length = 100)
    public String template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public NotificationStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, String> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;
    public LocalDateTime sentAt;
    public LocalDateTime deliveredAt;

    public NotificationEntity() {}

    public NotificationEntity(Notification notification) {
        this.id = notification.getId();
        this.userId = notification.getUserId();
        this.type = notification.getType();
        this.channel = notification.getChannel();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.template = notification.getTemplate();
        this.status = notification.getStatus();
        this.metadata = new HashMap<>(notification.getMetadata());
        this.createdAt = notification.getCreatedAt();
        this.updatedAt = notification.getUpdatedAt();
    }

    public Notification toDomain() {
        return new Notification(
                this.id,
                this.userId,
                this.type,
                this.channel,
                this.title,
                this.body,
                this.template,
                this.status,
                this.metadata,
                this.createdAt,
                this.updatedAt != null ? this.updatedAt : this.createdAt
        );
    }
}