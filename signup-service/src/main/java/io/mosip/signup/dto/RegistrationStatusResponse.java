package io.mosip.signup.dto;

import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import lombok.Data;

@Data
public class RegistrationStatusResponse {

    private ProfileCreateUpdateStatus status;
}
