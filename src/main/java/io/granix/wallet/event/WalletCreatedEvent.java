package io.granix.wallet.event;

public class WalletCreatedEvent {
    private final String userId;
    private final String walletId;

    public WalletCreatedEvent(String userId, String walletId) {
        this.userId = userId;
        this.walletId = walletId;
    }

    public String getUserId() {
        return userId;
    }

    public String getWalletId() {
        return walletId;
    }
}
