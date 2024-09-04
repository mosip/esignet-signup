/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
