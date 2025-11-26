package io.granix.transaction.service;

import io.granix.common.security.CryptoService;
import io.granix.transaction.database.entity.TransactionEntity;
import io.granix.transaction.database.entity.utils.CurrencyType;
import io.granix.transaction.database.entity.utils.TransactionType;
import io.granix.transaction.dto.request.MultiCurrencyTransactionRequest;
import io.granix.transaction.dto.response.TransactionResponse;
import io.granix.transaction.repository.TransactionRepository;
import io.granix.transaction.port.WalletPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class MultiCurrencyTransactionService {

    @Inject
    TransactionRepository repository;

    @Inject
    WalletPort walletPort;

    @Inject
    CryptoService cryptoService;

    @Inject
    TransactionSecurityService securityService;

    @Inject
    CurrencyConversionService conversionService;

    @Inject
    TransactionProcessor processor;

    @Transactional
    public TransactionResponse createMultiCurrencyTransaction(MultiCurrencyTransactionRequest request, UUID userId) {
        // Security
        securityService.validateTransactionPermissions(request.fromWalletId, userId);
        securityService.validateTransactionLimits(request.fromWalletId, request.amount);

        var fromWallet = walletPort.getWalletByUserId(userId, request.fromWalletId);
        var toWallet = walletPort.getWalletById(request.toWalletId);
        if (fromWallet == null || toWallet == null) throw new IllegalArgumentException("Wallet source ou destination non trouvé");

        CurrencyType fromCurrencyType = conversionService.getCurrencyType(fromWallet.currencyCode);
        CurrencyType toCurrencyType = conversionService.getCurrencyType(toWallet.currencyCode);

        boolean isCrossCurrency = !fromWallet.currencyCode.equals(toWallet.currencyCode);

        BigDecimal finalAmount = request.amount;
        BigDecimal exchangeRate = BigDecimal.ONE;
        if (isCrossCurrency) {
            if (!conversionService.areCurrenciesCompatible(fromWallet.currencyCode, toWallet.currencyCode)) {
                throw new IllegalArgumentException("Conversion non supportée: " + fromWallet.currencyCode + " vers " + toWallet.currencyCode);
            }
            exchangeRate = conversionService.getExchangeRate(fromWallet.currencyCode, toWallet.currencyCode);
            finalAmount = conversionService.convertAmount(request.amount, fromWallet.currencyCode, toWallet.currencyCode);
        }

        BigDecimal fee = calculateMultiCurrencyFee(request.amount, fromCurrencyType, toCurrencyType, isCrossCurrency);

        TransactionEntity tx = new TransactionEntity();
        tx.fromWalletId = request.fromWalletId;
        tx.toWalletId = request.toWalletId;
        tx.amount = request.amount;
        tx.convertedAmount = finalAmount;
        tx.fee = fee;
        tx.currencyCode = fromWallet.currencyCode;
        tx.fromCurrencyCode = fromWallet.currencyCode;
        tx.toCurrencyCode = toWallet.currencyCode;
        tx.fromCurrencyType = fromCurrencyType;
        tx.toCurrencyType = toCurrencyType;
        tx.exchangeRate = exchangeRate;
        tx.type = determineTransactionType(fromCurrencyType, toCurrencyType, isCrossCurrency);

        // Crypto-specific
        try {
            // 🔐 Génération et chiffrement de la référence
            String ref = generateReference();
            String encRef = cryptoService.encrypt(ref);
            System.out.println("🔐 [DEBUG] Reference générée = " + ref);
            System.out.println("🔐 [DEBUG] Reference chiffrée = " + encRef);

            // Protection anti-null pour éviter les erreurs SQL
            if (encRef == null || encRef.trim().isEmpty()) {
                System.out.println("⚠️ [WARN] Échec du chiffrement, utilisation de fallback non chiffré.");
                encRef = "UNENCRYPTED-" + ref;
            }

            tx.referenceEncrypted = encRef;

            // Autres champs à chiffrer
            if (request.cryptoWalletAddress != null && !request.cryptoWalletAddress.trim().isEmpty()) {
                tx.cryptoWalletAddressEncrypted = cryptoService.encrypt(request.cryptoWalletAddress);
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

            if (request.networkFeeLevel != null && !request.networkFeeLevel.trim().isEmpty()) {
                var metadata = "{\"network_fee_level\":\"" + request.networkFeeLevel + "\"}";
                tx.metadataEncrypted = cryptoService.encrypt(metadata);
            }

        } catch (Exception e) {
            // En cas d’erreur grave de chiffrement, on crée une référence de secours
            System.out.println("❌ [ERROR] Erreur de chiffrement: " + e.getMessage());
            tx.referenceEncrypted = "UNENCRYPTED-" + generateReference();
        }


        var processed = processor.processAndPersist(tx);
        return convertToResponse(processed);
    }

    private BigDecimal calculateMultiCurrencyFee(BigDecimal amount, CurrencyType fromType,
                                                 CurrencyType toType, boolean isCrossCurrency) {
        BigDecimal feeRate = BigDecimal.ZERO;
        if (isCrossCurrency) feeRate = BigDecimal.valueOf(0.02);
        else if (fromType == CurrencyType.CRYPTO || toType == CurrencyType.CRYPTO) feeRate = BigDecimal.valueOf(0.001);
        else feeRate = BigDecimal.valueOf(0.01);
        BigDecimal fee = amount.multiply(feeRate);
        BigDecimal minFee = BigDecimal.valueOf(0.01);
        return fee.max(minFee);
    }

    private TransactionType determineTransactionType(CurrencyType fromType, CurrencyType toType, boolean isCrossCurrency) {
        if (isCrossCurrency) return TransactionType.CONVERSION;
        else if (fromType == CurrencyType.CRYPTO || toType == CurrencyType.CRYPTO) return TransactionType.CRYPTO_TRANSFER;
        else return TransactionType.TRANSFER;
    }

    private String generateReference() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TransactionResponse convertToResponse(io.granix.transaction.database.entity.TransactionEntity transaction) {
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

    public CurrencyConversionService getConversionService() {
        return conversionService;
    }

    /**
     * Estime les frais pour une transaction multi-devises sans l'exécuter.
     * Utile pour l'affichage d'une prévisualisation côté frontend.
     */
    public BigDecimal estimateFee(BigDecimal amount, String fromCurrency, String toCurrency) {
        try {
            boolean isCrossCurrency = !fromCurrency.equalsIgnoreCase(toCurrency);
            var fromType = conversionService.getCurrencyType(fromCurrency);
            var toType = conversionService.getCurrencyType(toCurrency);
            return calculateMultiCurrencyFee(amount, fromType, toType, isCrossCurrency);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible d'estimer les frais : " + e.getMessage(), e);
        }
    }

}
