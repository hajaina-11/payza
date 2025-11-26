package io.granix.transaction.repository;

import io.granix.transaction.database.entity.TransactionEntity;
import io.granix.transaction.database.entity.utils.TransactionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<TransactionEntity> {

    public TransactionEntity findById(UUID id) {
        return find("id", id).firstResult();
    }

    public List<TransactionEntity> findByWalletIds(List<UUID> walletIds, int page, int size) {
        return find("fromWalletId in ?1 or toWalletId in ?1", walletIds)
                .page(page, size)
                .list();
    }

    public List<TransactionEntity> findByWalletId(UUID walletId, int page, int size) {
        return find("fromWalletId = ?1 or toWalletId = ?1", walletId)
                .page(page, size)
                .list();
    }

    public List<TransactionEntity> findByStatus(TransactionStatus status, int page, int size) {
        return find("status", status)
                .page(page, size)
                .list();
    }

    public List<TransactionEntity> findByUserWallets(List<UUID> walletIds) {
        return find("fromWalletId in ?1 or toWalletId in ?1", walletIds).list();
    }

    public List<TransactionEntity> findRecentByWalletId(UUID walletId, int limit) {
        return find("fromWalletId = ?1 or toWalletId = ?1 order by createdAt desc", walletId)
                .page(0, limit)
                .list();
    }
}
