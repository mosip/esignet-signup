/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.api.dto;


import io.mosip.signup.api.util.VerificationStatus;
import lombok.Data;

import java.util.Map;

@Data
public class VerificationResult {

    private VerificationStatus status;
    private Map<String, VerificationDetail> verifiedClaims;
    private String errorCode;
}
