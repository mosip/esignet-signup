/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.exception;

import io.mosip.esignet.core.exception.EsignetException;
import io.mosip.signup.util.ErrorConstants;

public class SignUpException extends EsignetException {

    public SignUpException(String errorCode){
        super(errorCode);
    }
}
