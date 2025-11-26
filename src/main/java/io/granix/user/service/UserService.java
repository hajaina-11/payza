package io.granix.user.service;

import io.granix.common.event.user.UserCreatedEvent;
import io.granix.common.event.user.UserLoggedInEvent;
import io.granix.common.security.CryptoService;
import io.granix.user.database.entity.Role;
import io.granix.user.database.entity.utils.RoleNames;
import io.granix.user.database.entity.UserEntity;
import io.granix.user.database.entity.utils.UserIDPieceType;
import io.granix.user.database.entity.utils.UserStatus;
import io.granix.user.database.repository.UserRepository;
import io.granix.user.dto.request.CompleteUserRegisterRequest;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserService {
    @Inject
    private UserRepository repository;

    @Inject
    private CryptoService cryptoService;

    @Inject
    Event<UserCreatedEvent> userCreatedEvent;

    @Inject
    Event<UserLoggedInEvent> userLoggedInEvent;


    public UserService(){ }

    public UserService(UserRepository repository, CryptoService cryptoService){
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    /**
     * After the user registration, we should emit UserCreatedEvent
     * in order that others module, such as wallet and notification can do their jobs.
     */
    @Transactional
    public UserEntity register(
            String msisdn,
            String password,
            boolean isRGPD
    ) throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException,
            InterruptedException {

        // Verify that msisdn doesn't exist in database
        if (repository.find("msisdnEncrypted", cryptoService.encrypt(msisdn)).firstResult() != null)
            throw new IllegalArgumentException("Phone number already in use");

        // Setup default Role
        var dafaultRole = Role.findOrCreate(RoleNames.USER);

        // Setup User
        var user = new UserEntity();
        user.id = UUID.randomUUID();
        user.msisdnEncrypted = cryptoService.encrypt(msisdn);
        user.passwordHashed = BcryptUtil.bcryptHash(password);
        user.createdAt = LocalDateTime.now();
        user.isRGPD = isRGPD;
        user.status = UserStatus.ACTIVE;
        user.roles.add(dafaultRole);

        repository.persist(user);

        return user;
    }

    //Aditionée temporairement pour le notification
    @Transactional
    public UserEntity registerCompleteUser(CompleteUserRegisterRequest request)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException,
            InterruptedException {

        // Vérification du numéro
        if (repository.find("msisdnEncrypted", cryptoService.encrypt(request.msisdn)).firstResult() != null)
            throw new IllegalArgumentException("Numéro de téléphone déjà utilisé");

        // Validation des champs obligatoires
        if (request.firstname == null || request.firstname.trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }
        if (request.lastname == null || request.lastname.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        // Rôle par défaut
        var defaultRole = Role.findOrCreate(RoleNames.USER);

        // Création de l'utilisateur COMPLET
        var user = new UserEntity();
        user.id = UUID.randomUUID();
        user.msisdnEncrypted = cryptoService.encrypt(request.msisdn);
        user.passwordHashed = BcryptUtil.bcryptHash(request.password);
        user.createdAt = LocalDateTime.now();
        user.isRGPD = request.is_rgpd;
        user.status = UserStatus.ACTIVE;
        user.roles.add(defaultRole);

        // ⭐⭐ INFORMATIONS PERSONNELLES COMPLÈTES ⭐⭐
        user.firstname = request.firstname;
        user.lastname = request.lastname;

        if (request.email != null && !request.email.trim().isEmpty()) {
            user.emailEncrypted = cryptoService.encrypt(request.email);
            System.out.println("✅ Email sauvegardé: " + request.email);
        }

        if (request.birthday != null) {
            user.birthday = request.birthday.toString();
        }

        if (request.gender != null && !request.gender.trim().isEmpty()) {
            user.gender = request.gender;
        }

        if (request.identityPieceType != null && !request.identityPieceType.trim().isEmpty()) {
            user.identityPieceType = UserIDPieceType.valueOf(request.identityPieceType);
        }

        if (request.identityNumber != null && !request.identityNumber.trim().isEmpty()) {
            user.identityNumber = request.identityNumber;
        }

        repository.persist(user);

        System.out.println("🎉 Utilisateur créé avec succès: " + request.firstname + " " + request.lastname);
        try {
            UserCreatedEvent event = new UserCreatedEvent(
                    user.id,
                    user.msisdnEncrypted,
                    user.emailEncrypted
            );

            userCreatedEvent.fireAsync(event);  // ← CORRECTION ICI
            System.out.println("🚀 Événement UserCreatedEvent émis pour: " + user.id);

        } catch (Exception e) {
            System.err.println("❌ Erreur émission événement: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

    // Authentification Event


    private void emitSuccessfulLoginNotification(UserEntity user, String ipAddress, String userAgent) {
        try {
            System.out.println("🔐 Émission événement de connexion RÉUSSIE pour: " + user.firstname);

            UserLoggedInEvent event = new UserLoggedInEvent(
                    user.id,
                    ipAddress,
                    userAgent,
                    java.time.LocalDateTime.now(),
                    true // succès
            );

            userLoggedInEvent.fireAsync(event);

        } catch (Exception e) {
            System.err.println("❌ Erreur émission événement connexion réussie: " + e.getMessage());
        }
    }

    private void emitFailedLoginNotification(UserEntity user, String ipAddress, String userAgent, String reason) {
        try {
            System.out.println("🚨 Émission événement de connexion ÉCHOUÉE - Raison: " + reason);

            UUID userId = user != null ? user.id : UUID.randomUUID(); // ID temporaire si user null

            UserLoggedInEvent event = new UserLoggedInEvent(
                    userId,
                    ipAddress,
                    userAgent,
                    java.time.LocalDateTime.now(),
                    false // échec
            );

            userLoggedInEvent.fireAsync(event);

        } catch (Exception e) {
            System.err.println("❌ Erreur émission événement connexion échouée: " + e.getMessage());
        }
    }

    //jusque là



    @Transactional
    public UserEntity registerAdmin(
            String msisdn,
            String password,
            boolean isRGPD
    ) throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException,
            InterruptedException {

        // Verify that msisdn doesn't exist in database
        if (repository.find("msisdnEncrypted", cryptoService.encrypt(msisdn)).firstResult() != null)
            throw new IllegalArgumentException("Phone number already in use");

        // Setup default Role
        var dafaultRole = Role.findOrCreate(RoleNames.ADMIN);

        // Setup User
        var user = new UserEntity();
        user.id = UUID.randomUUID();
        user.msisdnEncrypted = cryptoService.encrypt(msisdn);
        user.passwordHashed = BcryptUtil.bcryptHash(password);
        user.createdAt = LocalDateTime.now();
        user.isRGPD = isRGPD;
        user.status = UserStatus.ACTIVE;
        user.roles.add(dafaultRole);

        repository.persist(user);

        return user;
    }


    public UserEntity authenticate(String phone, String password)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        String encryptedPhone = cryptoService.encrypt(phone);
        UserEntity user = repository.find("msisdnEncrypted", encryptedPhone).firstResult();

        if (user == null)
            throw new IllegalArgumentException("User not found");

        if (!BcryptUtil.matches(password, user.passwordHashed))
            throw new IllegalArgumentException("Invalid password");

        return user;
    }

    public UserEntity findByMSISDN(String msisdn)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        return repository.find("msisdnEncrypted", cryptoService.encrypt(msisdn)).firstResult();
    }

    public String decryptPhone(UserEntity user)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        return cryptoService.decrypt(user.msisdnEncrypted);
    }

    public String decryptEmail(UserEntity user)
            throws InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            BadPaddingException,
            InvalidKeyException {
        return cryptoService.decrypt(user.emailEncrypted);
    }
}
