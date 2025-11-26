package io.granix.transaction.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface ExternalPaymentGateway {
    /**
     * Simule un dépôt externe (bank/mobilemoney).
     * @param walletId destination
     * @param amount montant
     * @param currency devise
     * @param requestId idempotence
     * @param outcome mode de simulation (success|failure|insufficient|delay)
     * @return external reference (ex: SIMBANK-...)
     * @throws Exception en cas d'échec
     */
    String deposit(UUID walletId, BigDecimal amount, String currency, String requestId, String outcome) throws Exception;

    /**
     * Simule un retrait/payout vers compte externe.
     */
    String withdraw(UUID walletId, BigDecimal amount, String currency, String requestId, String outcome) throws Exception;
}
