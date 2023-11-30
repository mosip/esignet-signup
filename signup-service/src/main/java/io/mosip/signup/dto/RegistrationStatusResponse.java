package io.mosip.signup.dto;

import io.mosip.signup.util.RegistrationStatus;
import lombok.Data;

@Data
public class RegistrationStatusResponse {
    private RegistrationStatus status;
}
