package io.mosip.signup.api.dto;


import lombok.Data;

@Data
public class IdentityVerificationResult {

    private String id;
    private String verifierId;
    private IDVProcessStepDetail step;
    private IDVProcessFeedback feedback;
}
