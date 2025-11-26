package io.granix.wallet.service;

import io.granix.wallet.entity.WalletEntity;
import io.granix.wallet.entity.utils.BankCurrency;
import io.granix.wallet.entity.utils.CryptoCurrency;
import io.granix.wallet.entity.utils.CurrencyType;
import io.granix.wallet.repository.WalletRepository;
import io.granix.wallet.service.iban.IbanGeneratorService;
import io.granix.wallet.service.iban.IbanSecurityService;
import io.granix.wallet.service.iban.config.Country;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@ApplicationScoped
public class WalletGeneratorService {
    @Inject
    IbanSecurityService encryptor;

    @Inject
    IbanGeneratorService generator;

    @Inject
    WalletRepository repository;

    public WalletEntity generate(
            Country country,
            BankCurrency bankCurrency,
            UUID userId
    ) throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        var result = new WalletEntity();

        result.id = UUID.randomUUID();
        result.ibanEncrypted = encryptor.encrypt(generator.generateIban(
                String.valueOf(result.id),
                country,
                bankCurrency
        ));
        result.currencyCode = bankCurrency.getCode();
        result.currencyType = CurrencyType.FIAT;
        result.ownerId = userId;

        repository.persist(result);

        return result;
    }

    // Méthode additonée pour les compte crypto
    public WalletEntity generateCrypto(
            Country country,
            CryptoCurrency cryptoCurrency,
            UUID userId
    ) throws Exception {

        var result = new WalletEntity();

        result.id = UUID.randomUUID();

        // IBAN crypto = adresse crypto simulée
        // (tu pourras remplacer par une vraie génération plus tard)
        String fakeIban = "CR-" + cryptoCurrency.name() + "-" + result.id.toString().substring(0, 8);

        result.ibanEncrypted = encryptor.encrypt(fakeIban);
        result.currencyCode = cryptoCurrency.name();     // ex: BTC, ETH
        result.currencyType = CurrencyType.CRYPTO;
        result.ownerId = userId;

        repository.persist(result);

        return result;
    }

}
