/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.api.util.VerificationStatus;
import lombok.Data;

import java.io.Serializable;

@Data
public class IdentityVerificationTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    private String individualId;
    private String slotId;
    private String verifierId;
    private String disabilityType;
    private String accessToken;

    //Id to track the profile update status
    private String applicationId;
    private VerificationStatus status;
    private String errorCode;

}
