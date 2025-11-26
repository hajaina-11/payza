package io.granix.transaction;

import io.granix.transaction.service.TransactionService;
import io.granix.transaction.database.entity.TransactionEntity;
import io.granix.transaction.database.entity.utils.TransactionStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.UUID;

@QuarkusTest
public class TransactionServiceIntegrationTest {

    @Inject
    TransactionService transactionService;

    @Test
    public void testDepositSuccessAndIdempotence() {
        UUID walletId = UUID.randomUUID();
        String requestId = UUID.randomUUID().toString();

        // Premier dépôt
        TransactionEntity tx1 = transactionService.createDeposit(
                walletId,
                new BigDecimal("1000"),
                "MGA",
                requestId,
                "success"
        );

        Assertions.assertNotNull(tx1, "La transaction ne doit pas être null");
        Assertions.assertEquals(TransactionStatus.COMPLETED, tx1.status, "Le statut doit être COMPLETED");
        Assertions.assertNotNull(tx1.referenceEncrypted, "La référence ne doit pas être null");

        // Test d'idempotence : même requestId doit retourner la même transaction
        TransactionEntity tx2 = transactionService.createDeposit(
                walletId,
                new BigDecimal("1000"),
                "MGA",
                requestId,
                "success"
        );

        Assertions.assertNotNull(tx2, "La transaction idempotente ne doit pas être null");
        Assertions.assertEquals(tx1.id, tx2.id, "Les IDs doivent être identiques (idempotence)");
        Assertions.assertEquals(tx1.referenceEncrypted, tx2.referenceEncrypted,
                "Les références doivent être identiques (idempotence)");
    }

    @Test
    public void testWithdrawalInsufficient() {
        UUID walletId = UUID.randomUUID();
        String requestId = UUID.randomUUID().toString();

        // Tentative de retrait avec simulation d'insuffisance de fonds
        TransactionEntity tx = transactionService.createWithdrawal(
                walletId,
                new BigDecimal("5000"),
                "MGA",
                requestId,
                "insufficient"
        );

        Assertions.assertNotNull(tx, "La transaction ne doit pas être null");
        Assertions.assertEquals(TransactionStatus.FAILED, tx.status,
                "Le statut doit être FAILED pour fonds insuffisants");
        Assertions.assertNotNull(tx.failureReason, "La raison de l'échec doit être présente");
        Assertions.assertTrue(tx.failureReason.toLowerCase().contains("insufficient"),
                "Le message d'erreur doit mentionner 'insufficient'");
    }

    @Test
    public void testDepositFailureSimulation() {
        UUID walletId = UUID.randomUUID();
        String requestId = UUID.randomUUID().toString();

        // Simulation d'échec de dépôt
        TransactionEntity tx = transactionService.createDeposit(
                walletId,
                new BigDecimal("2000"),
                "MGA",
                requestId,
                "failure"
        );

        Assertions.assertNotNull(tx, "La transaction ne doit pas être null");
        Assertions.assertEquals(TransactionStatus.FAILED, tx.status,
                "Le statut doit être FAILED pour une simulation d'échec");
        Assertions.assertNotNull(tx.failureReason, "La raison de l'échec doit être présente");
    }

    @Test
    public void testWithdrawalSuccess() {
        UUID walletId = UUID.randomUUID();
        String requestId = UUID.randomUUID().toString();

        // Retrait réussi
        TransactionEntity tx = transactionService.createWithdrawal(
                walletId,
                new BigDecimal("500"),
                "MGA",
                requestId,
                "success"
        );

        Assertions.assertNotNull(tx, "La transaction ne doit pas être null");
        Assertions.assertEquals(TransactionStatus.COMPLETED, tx.status,
                "Le statut doit être COMPLETED");
        Assertions.assertNotNull(tx.referenceEncrypted, "La référence ne doit pas être null");
        Assertions.assertTrue(tx.referenceEncrypted.startsWith("SIMPAYOUT-") ||
                        !tx.referenceEncrypted.isEmpty(),
                "La référence doit être présente");
    }

    @Test
    public void testMultipleDepositsWithDifferentRequestIds() {
        UUID walletId = UUID.randomUUID();

        // Premier dépôt
        String requestId1 = UUID.randomUUID().toString();
        TransactionEntity tx1 = transactionService.createDeposit(
                walletId,
                new BigDecimal("1000"),
                "MGA",
                requestId1,
                "success"
        );

        // Deuxième dépôt avec un requestId différent
        String requestId2 = UUID.randomUUID().toString();
        TransactionEntity tx2 = transactionService.createDeposit(
                walletId,
                new BigDecimal("1500"),
                "MGA",
                requestId2,
                "success"
        );

        Assertions.assertNotNull(tx1, "Première transaction non null");
        Assertions.assertNotNull(tx2, "Deuxième transaction non null");
        Assertions.assertNotEquals(tx1.id, tx2.id, "Les transactions doivent avoir des IDs différents");
        Assertions.assertEquals(TransactionStatus.COMPLETED, tx1.status);
        Assertions.assertEquals(TransactionStatus.COMPLETED, tx2.status);
    }

    @Test
    public void testDepositWithDelay() {
        UUID walletId = UUID.randomUUID();
        String requestId = UUID.randomUUID().toString();

        long startTime = System.currentTimeMillis();

        // Simulation avec délai
        TransactionEntity tx = transactionService.createDeposit(
                walletId,
                new BigDecimal("3000"),
                "MGA",
                requestId,
                "delay"
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        Assertions.assertNotNull(tx, "La transaction ne doit pas être null");
        Assertions.assertTrue(duration >= 4000,
                "La transaction avec délai devrait prendre au moins 4 secondes");
    }
}

// FICHIER : src/test/java/io/granix/transaction/TransactionServiceIntegrationTest.java