package io.mosip.signup.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class InitiateIdentityVerificationRequest {

    @NotBlank
    private String authorizationCode;

    @NotBlank
    private String state;
}
