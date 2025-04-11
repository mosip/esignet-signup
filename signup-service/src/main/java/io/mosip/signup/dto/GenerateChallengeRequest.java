/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.mosip.signup.util.BooleanValidatorDeserializer;
import io.mosip.signup.util.Purpose;
import io.mosip.signup.validator.Identifier;
import io.mosip.signup.validator.Language;
import lombok.Data;

@Data
public class GenerateChallengeRequest {

    @Identifier
    private String identifier;
    private String captchaToken;
    @Language(required = false)
    private String locale;
    @JsonDeserialize(using = BooleanValidatorDeserializer.class)
    private boolean regenerateChallenge;

    @io.mosip.signup.validator.Purpose
    private Purpose purpose;
}
