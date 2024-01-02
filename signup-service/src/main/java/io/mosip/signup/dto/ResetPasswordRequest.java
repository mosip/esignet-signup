package io.mosip.signup.dto;

import io.mosip.signup.validator.Identifier;
import io.mosip.signup.validator.Password;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @Identifier
    private String identifier;

    @Password
    private String password;
}
