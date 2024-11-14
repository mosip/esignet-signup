package io.mosip.signup.helper;

import io.mosip.signup.api.dto.FrameDetail;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.util.ErrorConstants;
import org.apache.kafka.common.errors.InvalidRequestException;

import java.util.List;

public class IdentityVerificationRequestValidator {
    public void validate(IdentityVerificationRequest request) {
        if (request.getStepCode() == null || request.getStepCode().isBlank()) {
            throw new InvalidRequestException(ErrorConstants.INVALID_STEP_CODE);
        }

        List<FrameDetail> frames = request.getFrames();
        if (frames != null && !frames.isEmpty()) {
            for (FrameDetail frame : frames) {
                if (frame.getFrame() == null || frame.getFrame().isBlank()) {
                    throw new InvalidRequestException(ErrorConstants.INVALID_FRAME);
                }
                if (frame.getOrder() <= 0) {
                    throw new InvalidRequestException(ErrorConstants.INVALID_ORDER);
                }
            }
        }
    }
}
