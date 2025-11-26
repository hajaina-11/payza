package io.granix.notification.infrastructure.persistence;

import io.granix.notification.domain.model.Notification;
import io.granix.notification.domain.repository.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationRepositoryImpl implements NotificationRepository {

    @Override
    @Transactional
    public Notification save(Notification notification) {
        NotificationEntity entity = new NotificationEntity(notification);
        entity.persist();
        return entity.toDomain();
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return NotificationEntity.findByIdOptional(id)
                .map(entity -> ((NotificationEntity) entity).toDomain());
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        return NotificationEntity.find("userId", userId).list()
                .stream()
                .map(entity -> ((NotificationEntity) entity).toDomain())
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByStatus(String status) {
        return NotificationEntity.find("status", status).list()
                .stream()
                .map(entity -> ((NotificationEntity) entity).toDomain())
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findPendingRetries() {
        return NotificationEntity.find("status = ?1", "FAILED").list()
                .stream()
                .map(entity -> ((NotificationEntity) entity).toDomain())
                .filter(notification -> {
                    String retryCount = notification.getMetadata().getOrDefault("retryCount", "0");
                    return Integer.parseInt(retryCount) < 3;
                })
                .collect(Collectors.toList());
    }
}
