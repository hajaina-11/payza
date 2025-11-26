package io.granix.wallet.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceResponse {
    public UUID walletId;
    public BigDecimal balance;
    public BigDecimal availableBalance;
    public String currency;
    public UUID ownerId;

    public BalanceResponse(UUID walletId, BigDecimal balance, BigDecimal availableBalance, String currency, UUID ownerId) {
        this.walletId = walletId;
        this.balance = balance;
        this.availableBalance = availableBalance;
        this.currency = currency;
        this.ownerId = ownerId;
    }
}
