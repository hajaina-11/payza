package io.granix.wallet.service.iban.config;

import io.granix.wallet.entity.utils.BankCurrency;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class IbanConfig {
    private String defaultBankCode = "PAYZ";
    private String defaultBranchCode = "001";
    private Map<String, CountryIbanConfig> countries = new HashMap<>();

    public String getDefaultBankCode() {
        return defaultBankCode;
    }

    public IbanConfig setDefaultBankCode(String defaultBankCode) {
        this.defaultBankCode = defaultBankCode;
        return this;
    }

    public String getDefaultBranchCode() {
        return defaultBranchCode;
    }

    public IbanConfig setDefaultBranchCode(String defaultBranchCode) {
        this.defaultBranchCode = defaultBranchCode;
        return this;
    }

    public Map<String, CountryIbanConfig> getCountries() {
        return countries;
    }

    public IbanConfig setCountries(Map<String, CountryIbanConfig> countries) {
        this.countries = countries;
        return this;
    }

    @PostConstruct
    public void initDefaults() {
        // Configuration par défaut si pas dans application.yml
        if (countries.isEmpty()) {
            loadDefaultConfigurations();
        }
    }

    private void loadDefaultConfigurations() {
        for (Country country : Country.values()) {
            countries.put(country.getIsoCode(), new CountryIbanConfig(
                    country.getIbanLength(),
                    country.getDefaultBranchCode(),
                    country.getBankCodes()
            ));
        }
    }

    public static class CountryIbanConfig {
        private int length;
        private String branchCode;
        private Map<BankCurrency, String> bankCodes;

        public CountryIbanConfig(int length, String branchCode, Map<BankCurrency, String> bankCodes) {
            this.length = length;
            this.branchCode = branchCode;
            this.bankCodes = bankCodes;
        }

        public int getLength() {
            return length;
        }

        public String getBranchCode() {
            return branchCode;
        }

        public Map<BankCurrency, String> getBankCodes() {
            return bankCodes;
        }

        public CountryIbanConfig setLength(int length) {
            this.length = length;
            return this;
        }

        public CountryIbanConfig setBranchCode(String branchCode) {
            this.branchCode = branchCode;
            return this;
        }

        public CountryIbanConfig setBankCodes(Map<BankCurrency, String> bankCodes) {
            this.bankCodes = bankCodes;
            return this;
        }
    }
}
