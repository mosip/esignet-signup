package io.mosip.signup.dto;

import lombok.Data;

@Data
public class IdentityVerificationResponse {

    private String slotId;
    private IDVProcessStepDetail step;
    private IDVProcessFeedback feedback;
}
