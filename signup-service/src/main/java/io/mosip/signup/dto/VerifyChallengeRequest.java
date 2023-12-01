package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Identifier;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class VerifyChallengeRequest {

    @Identifier
    private String identifier;

    @Valid
    @NotNull(message = ErrorConstants.INVALID_CHALLENGE_INFO)
    private ChallengeInfo challengeInfo;
}
