package io.granix.wallet.entity.utils;

public enum WalletOperation {
    VIEW_BALANCE("View balance", OperationType.READ),
    SEND_MONEY("Send money", OperationType.DEBIT),
    RECEIVE_MONEY("Receive money", OperationType.CREDIT),
    WITHDRAW("Withdraw", OperationType.DEBIT),
    DEPOSIT("Deposit", OperationType.CREDIT),
    INTERNATIONAL_TRANSFER("International transfer", OperationType.DEBIT),
    CRYPTO_BUY("Buy cryptocurrency", OperationType.DEBIT),
    CRYPTO_SELL("Sell cryptocurrency", OperationType.CREDIT),
    CRYPTO_TRANSFER("Transfer cryptocurrency", OperationType.DEBIT),
    CRYPTO_STAKE("Stake cryptocurrency", OperationType.DEBIT),
    CRYPTO_UNSTAKE("Unstake cryptocurrency", OperationType.CREDIT),
    P2P_TRADE("Peer-to-peer trade", OperationType.BOTH),
    BILL_PAYMENT("Bill payment", OperationType.DEBIT),
    MOBILE_TOPUP("Mobile top-up", OperationType.DEBIT);

    private final String description;
    private final OperationType type;

    WalletOperation(String description, OperationType type) {
        this.description = description;
        this.type = type;
    }

    public String getDescription() { return description; }
    public OperationType getType() { return type; }
}
