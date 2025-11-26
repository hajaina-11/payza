package io.granix.transaction.database.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_requests")
public class ExternalRequestEntity extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(name = "request_id", unique = true, nullable = false, length = 100)
    public String requestId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "status")
    public String status;

    @Column(name = "external_reference")
    public String externalReference;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
