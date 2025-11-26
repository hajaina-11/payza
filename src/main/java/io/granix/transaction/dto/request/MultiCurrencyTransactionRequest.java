package io.granix.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class MultiCurrencyTransactionRequest {
    @JsonProperty(value = "from_wallet_id", required = true)
    public UUID fromWalletId;

    @JsonProperty(value = "to_wallet_id", required = true)
    public UUID toWalletId;

    @JsonProperty(required = true)
    public BigDecimal amount;

    @JsonProperty(value = "target_currency")
    public String targetCurrency;

    public String description;

    @JsonProperty(value = "ip_address")
    public String ipAddress;

    @JsonProperty(value = "device_info")
    public String deviceInfo;

    public Map<String, Object> metadata;

    @JsonProperty(value = "crypto_wallet_address")
    public String cryptoWalletAddress;

    @JsonProperty(value = "network_fee_level")
    public String networkFeeLevel;
}
