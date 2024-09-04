/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.ChallengeFormat;
import io.mosip.signup.validator.ChallengeType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChallengeInfo {

    @NotBlank(message = ErrorConstants.INVALID_CHALLENGE)
    private String challenge;

    @ChallengeFormat
    private String format;

    @ChallengeType
    private String type;
}
