package io.granix.transaction.repository;

import io.granix.transaction.database.entity.ExternalRequestEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ExternalRequestRepository implements PanacheRepository<ExternalRequestEntity> {

    public Optional<ExternalRequestEntity> findByRequestId(String requestId) {
        return find("requestId", requestId).firstResultOptional();
    }
}
