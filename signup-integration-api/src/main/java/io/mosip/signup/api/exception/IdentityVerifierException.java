package io.mosip.signup.api.exception;

import io.mosip.signup.api.util.ErrorConstants;

public class IdentityVerifierException  extends RuntimeException {

    private String errorCode;

    public IdentityVerifierException() {
        super(ErrorConstants.UNKNOWN_ERROR);
        this.errorCode = ErrorConstants.UNKNOWN_ERROR;
    }

    public IdentityVerifierException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
