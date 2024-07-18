package io.mosip.signup.dto;

import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.api.util.VerificationStatus;
import lombok.Data;

import java.io.Serializable;

@Data
public class IdentityVerificationTransaction implements Serializable {

    private String individualId;
    private String slotId;
    private String verifierId;
    private String accessToken;

    //Id to track the profile update status
    private String applicationId;
    private VerificationStatus status;
    private String errorCode;

}
