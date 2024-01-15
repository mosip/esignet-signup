package io.mosip.signup.exception;

import io.mosip.esignet.core.constants.ErrorConstants;

public class InvalidTransactionException extends SignUpException {

    public InvalidTransactionException() {
        super(ErrorConstants.INVALID_TRANSACTION);
    }
}
