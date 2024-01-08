package io.mosip.signup.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public enum RegistrationStatus implements Serializable {
    PENDING,
    COMPLETED,
    FAILED;

    public static List<RegistrationStatus> getEndStatuses() {
        return Arrays.asList(RegistrationStatus.COMPLETED, RegistrationStatus.FAILED);
    }
}
