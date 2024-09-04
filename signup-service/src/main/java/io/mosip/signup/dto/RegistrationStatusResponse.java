/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import lombok.Data;

@Data
public class RegistrationStatusResponse {

    private ProfileCreateUpdateStatus status;
}
