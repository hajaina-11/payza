package io.granix.wallet.entity.utils;

public enum CryptoCurrency implements Currency {
    BTC("BTC", "Bitcoin", 8),
    ETH("ETH", "Ethereum", 18),
    USDT("USDT", "Tether", 6),
    BNB("BNB", "Binance Coin", 18),
    ADA("ADA", "Cardano", 6),
    DOT("DOT", "Polkadot", 10);

    private final String code;
    private final String name;
    private final int decimalPlaces;

    CryptoCurrency(String code, String name, int decimalPlaces) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
    }

    @Override public String getCode() { return code; }
    @Override public String getName() { return name; }
    @Override public int getDecimalPlaces() { return decimalPlaces; }
    @Override public CurrencyType getType() { return CurrencyType.CRYPTO; }
    @Override public boolean isVolatile() { return true; }
}
