package io.mosip.signup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuditResponse {

    @JsonProperty("status")
    private boolean status;
}
