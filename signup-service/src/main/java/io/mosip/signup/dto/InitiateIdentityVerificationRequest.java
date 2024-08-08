package io.mosip.signup.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;

import static io.mosip.signup.util.ErrorConstants.INVALID_AUTHORIZATION_CODE;
import static io.mosip.signup.util.ErrorConstants.INVALID_STATE;

@Data
public class InitiateIdentityVerificationRequest {

    @NotBlank(message = INVALID_AUTHORIZATION_CODE)
    private String authorizationCode;

    @NotBlank(message = INVALID_STATE)
    private String state;
}
