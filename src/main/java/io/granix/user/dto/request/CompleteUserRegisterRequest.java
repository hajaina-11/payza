package io.granix.user.dto.request;

import java.time.LocalDate;

public class CompleteUserRegisterRequest {
    // Identification
    public String msisdn;
    public String password;

    // Informations personnelles
    public String firstname;
    public String lastname;
    public String email;
    public LocalDate birthday;
    public String gender;

    // Pièce d'identité
    public String identityPieceType;
    public String identityNumber;

    // Consentements
    public boolean is_rgpd;

    // Constructeurs
    public CompleteUserRegisterRequest() {}

    public CompleteUserRegisterRequest(String msisdn, String password, String firstname,
                                       String lastname, String email, LocalDate birthday,
                                       String gender, String identityPieceType,
                                       String identityNumber, boolean is_rgpd) {
        this.msisdn = msisdn;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
        this.identityPieceType = identityPieceType;
        this.identityNumber = identityNumber;
        this.is_rgpd = is_rgpd;
    }
}