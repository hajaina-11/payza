package io.granix.wallet.adapter;

import io.granix.transaction.port.WalletPort;
import io.granix.wallet.entity.WalletEntity;
import io.granix.wallet.repository.WalletRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletAdapter implements WalletPort {

    @Inject
    WalletRepository walletRepository;

    @Override
    public WalletEntity getWalletById(UUID walletId) {
        WalletEntity wallet = walletRepository.findByWalletId(walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet non trouvé : " + walletId);
        }
        return wallet;
    }

    @Override
    public WalletEntity getWalletByUserId(UUID userId, UUID walletId) {
        WalletEntity wallet = walletRepository.findByUserIdAndWalletId(userId, walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet non trouvé pour cet utilisateur : " + walletId);
        }
        return wallet;
    }

    @Override
    public List<WalletEntity> searchByUser(UUID userId) {
        return walletRepository.findByUserId(userId);
    }

    @Override
    public BigDecimal getBalance(UUID walletId) {
        WalletEntity wallet = walletRepository.findByWalletId(walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet introuvable : " + walletId);
        }
        return wallet.balance;
    }

    @Override
    @Transactional
    public void debit(UUID walletId, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findByWalletId(walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet introuvable : " + walletId);
        }
        if (wallet.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant dans le wallet " + walletId);
        }
        wallet.balance = wallet.balance.subtract(amount);
        //walletRepository.persist(wallet);
        walletRepository.flush();
    }

    @Override
    @Transactional
    public void credit(UUID walletId, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findByWalletId(walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet introuvable : " + walletId);
        }
        wallet.balance = wallet.balance.add(amount);
        //walletRepository.persist(wallet);
        walletRepository.flush();
    }
}
