package io.mosip.signup.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Language;
import io.mosip.signup.validator.Password;
import io.mosip.signup.validator.UserInfo;
import io.mosip.signup.validator.Username;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class RegisterRequest {

    @Username
    private String username;

    @Password
    private String password;

    @NotBlank(message = ErrorConstants.INVALID_CONSENT)
    @Pattern(message = ErrorConstants.INVALID_CONSENT, regexp = "^(DISAGREE)|(AGREE)$")
    private String consent;

    @UserInfo
    private JsonNode userInfo;

    @Language(required = false)
    private String locale;
}
