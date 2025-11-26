package io.granix.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.granix.transaction.database.entity.utils.TransactionType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class TransactionRequest {
    @JsonProperty(value = "from_wallet_id", required = true)
    public UUID fromWalletId;

    @JsonProperty(value = "to_wallet_id", required = true)
    public UUID toWalletId;

    @JsonProperty(required = true)
    public BigDecimal amount;

    @JsonProperty(required = true)
    public String currencyCode;

    @JsonProperty(required = true)
    public TransactionType type;

    public String description;

    @JsonProperty(value = "ip_address")
    public String ipAddress;

    @JsonProperty(value = "device_info")
    public String deviceInfo;

    public Map<String, Object> metadata;
}
