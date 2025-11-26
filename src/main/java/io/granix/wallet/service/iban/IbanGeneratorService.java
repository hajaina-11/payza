package io.granix.wallet.service.iban;

import io.granix.wallet.entity.utils.BankCurrency;
import io.granix.wallet.entity.utils.CurrencyType;
import io.granix.wallet.exception.InvalidIbanException;
import io.granix.wallet.exception.UnsupportedCountryException;
import io.granix.wallet.service.iban.config.IbanConfig;
import io.granix.wallet.service.iban.config.Country;
import io.granix.wallet.service.iban.utils.IbanInfo;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;

@ApplicationScoped
public class IbanGeneratorService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger logger = LoggerFactory.getLogger(IbanGeneratorService.class);

    private final IbanConfig ibanConfig;

    public IbanGeneratorService(IbanConfig ibanConfig) {
        this.ibanConfig = ibanConfig;
    }

    /**
     * Génère un IBAN pour un wallet donné
     */
    public String generateIban(String walletId, Country country, BankCurrency currency) throws UnsupportedCountryException {
        validateInputs(walletId, country, currency);

        IbanConfig.CountryIbanConfig countryConfig = ibanConfig.getCountries().get(country.getIsoCode());
        if (countryConfig == null)
            throw new UnsupportedCountryException("Country not supported for IBAN generation: " + country.getName());

        String bankCode = countryConfig.getBankCodes().getOrDefault(currency, ibanConfig.getDefaultBankCode());
        String branchCode = countryConfig.getBranchCode();
        String accountNumber = generateAccountNumber(walletId, countryConfig.getLength(), bankCode, branchCode);

        // IBAN provisoire sans les chiffres de contrôle
        String bban = bankCode + branchCode + accountNumber; // Basic Bank Account Number
        String provisionalIban = country.getIsoCode() + "00" + bban;

        // Calcul des chiffres de contrôle
        int checkDigits = calculateCheckDigits(provisionalIban);

        String finalIban = country.getIsoCode() + String.format("%02d", checkDigits) + bban;

        logger.debug("Generated IBAN {} for wallet {} in country {}",
                maskIban(finalIban), walletId, country.getName());

        return finalIban;
    }

    /**
     * Génère un IBAN en détectant automatiquement le pays depuis l'utilisateur
     */
    public String generateIban(String walletId, String userId, BankCurrency currency) throws UnsupportedCountryException {
        // Vous pourrez implémenter la détection du pays via le profil utilisateur
        Country userCountry = detectUserCountry(userId);
        return generateIban(walletId, userCountry, currency);
    }

    /**
     * Valide un IBAN existant
     */
    public boolean isValidIban(String iban) {
        if (iban == null || iban.length() < 15)
            return false;

        String countryCode = iban.substring(0, 2);

        try {
            Country country = Country.fromIsoCode(countryCode);

            if (iban.length() != country.getIbanLength())
                return false;

            return calculateCheckDigits(iban.substring(0, 2) + "00" + iban.substring(4))
                    == Integer.parseInt(iban.substring(2, 4));

        } catch (Exception e) {
            logger.warn("Invalid IBAN format: {}", maskIban(iban), e);
            return false;
        }
    }

    /**
     * Extrait les informations d'un IBAN
     */
    public IbanInfo parseIban(String iban) throws InvalidIbanException {
        if (!isValidIban(iban))
            throw new InvalidIbanException("Invalid IBAN: " + maskIban(iban));

        String countryCode = iban.substring(0, 2);
        String checkDigits = iban.substring(2, 4);
        Country country = Country.fromIsoCode(countryCode);

        String bban = iban.substring(4);
        String bankCode = bban.substring(0, 4);
        String branchCode = bban.substring(4, 7);
        String accountNumber = bban.substring(7);

        return new IbanInfo(iban, country, bankCode, branchCode, accountNumber, checkDigits);
    }

    private void validateInputs(String walletId, Country country, BankCurrency currency) {
        if (walletId == null || walletId.trim().isEmpty())
            throw new IllegalArgumentException("WalletId cannot be null or empty");

        if (country == null)
            throw new IllegalArgumentException("Country cannot be null");

        if (currency == null)
            throw new IllegalArgumentException("Currency cannot be null");

        if (currency.getType() != CurrencyType.FIAT)
            throw new IllegalArgumentException("IBAN generation only supports fiat currencies");
    }

    private String generateAccountNumber(String walletId, int ibanLength, String bankCode, String branchCode) {
        // Calcul de la longueur disponible pour le numéro de compte
        int accountNumberLength = ibanLength - 4 - bankCode.length() - branchCode.length(); // 4 = country(2) + check(2)

        // Hash du walletId pour reproductibilité
        String hash = Integer.toUnsignedString(walletId.hashCode());

        // Complément aléatoire sécurisé
        StringBuilder accountNumber = new StringBuilder();
        accountNumber.append(hash);

        // Compléter avec des chiffres aléatoires si nécessaire
        while (accountNumber.length() < accountNumberLength)
            accountNumber.append(RANDOM.nextInt(10));

        // Tronquer si trop long
        return accountNumber.substring(0, accountNumberLength);
    }

    private int calculateCheckDigits(String iban) {
        // Déplacer les 4 premiers caractères à la fin
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Remplacer les lettres par leurs valeurs numériques (A=10, B=11, etc.)
        StringBuilder numericString = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c))
                numericString.append(Character.getNumericValue(c));
            else
                numericString.append(c);
        }

        // Calcul modulo 97 avec support des très grands nombres
        BigInteger num = new BigInteger(numericString.toString());
        int remainder = num.mod(BigInteger.valueOf(97)).intValue();

        return 98 - remainder;
    }

    private Country detectUserCountry(String userId) {
        // TODO: Implémenter la détection via le profil utilisateur
        // Pour l'instant, Madagascar par défaut
        return Country.MADAGASCAR;
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return "****";
        }
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }
}
