package io.granix.wallet.service.iban.utils;

import io.granix.wallet.entity.utils.BankCurrency;
import io.granix.wallet.service.iban.config.Country;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class IbanInfo {
    private String iban;
    private Country country;
    private String bankCode;
    private String branchCode;
    private String accountNumber;
    private String checkDigits;

    public IbanInfo(String iban, Country country, String bankCode, String branchCode, String accountNumber, String checkDigits) {
        this.iban = iban;
        this.country = country;
        this.bankCode = bankCode;
        this.branchCode = branchCode;
        this.accountNumber = accountNumber;
        this.checkDigits = checkDigits;
    }

    public IbanInfo() {
    }

    public String getIban() {
        return iban;
    }

    public IbanInfo setIban(String iban) {
        this.iban = iban;
        return this;
    }

    public Country getCountry() {
        return country;
    }

    public IbanInfo setCountry(Country country) {
        this.country = country;
        return this;
    }

    public String getBankCode() {
        return bankCode;
    }

    public IbanInfo setBankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public IbanInfo setBranchCode(String branchCode) {
        this.branchCode = branchCode;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public IbanInfo setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getCheckDigits() {
        return checkDigits;
    }

    public IbanInfo setCheckDigits(String checkDigits) {
        this.checkDigits = checkDigits;
        return this;
    }

    public boolean isPayzaIban() {
        return "PAYZ".equals(bankCode);
    }

    public BankCurrency detectCurrency() {
        // Logique pour détecter la devise depuis le code banque ou pays
        return country.getBankCodes().entrySet().stream()
                .filter(entry -> entry.getValue().equals(bankCode))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(getDefaultCurrencyForCountry());
    }

    private BankCurrency getDefaultCurrencyForCountry() {
        return switch (country) {
            case MADAGASCAR -> BankCurrency.MGA;
            case SOUTH_AFRICA -> BankCurrency.ZAR;
            case KENYA -> BankCurrency.KES;
            case TANZANIA -> BankCurrency.TZS;
            case SENEGAL -> BankCurrency.XOF;
            case CAMEROON -> BankCurrency.XAF;
        };
    }
}
