package io.granix.wallet.repository;

import io.granix.wallet.entity.WalletEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletRepository implements PanacheRepository<WalletEntity> {

    public WalletEntity findByWalletId(UUID walletId) {
        return find("id", walletId).firstResult();
    }
    public WalletEntity findByUserIdAndWalletId(UUID userId, UUID walletId) {
        return find("ownerId = ?1 and id = ?2", userId, walletId).firstResult();
    }

    public List<WalletEntity> findByUserId(UUID userId) {
        return list("ownerId", userId);
    }
}
