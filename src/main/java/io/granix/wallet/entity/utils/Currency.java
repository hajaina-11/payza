package io.granix.wallet.entity.utils;

public interface Currency {
    String getCode();
    String getName();
    int getDecimalPlaces();
    CurrencyType getType();
    boolean isVolatile(); // Pour les cryptos
}
