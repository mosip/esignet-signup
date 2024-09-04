/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class RestResponseWrapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String version;
    private String responsetime;
    private String metadata;
    private T response;
    private ArrayList<RestError> errors;
}
