package io.mosip.signup.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Language;
import io.mosip.signup.validator.Password;
import io.mosip.signup.validator.UserInfo;
import io.mosip.signup.validator.Username;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
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

    public RegisterRequest(String username, String password, String consent, JsonNode userInfo, String locale) {
        this.userInfo = userInfo;
        this.username = username;
        this.consent = consent;
        this.locale = locale;

        //TODO - This is temporary fix we should remove the hardcoding from here.
        ((ObjectNode)this.userInfo).set("password", JsonNodeFactory.instance.textNode(password));
        this.password = password;
    }
}
