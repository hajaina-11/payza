package io.granix.transaction.service;

import io.granix.common.security.CryptoService;
import io.granix.transaction.database.entity.TransactionEntity;
import io.granix.transaction.database.entity.utils.CurrencyType;
import io.granix.transaction.database.entity.utils.TransactionStatus;
import io.granix.transaction.repository.TransactionRepository;
import io.granix.transaction.port.WalletPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApplicationScoped
public class TransactionProcessor {

    @Inject
    TransactionRepository repository;

    @Inject
    WalletPort walletPort;

    @Inject
    CryptoService cryptoService;

    /**
     * Persiste la transaction, effectue le débit/crédit, et met à jour le statut.
     * En cas d’erreur, un rollback complet est effectué grâce à @Transactional.
     */
    @Transactional
    public TransactionEntity processAndPersist(TransactionEntity tx) {
        try {
            // ⚠️ Assurer que referenceEncrypted n'est jamais null
            if (tx.referenceEncrypted == null || tx.referenceEncrypted.trim().isEmpty()) {
                tx.referenceEncrypted = cryptoService.encrypt("TXN" + System.currentTimeMillis());
            }
            // 1️⃣ Définir le statut initial
            tx.status = TransactionStatus.PENDING;
            tx.createdAt = LocalDateTime.now();

            // 2️⃣ Persister la transaction initiale
            repository.persist(tx);
            repository.flush();

            // 3️⃣ Vérification des soldes avant opération
            var fromWallet = walletPort.getWalletById(tx.fromWalletId);
            if (fromWallet.balance.compareTo(tx.amount.add(tx.fee != null ? tx.fee : BigDecimal.ZERO)) < 0) {
                tx.status = TransactionStatus.FAILED;
                tx.failureReason = "Solde insuffisant";
                repository.persist(tx);
                repository.flush();
                return tx;
            }

            // 4️⃣ Marquer comme en cours
            tx.status = TransactionStatus.PROCESSING;
            repository.persist(tx);
            repository.flush();

            // 5️⃣ Appliquer les opérations de débit/crédit
            BigDecimal totalDebit = tx.amount.add(tx.fee != null ? tx.fee : BigDecimal.ZERO);
            walletPort.debit(tx.fromWalletId, totalDebit);

            BigDecimal creditAmount = tx.convertedAmount != null ? tx.convertedAmount : tx.amount;
            walletPort.credit(tx.toWalletId, creditAmount);

            // 6️⃣ Si crypto, simuler un hash blockchain
            if (tx.fromCurrencyType == CurrencyType.CRYPTO || tx.toCurrencyType == CurrencyType.CRYPTO) {
                String randomHex = java.util.UUID.randomUUID().toString().replace("-", "")
                        + java.util.UUID.randomUUID().toString().replace("-", ""); // 32 + 32 = 64
                String txHash = "0x" + randomHex;
                tx.blockchainTxHashEncrypted = cryptoService.encrypt(txHash);
                tx.blockchainConfirmations = 3;
            }

            // 7️⃣ Marquer comme terminée
            tx.status = TransactionStatus.COMPLETED;
            tx.processedAt = LocalDateTime.now();
            repository.persist(tx);
            repository.flush();

            return tx;

        } catch (Exception e) {
            // 8️⃣ Gestion d'erreur et rollback logique
            try {
                tx.status = TransactionStatus.FAILED;
                tx.failureReason = e.getMessage();
                repository.persist(tx);
                repository.flush();
            } catch (Exception ex) {
                // Ignorer si rollback automatique
            }
            throw new RuntimeException("Échec du traitement: " + e.getMessage(), e);
        }
    }
}
