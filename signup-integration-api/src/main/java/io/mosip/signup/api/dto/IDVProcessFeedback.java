package io.mosip.signup.api.dto;

import io.mosip.signup.api.util.ProcessFeedbackType;
import lombok.Data;

@Data
public class IDVProcessFeedback {

    private ProcessFeedbackType type;
    private String code;
}
