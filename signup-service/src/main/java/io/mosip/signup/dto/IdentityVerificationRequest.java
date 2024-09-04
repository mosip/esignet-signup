/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.api.dto.FrameDetail;
import io.mosip.signup.util.ErrorConstants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class IdentityVerificationRequest {

    @NotBlank(message = ErrorConstants.INVALID_SLOT_ID)
    private String slotId;

    private String stepCode;
    private List<FrameDetail> frames;
}