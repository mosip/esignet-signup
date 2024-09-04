/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class IdentityVerifierDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private Map<String, String> displayName;
    private Map<String, String> description;
    private String processType;
    private boolean active;
    private String logoUrl;
    private boolean retryOnFailure;
    private boolean resumeOnSuccess;
    private int processDuration; //in seconds

}
