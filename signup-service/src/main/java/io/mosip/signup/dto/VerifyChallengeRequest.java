package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class VerifyChallengeRequest {

    @NotBlank(message = ErrorConstants.INVALID_IDENTIFIER)
    private String identifier;

    @Valid
    @NotNull(message = ErrorConstants.INVALID_CHALLENGE_INFO)
    private ChallengeInfo challengeInfo;
}
