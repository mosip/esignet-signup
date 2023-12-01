package io.mosip.signup.exception;

public class CaptchaException extends SignUpException {

    public CaptchaException(String errorCode) {
        super(errorCode);
    }

}
