package io.mosip.signup.exception;

import io.mosip.signup.util.ErrorConstants;

public class ChallengeFailedException extends SignUpException {

    public ChallengeFailedException() {
        super(ErrorConstants.CHALLENGE_FAILED);
    }
}
