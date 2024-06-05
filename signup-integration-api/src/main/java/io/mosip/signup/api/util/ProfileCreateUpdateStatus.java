package io.mosip.signup.api.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public enum ProfileCreateUpdateStatus implements Serializable {
    PENDING,
    COMPLETED,
    FAILED;

    public static List<ProfileCreateUpdateStatus> getEndStatuses() {
        return Arrays.asList(ProfileCreateUpdateStatus.COMPLETED, ProfileCreateUpdateStatus.FAILED);
    }
}
