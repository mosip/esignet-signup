package io.mosip.signup.api.exception;

public class InvalidProfileException extends ProfileException{

    public InvalidProfileException(String errorCode) {
        super(errorCode);
    }
}
