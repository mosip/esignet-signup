package io.mosip.signup.exception;

import io.mosip.esignet.core.exception.EsignetException;
import io.mosip.signup.util.ErrorConstants;

public class SignUpException extends EsignetException {

    public SignUpException() {super(ErrorConstants.UNKNOWN_ERROR);}

    public SignUpException(String errorCode){
        super(errorCode);
    }
}
