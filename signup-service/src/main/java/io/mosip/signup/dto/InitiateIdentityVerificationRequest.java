/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;

import static io.mosip.signup.util.ErrorConstants.INVALID_AUTHORIZATION_CODE;
import static io.mosip.signup.util.ErrorConstants.INVALID_STATE;

@Data
public class InitiateIdentityVerificationRequest {

    @NotBlank(message = INVALID_AUTHORIZATION_CODE)
    private String authorizationCode;

    @NotBlank(message = INVALID_STATE)
    private String state;
}
