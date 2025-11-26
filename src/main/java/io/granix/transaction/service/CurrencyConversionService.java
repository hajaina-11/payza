package io.granix.transaction.service;

import io.granix.transaction.database.entity.utils.CurrencyType;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@ApplicationScoped
public class CurrencyConversionService {

    private final Config config = ConfigProvider.getConfig();

    /**
     * Convert amount using configured exchange rates in application.properties.
     * Fallback: throws IllegalArgumentException if not configured.
     * Expected keys in application.properties: payza.exchange-rate.<from>-<to>=<rate>
     * e.g. payza.exchange-rate.MGA-EUR=0.00020
     */
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return amount;

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(8, RoundingMode.HALF_UP);
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return BigDecimal.ONE;

        String key = String.format("payza.exchange-rate.%s-%s", fromCurrency.toLowerCase(), toCurrency.toLowerCase())
                .replaceAll("\\s+", "");

        // Try case-insensitive search variants
        Optional<BigDecimal> cfgRate = getConfigDecimal(
                String.format("payza.exchange-rate.%s-%s", fromCurrency.toUpperCase(), toCurrency.toUpperCase())
        );
        if (cfgRate.isPresent()) return cfgRate.get();

        cfgRate = getConfigDecimal(key);
        if (cfgRate.isPresent()) return cfgRate.get();

        // Try common swapped currency keys
        cfgRate = getConfigDecimal(String.format("payza.exchange-rate.%s-%s", fromCurrency, toCurrency));
        if (cfgRate.isPresent()) return cfgRate.get();

        throw new IllegalArgumentException("Taux de change non disponible pour: " + fromCurrency + " -> " + toCurrency);
    }

    private Optional<BigDecimal> getConfigDecimal(String propertyName) {
        try {
            Config cfg = ConfigProvider.getConfig();
            Optional<String> val = cfg.getOptionalValue(propertyName, String.class);
            if (val.isPresent()) {
                return Optional.of(new BigDecimal(val.get()));
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    public boolean areCurrenciesCompatible(String fromCurrency, String toCurrency) {
        try {
            getExchangeRate(fromCurrency, toCurrency);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CurrencyType getCurrencyType(String currencyCode) {
        try {
            // Determine type by wallet currency enums in wallet module if available
            // Fallback heuristic: common crypto codes
            switch (currencyCode.toUpperCase()) {
                case "BTC":
                case "ETH":
                case "USDT":
                case "BNB":
                case "ADA":
                case "DOT":
                    return CurrencyType.CRYPTO;
                default:
                    return CurrencyType.FIAT;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Devise non supportée: " + currencyCode);
        }
    }
}
