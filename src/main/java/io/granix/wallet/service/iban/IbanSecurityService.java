package io.granix.wallet.service.iban;

import io.granix.common.security.CryptoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class IbanSecurityService {
    @Inject
    CryptoService encryptor;

    public String encrypt(String iban)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        return encryptor.encrypt(iban);
    }

    public String decrypt(String encryptedIban)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        return encryptor.decrypt(encryptedIban);
    }
}
