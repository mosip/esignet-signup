/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.*;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequest {

    @Identifier
    private String username;

    @Password
    private String password;

    @NotBlank(message = ErrorConstants.INVALID_CONSENT)
    @Pattern(message = ErrorConstants.INVALID_CONSENT, regexp = "^(DISAGREE)|(AGREE)$")
    private String consent;

    @UserInfo
    private JsonNode userInfo;

    @Language(required = false)
    private String locale;
}
