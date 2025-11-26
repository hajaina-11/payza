package io.granix.transaction.resource;

import io.granix.transaction.database.entity.TransactionEntity;
import io.granix.transaction.service.TransactionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/transactions/external")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DepositWithdrawalResource {

    @Inject
    TransactionService transactionService;

    @Inject
    JsonWebToken jwt;

    /**
     * 💰 ENDPOINT DÉPÔT - Simuler un dépôt bancaire
     * POST /transactions/external/deposit
     */
    @POST
    @Path("/deposit")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> createDeposit(DepositRequest request) {
        var response = new HashMap<String, Object>();

        try {
            System.out.println("📥 DÉBUT DÉPÔT");
            System.out.println("   Wallet: " + request.walletId);
            System.out.println("   Montant: " + request.amount + " " + request.currency);
            System.out.println("   Outcome: " + request.outcome);

            // Générer un requestId unique pour l'idempotence
            String requestId = request.requestId != null
                    ? request.requestId
                    : UUID.randomUUID().toString();

            // Appeler le service
            TransactionEntity transaction = transactionService.createDeposit(
                    request.walletId,
                    request.amount,
                    request.currency,
                    requestId,
                    request.outcome != null ? request.outcome : "success"
            );

            System.out.println("✅ DÉPÔT CRÉÉ");
            System.out.println("   ID Transaction: " + transaction.id);
            System.out.println("   Statut: " + transaction.status);
            System.out.println("   Frais: " + transaction.fee);

            // Construire la réponse
            response.put("status", "success");
            response.put("message", "Dépôt effectué avec succès");
            response.put("transaction", Map.of(
                    "id", transaction.id,
                    "walletId", transaction.fromWalletId,
                    "amount", transaction.amount,
                    "fee", transaction.fee,
                    "currency", transaction.currencyCode,
                    "status", transaction.status.name(),
                    "type", transaction.type.name(),
                    "createdAt", transaction.createdAt.toString(),
                    "processedAt", transaction.processedAt != null ? transaction.processedAt.toString() : null,
                    "requestId", transaction.requestId
            ));

            return response;

        } catch (Exception e) {
            System.err.println("❌ ERREUR DÉPÔT: " + e.getMessage());
            e.printStackTrace();

            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 💸 ENDPOINT RETRAIT - Simuler un retrait bancaire
     * POST /transactions/external/withdrawal
     */
    @POST
    @Path("/withdrawal")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> createWithdrawal(WithdrawalRequest request) {
        var response = new HashMap<String, Object>();

        try {
            System.out.println("📤 DÉBUT RETRAIT");
            System.out.println("   Wallet: " + request.walletId);
            System.out.println("   Montant: " + request.amount + " " + request.currency);
            System.out.println("   Outcome: " + request.outcome);

            // Générer un requestId unique pour l'idempotence
            String requestId = request.requestId != null
                    ? request.requestId
                    : UUID.randomUUID().toString();

            // Appeler le service
            TransactionEntity transaction = transactionService.createWithdrawal(
                    request.walletId,
                    request.amount,
                    request.currency,
                    requestId,
                    request.outcome != null ? request.outcome : "success"
            );

            System.out.println("✅ RETRAIT CRÉÉ");
            System.out.println("   ID Transaction: " + transaction.id);
            System.out.println("   Statut: " + transaction.status);
            System.out.println("   Frais: " + transaction.fee);

            // Construire la réponse
            response.put("status", "success");
            response.put("message", "Retrait effectué avec succès");
            response.put("transaction", Map.of(
                    "id", transaction.id,
                    "walletId", transaction.fromWalletId,
                    "amount", transaction.amount,
                    "fee", transaction.fee,
                    "currency", transaction.currencyCode,
                    "status", transaction.status.name(),
                    "type", transaction.type.name(),
                    "createdAt", transaction.createdAt.toString(),
                    "processedAt", transaction.processedAt != null ? transaction.processedAt.toString() : null,
                    "requestId", transaction.requestId
            ));

            return response;

        } catch (Exception e) {
            System.err.println("❌ ERREUR RETRAIT: " + e.getMessage());
            e.printStackTrace();

            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 📊 ENDPOINT HISTORIQUE - Voir toutes les transactions d'un wallet
     * GET /transactions/external/history/{walletId}
     */
    @GET
    @Path("/history/{walletId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> getTransactionHistory(@PathParam("walletId") UUID walletId) {
        var response = new HashMap<String, Object>();

        try {
            var transactions = transactionService.getWalletTransactions(walletId);

            response.put("status", "success");
            response.put("walletId", walletId);
            response.put("count", transactions.size());
            response.put("transactions", transactions);

            return response;

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    // ==================== DTOs ====================

    public static class DepositRequest {
        public UUID walletId;
        public BigDecimal amount;
        public String currency = "MGA";
        public String outcome = "success"; // success, failure, insufficient, delay
        public String requestId; // Pour l'idempotence (optionnel)
    }

    public static class WithdrawalRequest {
        public UUID walletId;
        public BigDecimal amount;
        public String currency = "MGA";
        public String outcome = "success"; // success, failure, insufficient, delay
        public String requestId; // Pour l'idempotence (optionnel)
    }
}

// FICHIER : src/main/java/io/granix/transaction/resource/DepositWithdrawalResource.java