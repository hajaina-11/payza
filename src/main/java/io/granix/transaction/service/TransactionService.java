package io.granix.transaction.service;

import io.granix.common.security.CryptoService;
import io.granix.transaction.database.entity.ExternalRequestEntity;
import io.granix.transaction.database.entity.TransactionEntity;
import io.granix.transaction.database.entity.utils.CurrencyType;
import io.granix.transaction.database.entity.utils.TransactionStatus;
import io.granix.transaction.database.entity.utils.TransactionType;
import io.granix.transaction.dto.request.TransactionRequest;
import io.granix.transaction.dto.response.TransactionResponse;
import io.granix.transaction.port.ExternalPaymentGateway;
import io.granix.transaction.repository.ExternalRequestRepository;
import io.granix.transaction.repository.TransactionRepository;
import io.granix.transaction.port.WalletPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository repository;

    @Inject
    WalletPort walletPort;

    @Inject
    CryptoService cryptoService;

    @Inject
    TransactionSecurityService securityService;

    @Inject
    TransactionProcessor processor;

    @Inject
    ExternalPaymentGateway paymentGateway;


    public TransactionService() {}

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, UUID userId) {
        // Security checks
        securityService.validateTransactionPermissions(request.fromWalletId, userId);
        securityService.validateTransactionLimits(request.fromWalletId, request.amount);

        // Validate wallets
        var fromWallet = walletPort.getWalletByUserId(userId, request.fromWalletId);
        var toWallet = walletPort.getWalletById(request.toWalletId);
        if (fromWallet == null || toWallet == null) throw new IllegalArgumentException("Wallet source ou destination non trouvé");

        if (!fromWallet.currencyCode.equals(request.currencyCode))
            throw new IllegalArgumentException("La devise ne correspond pas au wallet source");

        BigDecimal fee = calculateFee(request.amount, request.type);

        TransactionEntity tx = new TransactionEntity();
        tx.fromWalletId = request.fromWalletId;
        tx.toWalletId = request.toWalletId;
        tx.amount = request.amount;
        tx.fee = fee;
        tx.currencyCode = request.currencyCode;
        tx.fromCurrencyCode = request.currencyCode;
        tx.toCurrencyCode = toWallet.currencyCode;
        tx.fromCurrencyType = CurrencyType.FIAT;
        tx.toCurrencyType = CurrencyType.FIAT;
        tx.exchangeRate = BigDecimal.ONE;
        tx.convertedAmount = request.amount;
        tx.type = request.type;

        try {
            tx.referenceEncrypted = cryptoService.encrypt(generateReference());
            if (tx.referenceEncrypted == null) {
                throw new RuntimeException("Erreur : referenceEncrypted est null après chiffrement !");
            }
            if (request.description != null && !request.description.trim().isEmpty()) {
                tx.descriptionEncrypted = cryptoService.encrypt(request.description);
            }
            if (request.ipAddress != null && !request.ipAddress.trim().isEmpty()) {
                tx.ipAddressEncrypted = cryptoService.encrypt(request.ipAddress);
            }
            if (request.deviceInfo != null && !request.deviceInfo.trim().isEmpty()) {
                tx.deviceInfoEncrypted = cryptoService.encrypt(request.deviceInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur chiffrement: " + e.getMessage(), e);
        }

        // Process (persist + debit/credit)
        var processed = processor.processAndPersist(tx);
        return convertToResponse(processed);
    }

    public List<TransactionResponse> getUserTransactions(UUID userId, int page, int size) {
        var wallets = walletPort.searchByUser(userId);
        var walletIds = wallets.stream().map(w -> w.id).collect(Collectors.toList());
        if (walletIds.isEmpty()) return List.of();
        var transactions = repository.findByWalletIds(walletIds, page, size);
        return transactions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public TransactionResponse getTransaction(UUID transactionId, UUID userId) {
        var transaction = repository.findById(transactionId);
        if (transaction == null) throw new IllegalArgumentException("Transaction non trouvée avec l'ID: " + transactionId);
        securityService.validateTransactionAccess(transaction.fromWalletId, transaction.toWalletId, userId);
        return convertToResponse(transaction);
    }

    private BigDecimal calculateFee(BigDecimal amount, TransactionType type) {
        BigDecimal feeRate;
        switch (type) {
            case TRANSFER -> feeRate = BigDecimal.valueOf(0.01);
            case WITHDRAWAL -> feeRate = BigDecimal.valueOf(0.02);
            case DEPOSIT -> feeRate = BigDecimal.valueOf(0.005);
            case PAYMENT -> feeRate = BigDecimal.valueOf(0.015);
            default -> feeRate = BigDecimal.ZERO;
        }
        BigDecimal fee = amount.multiply(feeRate);
        BigDecimal minFee = BigDecimal.valueOf(10);
        return fee.max(minFee);
    }

    private String generateReference() {
        return "TXN" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TransactionResponse convertToResponse(TransactionEntity transaction) {
        try {
            String decryptedReference = cryptoService.decrypt(transaction.referenceEncrypted);
            String decryptedDescription = transaction.descriptionEncrypted != null ? cryptoService.decrypt(transaction.descriptionEncrypted) : null;
            String decryptedBlockchainHash = transaction.blockchainTxHashEncrypted != null ? cryptoService.decrypt(transaction.blockchainTxHashEncrypted) : null;

            return new TransactionResponse(
                    transaction.id,
                    transaction.fromWalletId,
                    transaction.toWalletId,
                    transaction.amount,
                    transaction.convertedAmount,
                    transaction.fee,
                    transaction.currencyCode,
                    transaction.fromCurrencyCode,
                    transaction.toCurrencyCode,
                    transaction.exchangeRate,
                    transaction.type,
                    transaction.status,
                    decryptedReference,
                    decryptedDescription,
                    transaction.createdAt,
                    transaction.processedAt,
                    transaction.failureReason,
                    transaction.fromCurrencyType,
                    transaction.toCurrencyType,
                    decryptedBlockchainHash,
                    transaction.blockchainConfirmations
            );
        } catch (Exception e) {
            return new TransactionResponse(
                    transaction.id,
                    transaction.fromWalletId,
                    transaction.toWalletId,
                    transaction.amount,
                    transaction.convertedAmount != null ? transaction.convertedAmount : transaction.amount,
                    transaction.fee,
                    transaction.currencyCode,
                    transaction.fromCurrencyCode,
                    transaction.toCurrencyCode,
                    transaction.exchangeRate != null ? transaction.exchangeRate : BigDecimal.ONE,
                    transaction.type,
                    transaction.status,
                    "REF_ERROR",
                    null,
                    transaction.createdAt,
                    transaction.processedAt,
                    transaction.failureReason,
                    transaction.fromCurrencyType,
                    transaction.toCurrencyType,
                    null,
                    transaction.blockchainConfirmations
            );
        }
    }

        // ---------------------------- DEPOSIT ----------------------------
        @Transactional
        public TransactionEntity createDeposit(UUID walletId, BigDecimal amount, String currency,
                                               String requestId, String outcome) {

            // 1) Check idempotence
            TransactionEntity existing = repository.find("requestId", requestId).firstResult();
            if (existing != null)
                return existing;

            // 2) Create transaction PENDING
            TransactionEntity tx = new TransactionEntity();
            tx.id = UUID.randomUUID();
            tx.fromWalletId = walletId;
            tx.toWalletId = walletId;
            BigDecimal fee = amount.multiply(new BigDecimal("0.01")); // 1%
            tx.fee = fee;
            tx.amount = amount.subtract(fee); // on crédite net
            tx.currencyCode = currency;
            tx.fromCurrencyCode = currency;
            tx.toCurrencyCode = currency;
            tx.fromCurrencyType = CurrencyType.FIAT;      // optionnel selon ton système
            tx.toCurrencyType = CurrencyType.FIAT;        // optionnel
            tx.exchangeRate = BigDecimal.ONE;
            tx.convertedAmount = amount;
            tx.type = TransactionType.DEPOSIT;
            tx.status = TransactionStatus.PENDING;
            tx.requestId = requestId;
            tx.createdAt = LocalDateTime.now();
            tx.referenceEncrypted = ""; // sera rempli après
            repository.persist(tx);

            try {
                // 3) Call sandbox API
                String externalRef = paymentGateway.deposit(walletId, amount, currency, requestId, outcome);

                // 4) Update transaction
                tx.referenceEncrypted = externalRef;
                tx.status = TransactionStatus.COMPLETED;
                tx.processedAt = LocalDateTime.now();

            } catch (Exception e) {
                tx.status = TransactionStatus.FAILED;
                tx.failureReason = e.getMessage();
            }

            repository.persist(tx);
            return tx;
        }

        // ---------------------------- WITHDRAW ----------------------------
        @Transactional
        public TransactionEntity createWithdrawal(UUID walletId, BigDecimal amount, String currency,
                                                  String requestId, String outcome) {

            TransactionEntity existing = repository.find("requestId", requestId).firstResult();
            if (existing != null)
                return existing;

            TransactionEntity tx = new TransactionEntity();
            tx.id = UUID.randomUUID();
            tx.fromWalletId = walletId;
            tx.toWalletId = walletId;
            BigDecimal fee = amount.multiply(new BigDecimal("0.03")); // 3%
            if(fee.compareTo(new BigDecimal("500")) < 0)
                fee = new BigDecimal("500"); // min 500 Ar

            tx.fee = fee;
            tx.amount = amount.add(fee); // montant final utilisateur = +frais
            tx.currencyCode = currency;
            tx.fromCurrencyCode = currency;
            tx.toCurrencyCode = currency;
            tx.fromCurrencyType = CurrencyType.FIAT;
            tx.toCurrencyType = CurrencyType.FIAT;
            tx.exchangeRate = BigDecimal.ONE;
            tx.convertedAmount = amount;
            tx.type = TransactionType.WITHDRAWAL;
            tx.status = TransactionStatus.PENDING;
            tx.requestId = requestId;
            tx.createdAt = LocalDateTime.now();
            tx.referenceEncrypted = "";
            repository.persist(tx);

            try {
                String externalRef = paymentGateway.withdraw(walletId, amount, currency, requestId, outcome);
                tx.referenceEncrypted = externalRef;
                tx.status = TransactionStatus.COMPLETED;
                tx.processedAt = LocalDateTime.now();

            } catch (Exception e) {
                tx.status = TransactionStatus.FAILED;
                tx.failureReason = e.getMessage();
            }

            repository.persist(tx);
            return tx;
        }
    public List<TransactionResponse> getWalletTransactions(UUID walletId) {
        var transactions = repository.findByWalletId(walletId, 0, 50);
        return transactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    }

