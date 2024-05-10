package io.mosip.signup.api.exception;

import io.mosip.signup.api.util.ErrorConstants;


public class ProfileException extends RuntimeException {

    private String errorCode;

    public ProfileException() {
        super(ErrorConstants.UNKNOWN_ERROR);
        this.errorCode = ErrorConstants.UNKNOWN_ERROR;
    }

    public ProfileException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
