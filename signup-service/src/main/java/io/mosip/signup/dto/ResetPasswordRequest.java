/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.validator.Identifier;
import io.mosip.signup.validator.Language;
import io.mosip.signup.validator.Password;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @Identifier
    private String identifier;

    @Password
    private String password;

    @Language(required = false)
    private String locale;
}
