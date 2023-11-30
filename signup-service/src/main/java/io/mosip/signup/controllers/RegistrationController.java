package io.mosip.signup.controllers;


import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.RegistrationStatusResponse;
import io.mosip.signup.dto.VerifyChallengeRequest;
import io.mosip.signup.dto.VerifyChallengeResponse;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    RegistrationService registrationService;

    @PostMapping("/verify-challenge")
    public ResponseWrapper<VerifyChallengeResponse> verifyChallenge(@Valid @RequestBody RequestWrapper<VerifyChallengeRequest> requestWrapper,
                                                                    @CookieValue(SignUpConstants.TRANSACTION_ID) String transactionId)
            throws SignUpException {
        ResponseWrapper<VerifyChallengeResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        responseWrapper.setResponse(registrationService.verifyChallenge(requestWrapper.getRequest(),transactionId));
        return  responseWrapper;
    }

    @GetMapping("/status")
    public ResponseWrapper<RegistrationStatusResponse> getRegistrationStatus(
            @CookieValue(SignUpConstants.TRANSACTION_ID) String transactionId) {
        ResponseWrapper<RegistrationStatusResponse> responseWrapper = new ResponseWrapper<RegistrationStatusResponse>();
        responseWrapper.setResponse(registrationService.getRegistrationStatus(transactionId));
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }
}
