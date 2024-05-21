package io.mosip.signup.dto;

import lombok.Data;

@Data
public class IdentityVerificationRequest {

    private String slotId;
    private String stepCode;
    private String[] frames;
}
