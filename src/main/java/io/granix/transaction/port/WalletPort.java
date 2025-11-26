package io.granix.transaction.port;

import io.granix.wallet.entity.WalletEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletPort {
    WalletEntity getWalletById(UUID walletId);
    WalletEntity getWalletByUserId(UUID userId, UUID walletId);
    List<WalletEntity> searchByUser(UUID userId);
    BigDecimal getBalance(UUID walletId);
    void debit(UUID walletId, BigDecimal amount);
    void credit(UUID walletId, BigDecimal amount);
}
