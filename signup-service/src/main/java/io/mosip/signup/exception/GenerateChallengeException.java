package io.mosip.signup.exception;

public class GenerateChallengeException extends SignUpException {

    public GenerateChallengeException(String errorCode) {
        super(errorCode);
    }
}
