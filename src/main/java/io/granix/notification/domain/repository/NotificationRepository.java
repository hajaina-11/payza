package io.granix.notification.domain.repository;

import io.granix.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    List<Notification> findByUserId(UUID userId);
    List<Notification> findByStatus(String status);
    List<Notification> findPendingRetries();
}