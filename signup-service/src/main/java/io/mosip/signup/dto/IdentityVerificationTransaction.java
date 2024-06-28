package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class IdentityVerificationTransaction implements Serializable {

    private String individualId;
    private String slotId;
    private String verifierId;
    private String accessToken;

}
