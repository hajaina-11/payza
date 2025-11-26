package io.granix.wallet.service.iban.config;

import io.granix.wallet.entity.utils.BankCurrency;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.Map;

@ApplicationScoped
public enum Country {
    MADAGASCAR("MG", "Madagascar", 27, "001", Map.of(
            BankCurrency.MGA, "PAYZ"
    )),
    SOUTH_AFRICA("ZA", "South Africa", 25, "001", Map.of(
            BankCurrency.ZAR, "PAYZ"
    )),
    KENYA("KE", "Kenya", 25, "001", Map.of(
            BankCurrency.KES, "PAYZ"
    )),
    TANZANIA("TZ", "Tanzania", 25, "001", Map.of(
            BankCurrency.TZS, "PAYZ"
    )),
    SENEGAL("SN", "Senegal", 28, "001", Map.of(
            BankCurrency.XOF, "PAYZ"
    )),
    CAMEROON("CM", "Cameroon", 27, "001", Map.of(
            BankCurrency.XAF, "PAYZ"
    ));

    private final String isoCode;
    private final String name;
    private final int ibanLength;
    private final String defaultBranchCode;
    private final Map<BankCurrency, String> bankCodes;

    Country(String isoCode, String name, int ibanLength, String defaultBranchCode,
            Map<BankCurrency, String> bankCodes) {
        this.isoCode = isoCode;
        this.name = name;
        this.ibanLength = ibanLength;
        this.defaultBranchCode = defaultBranchCode;
        this.bankCodes = bankCodes;
    }

    // Getters
    public String getIsoCode() { return isoCode; }
    public String getName() { return name; }
    public int getIbanLength() { return ibanLength; }
    public String getDefaultBranchCode() { return defaultBranchCode; }
    public Map<BankCurrency, String> getBankCodes() { return bankCodes; }

    public String getBankCode(BankCurrency currency) {
        return bankCodes.getOrDefault(currency, "PAYZ");
    }

    public static Country fromIsoCode(String isoCode) {
        return Arrays.stream(values())
                .filter(country -> country.isoCode.equals(isoCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported country: " + isoCode));
    }
}
