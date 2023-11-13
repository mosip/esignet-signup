package io.mosip.signup.dto;

import io.mosip.esignet.core.constants.ErrorConstants;
import io.mosip.signup.validator.ChallengeFormatType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChallengeInfo {

    @NotBlank(message = ErrorConstants.INVALID_CHALLENGE)
    @Size(max = 6, message = ErrorConstants.INVALID_CHALLENGE)
    private String challenge;

    @NotBlank(message = ErrorConstants.INVALID_CHALLENGE_FORMAT)
    private @ChallengeFormatType String format;
}
