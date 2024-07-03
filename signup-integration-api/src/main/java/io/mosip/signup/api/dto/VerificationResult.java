package io.mosip.signup.api.dto;


import io.mosip.signup.api.util.VerificationStatus;
import lombok.Data;

import java.util.Map;

@Data
public class VerificationResult {

    private VerificationStatus status;
    private Map<String, VerificationDetail> verifiedClaims;
    private String errorCode;
}
