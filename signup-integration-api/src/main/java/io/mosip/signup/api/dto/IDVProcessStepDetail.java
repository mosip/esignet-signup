package io.mosip.signup.api.dto;


import lombok.Data;

import java.util.List;

@Data
public class IDVProcessStepDetail {

    private String code;
    private int framesPerSecond;
    private int durationInSeconds;
    private int startupDelayInSeconds;
    private boolean retryOnTimeout;
    private List<String> retryableErrorCodes;

}
