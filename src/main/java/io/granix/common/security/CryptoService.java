package io.granix.common.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@ApplicationScoped
public class CryptoService {
    private static final String ALGO = ConfigProvider.getConfig()
            .getOptionalValue("payza.crypto.algo", String.class)
            .orElse("AES");
    private static final int TAG_LENGTH = ConfigProvider.getConfig()
            .getOptionalValue("payza.crypto.tag-length", Integer.class)
            .orElse(128);
    private static final byte[] KEY = ConfigProvider.getConfig()
            .getOptionalValue("payza.crypto.byte-key", String.class)
            .isPresent()
            ? ConfigProvider.getConfig()
            .getOptionalValue("payza.crypto.byte-key", String.class)
            .get().getBytes()
            : "12345678901234567890123456789012".getBytes();
    private static final byte[] IV = ConfigProvider.getConfig()
            .getOptionalValue("payza.crypto.byte-iv", String.class)
            .isPresent()
            ? ConfigProvider.getConfig()
            .getOptionalValue("payza.crypto.byte-iv", String.class)
            .get().getBytes()
            : " ".getBytes();

    private SecretKey getSecretKey() {
        return new SecretKeySpec(KEY, "AES");
    }

    public String encrypt(String plain)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        System.out.println("TAG_LENGTH:" + TAG_LENGTH);
        System.out.println("IV:" + IV);
        var spec = new GCMParameterSpec(TAG_LENGTH, IV);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);
        var encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encrypted)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        var spec = new GCMParameterSpec(TAG_LENGTH, IV);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);
        var decoded = Base64.getDecoder().decode(encrypted);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }
}
