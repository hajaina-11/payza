package io.granix.transaction.database.entity;

import io.granix.transaction.database.entity.utils.CurrencyType;
import io.granix.transaction.database.entity.utils.TransactionStatus;
import io.granix.transaction.database.entity.utils.TransactionType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pz_transaction")
public class TransactionEntity extends PanacheEntityBase {


    @Id
    @Column(name = "id")
    public UUID id;

    // ⭐⭐ TOUTES les colonnes AVEC LES NOMS EXACTS de la base ⭐⭐
    @Column(name = "fromwalletid", nullable = false)
    public UUID fromWalletId;

    @Column(name = "towalletid", nullable = false)
    public UUID toWalletId;

    @Column(name = "amount", precision = 19, scale = 8, nullable = false)
    public BigDecimal amount;

    @Column(name = "fee", precision = 19, scale = 8, nullable = false)
    public BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "currencycode", nullable = false)  // ⭐⭐ EXACTEMENT "currencycode" ⭐⭐
    public String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "referenceencrypted", nullable = false, length = 255)
    public String referenceEncrypted;

    @Column(name = "descriptionencrypted", length = 255)
    public String descriptionEncrypted;

    @Column(name = "createdat", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "processedat")
    public LocalDateTime processedAt;

    @Column(name = "failurereason", length = 255)
    public String failureReason;

    @Column(name = "metadataencrypted", length = 255)
    public String metadataEncrypted;

    @Column(name = "ipaddressencrypted", length = 255)
    public String ipAddressEncrypted;

    @Column(name = "deviceinfoencrypted", length = 255)
    public String deviceInfoEncrypted;

    @Column(name = "fromcurrencycode", nullable = false)
    public String fromCurrencyCode;

    @Column(name = "tocurrencycode", nullable = false)
    public String toCurrencyCode;

    @Column(name = "exchangerate", precision = 19, scale = 8)
    public BigDecimal exchangeRate;

    @Column(name = "convertedamount", precision = 19, scale = 8)
    public BigDecimal convertedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "fromcurrencytype", nullable = false)
    public CurrencyType fromCurrencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tocurrencytype", nullable = false)
    public CurrencyType toCurrencyType;

    @Column(name = "blockchaintxhashencrypted", length = 255)
    public String blockchainTxHashEncrypted;

    @Column(name = "blockchainconfirmations")
    public Integer blockchainConfirmations;

    @Column(name = "cryptowalletaddressencrypted", length = 255)
    public String cryptoWalletAddressEncrypted;

    @Column(name = "requestid", length = 100)
    public String requestId;


    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}