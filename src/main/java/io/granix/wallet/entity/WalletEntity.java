package io.granix.wallet.entity;

import io.granix.wallet.entity.utils.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pz_wallet")
public class WalletEntity extends PanacheEntityBase {

    // Identity
    @Id
    public UUID id;

    @Column(nullable = false, unique = true)
    public String ibanEncrypted;

    @Column(nullable = false)
    public UUID ownerId;

    // Balance and limits
    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    public BigDecimal dailyLimit;

    @Column(precision = 19, scale = 2)
    public BigDecimal monthlyLimit;

    // Currency configurations
    @Column(nullable = false)
    public String currencyCode; // MGA, USD, ...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CurrencyType currencyType;

    public Currency getCurrency() {
        return switch (currencyType) {
            case CurrencyType.FIAT -> BankCurrency.valueOf(currencyCode);
            case CurrencyType.CRYPTO -> CryptoCurrency.valueOf(currencyCode);
        };
    }

    // Status and security
    @Enumerated(EnumType.STRING)
    public WalletStatus status = WalletStatus.PENDING_VERIFICATION;

    @Column(nullable = false)
    public boolean isActive = true;

    @Column(nullable = false)
    public boolean isFrozen = false;

    // Audit
    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    public LocalDateTime lastTransactionAt;
}
