package io.granix.wallet.service;

import io.granix.wallet.entity.WalletEntity;
import io.granix.wallet.repository.WalletRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletInformationService {
    @Inject
    WalletRepository repository;

    public List<WalletEntity> getAllWallet()
    {
        return repository.listAll();
    }

    public List<WalletEntity> searchByUser(UUID userId)
    {
        return repository.list("ownerId = ?1", userId);
    }

    public WalletEntity getWalletByUserId(UUID userId, UUID walletId) {
        var queryParameters = new HashMap<String, Object>();

        queryParameters.put("userId", userId);
        queryParameters.put("walletId", walletId);

        return repository.find("ownerId = :userId AND id = :walletId", queryParameters).firstResult();
    }

    // Ajouter pour le transaction

    public WalletEntity getWalletById(UUID walletId) {
        return repository.find("id", walletId).firstResult();
    }
}
