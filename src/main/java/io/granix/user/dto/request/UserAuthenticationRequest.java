package io.granix.user.dto.request;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserAuthenticationRequest {
    public String msisdn;
    public String password;

    public UserAuthenticationRequest(String msisdn, String password) {
        this.msisdn = msisdn;
        this.password = password;
    }

    public UserAuthenticationRequest() {}
}
