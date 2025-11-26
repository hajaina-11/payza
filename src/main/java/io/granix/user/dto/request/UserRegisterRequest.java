package io.granix.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegisterRequest {
    @JsonProperty(value = "msisdn", required = true)
    public String msisdn;
    @JsonProperty(required = true)
    public String password;
    @JsonProperty(value = "is_rgpd")
    public boolean RGPDOption;
}
