/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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

    public static VerificationStatus getVerificationStatus(ProfileCreateUpdateStatus profileCreateUpdateStatus) {
        switch (profileCreateUpdateStatus) {
            case PENDING: return VerificationStatus.UPDATE_PENDING;
            case COMPLETED: return VerificationStatus.COMPLETED;
            case FAILED: return VerificationStatus.FAILED;
        }
        return VerificationStatus.UPDATE_PENDING;
    }
}
