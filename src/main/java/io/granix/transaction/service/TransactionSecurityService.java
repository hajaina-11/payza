package io.granix.transaction.service;

import io.granix.transaction.port.WalletPort;
import io.granix.wallet.entity.WalletEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionSecurityService {

    @Inject
    WalletPort walletPort;

    @ConfigProperty(name = "payza.transaction.limits.user.daily", defaultValue = "10000")
    BigDecimal userDailyLimit;

    @ConfigProperty(name = "payza.transaction.limits.user.monthly", defaultValue = "50000")
    BigDecimal userMonthlyLimit;

    /**
     * Vérifie que l'utilisateur est bien autorisé à utiliser le wallet.
     */
    public void validateTransactionPermissions(UUID walletId, UUID userId) {
        System.out.println("🔍 [DEBUG] Vérification accès wallet...");
        System.out.println("🔹 userId (du token) = " + userId);
        System.out.println("🔹 walletId (de la requête) = " + walletId);

        List<WalletEntity> userWallets = walletPort.searchByUser(userId);
        System.out.println("🔹 Wallets trouvés pour cet utilisateur = " + userWallets.size());

        boolean hasAccess = userWallets.stream().anyMatch(w -> w.id.equals(walletId));
        if (!hasAccess) {
            System.err.println("🚫 [SECURITY] L'utilisateur n'est pas propriétaire de ce wallet !");
            throw new SecurityException("Accès non autorisé à ce wallet");
        }

        var wallet = walletPort.getWalletByUserId(userId, walletId);
        if (wallet == null) {
            System.err.println("⚠️ [SECURITY] Wallet non trouvé pour cet utilisateur !");
            throw new SecurityException("Wallet non trouvé");
        }

        if (!wallet.isActive) {
            System.err.println("⚠️ [SECURITY] Wallet inactif !");
            throw new SecurityException("Le wallet n'est pas actif");
        }

        if (wallet.isFrozen) {
            System.err.println("⚠️ [SECURITY] Wallet gelé !");
            throw new SecurityException("Le wallet est gelé");
        }

        System.out.println("✅ [SECURITY] Accès au wallet autorisé.");
    }

    /**
     * Vérifie si l'utilisateur a accès à l'une des deux extrémités de la transaction.
     */
    public void validateTransactionAccess(UUID fromWalletId, UUID toWalletId, UUID userId) {
        List<WalletEntity> userWallets = walletPort.searchByUser(userId);
        boolean hasFrom = userWallets.stream().anyMatch(w -> w.id.equals(fromWalletId));
        boolean hasTo = userWallets.stream().anyMatch(w -> w.id.equals(toWalletId));
        if (!hasFrom && !hasTo) {
            System.err.println("🚫 [SECURITY] Aucun des wallets n'appartient à l'utilisateur !");
            throw new SecurityException("Accès non autorisé à cette transaction");
        }
    }

    /**
     * Vérifie les limites et le solde disponible avant transaction.
     */
    public void validateTransactionLimits(UUID walletId, BigDecimal amount) {
        BigDecimal balance = walletPort.getBalance(walletId);
        if (balance == null) {
            throw new SecurityException("Solde introuvable pour le wallet");
        }

        System.out.println("🔍 [LIMITS] Solde actuel = " + balance + " / Montant demandé = " + amount);

        if (amount.compareTo(balance) > 0) {
            throw new SecurityException("Solde insuffisant");
        }

        if (amount.compareTo(userDailyLimit) > 0) {
            throw new SecurityException("Limite quotidienne dépassée");
        }

        System.out.println("✅ [LIMITS] Vérifications de solde et limites OK");
    }
}
