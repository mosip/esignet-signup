package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Consent;
import io.mosip.signup.validator.DisabilityType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SlotRequest {

    @NotBlank(message = ErrorConstants.INVALID_IDENTITY_VERIFIER_ID)
    private String verifierId;

    @Consent
    private String consent;

    @DisabilityType
    private String disabilityType;

}
