/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
