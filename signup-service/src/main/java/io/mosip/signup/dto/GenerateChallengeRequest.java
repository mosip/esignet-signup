package io.mosip.signup.dto;

import io.mosip.signup.util.Purpose;
import io.mosip.signup.validator.Identifier;
import io.mosip.signup.validator.Language;
import lombok.Data;

@Data
public class GenerateChallengeRequest {

    @Identifier
    private String identifier;
    private String captchaToken;
    @Language(required = false)
    private String locale;
    private boolean regenerateChallenge;

    @io.mosip.signup.validator.Purpose
    private Purpose purpose;
}
