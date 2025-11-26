package io.granix.transaction.dto.response;

import io.granix.transaction.database.entity.utils.CurrencyType;
import io.granix.transaction.database.entity.utils.TransactionStatus;
import io.granix.transaction.database.entity.utils.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionResponse {
    public UUID id;
    public UUID fromWalletId;
    public UUID toWalletId;
    public BigDecimal amount;
    public BigDecimal convertedAmount;
    public BigDecimal fee;
    public String currencyCode;
    public String fromCurrencyCode;
    public String toCurrencyCode;
    public BigDecimal exchangeRate;
    public TransactionType type;
    public TransactionStatus status;
    public String reference;
    public String description;
    public LocalDateTime createdAt;
    public LocalDateTime processedAt;
    public String failureReason;
    public CurrencyType fromCurrencyType;
    public CurrencyType toCurrencyType;
    public String blockchainTxHash;
    public Integer blockchainConfirmations;

    public TransactionResponse() {}

    public TransactionResponse(UUID id, UUID fromWalletId, UUID toWalletId,
                               BigDecimal amount, BigDecimal convertedAmount, BigDecimal fee,
                               String currencyCode, String fromCurrencyCode, String toCurrencyCode,
                               BigDecimal exchangeRate, TransactionType type, TransactionStatus status,
                               String reference, String description, LocalDateTime createdAt,
                               LocalDateTime processedAt, String failureReason,
                               CurrencyType fromCurrencyType, CurrencyType toCurrencyType,
                               String blockchainTxHash, Integer blockchainConfirmations) {
        this.id = id;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.fee = fee;
        this.currencyCode = currencyCode;
        this.fromCurrencyCode = fromCurrencyCode;
        this.toCurrencyCode = toCurrencyCode;
        this.exchangeRate = exchangeRate;
        this.type = type;
        this.status = status;
        this.reference = reference;
        this.description = description;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.failureReason = failureReason;
        this.fromCurrencyType = fromCurrencyType;
        this.toCurrencyType = toCurrencyType;
        this.blockchainTxHash = blockchainTxHash;
        this.blockchainConfirmations = blockchainConfirmations;
    }
}
