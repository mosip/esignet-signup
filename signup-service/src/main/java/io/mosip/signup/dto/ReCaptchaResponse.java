package io.mosip.signup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mosip.signup.dto.ReCaptchaError;
import lombok.Data;

import java.util.List;

@Data
public class ReCaptchaResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("errorCodes")
    private List<ReCaptchaError> errorCodes;
}
