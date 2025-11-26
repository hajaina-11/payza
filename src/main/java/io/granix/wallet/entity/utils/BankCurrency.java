package io.granix.wallet.entity.utils;

public enum BankCurrency implements Currency {
    MGA("MGA", "Malagasy Ariary", 2),
    
    EUR("EUR", "Euro", 2),
    USD("USD", "US Dollar", 2),
    XOF("XOF", "West African CFA Franc", 0),
    XAF("XAF", "Central African CFA Franc", 0),
    ZAR("ZAR", "South African Rand", 2),
    KES("KES", "Kenyan Shilling", 2),
    TZS("TZS", "Tanzanian Shilling", 2);

    private final String code;
    private final String name;
    private final int decimalPlaces;

    BankCurrency(String code, String name, int decimalPlaces) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
    }

    @Override public String getCode() { return code; }
    @Override public String getName() { return name; }
    @Override public int getDecimalPlaces() { return decimalPlaces; }
    @Override public CurrencyType getType() { return CurrencyType.FIAT; }
    @Override public boolean isVolatile() { return false; }
}
