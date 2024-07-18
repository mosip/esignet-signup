package io.mosip.signup.dto;

import io.mosip.signup.api.util.VerificationStatus;
import lombok.Data;

@Data
public class IdentityVerificationStatusResponse {

    private VerificationStatus status;

}
