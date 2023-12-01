package io.mosip.signup.dto;

import io.mosip.signup.validator.Identifier;
import lombok.Data;

@Data
public class GenerateChallengeRequest {

    @Identifier
    private String identifier;
    private String captchaToken;
    private String locale;
}
