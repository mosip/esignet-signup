/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Identifier;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class VerifyChallengeRequest {

    @Identifier
    private String identifier;

    @NotEmpty(message = ErrorConstants.INVALID_CHALLENGE_INFO)
    private List<@Valid ChallengeInfo> challengeInfo;
}
