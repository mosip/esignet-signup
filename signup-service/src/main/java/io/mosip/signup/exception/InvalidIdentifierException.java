package io.mosip.signup.exception;

import io.mosip.signup.util.ErrorConstants;

public class InvalidIdentifierException extends SignUpException {

    public InvalidIdentifierException() {
        super(ErrorConstants.INVALID_IDENTIFIER);
    }
}
