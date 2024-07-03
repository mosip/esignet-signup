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