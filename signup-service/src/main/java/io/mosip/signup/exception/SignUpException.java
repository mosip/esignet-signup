package io.mosip.signup.exception;

import io.mosip.esignet.core.exception.EsignetException;

public class SignUpException extends EsignetException {

    public SignUpException(String errorCode){
        super(errorCode);
    }
}
