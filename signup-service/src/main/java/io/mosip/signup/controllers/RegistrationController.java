package io.mosip.signup.controllers;

import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.RestController;

import static io.mosip.signup.util.SignUpConstants.EMTPY;

@Slf4j
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    RegistrationService registrationService;

    @PostMapping("/generate-challenge")
    public ResponseWrapper<GenerateChallengeResponse> generateChallenge (@Valid @RequestBody RequestWrapper<GenerateChallengeRequest> requestWrapper,
                                                                         @CookieValue(name = SignUpConstants.TRANSACTION_ID, defaultValue = EMTPY) String transactionId)
            throws SignUpException {
        ResponseWrapper<GenerateChallengeResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(registrationService.generateChallenge(requestWrapper.getRequest(), transactionId));
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }

    @PostMapping("/verify-challenge")
    public ResponseWrapper<VerifyChallengeResponse> verifyChallenge(@Valid @RequestBody RequestWrapper<VerifyChallengeRequest> requestWrapper,
                                                                   @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                                   @CookieValue(value = SignUpConstants.TRANSACTION_ID, defaultValue = EMTPY) String transactionId)
            throws SignUpException {
        ResponseWrapper<VerifyChallengeResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        responseWrapper.setResponse(registrationService.verifyChallenge(requestWrapper.getRequest(),transactionId));
        return  responseWrapper;
    }

    @PostMapping("/register")
    public ResponseWrapper<RegisterResponse> register(@Valid @RequestBody RequestWrapper<RegisterRequest> requestWrapper,
                                                      @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                      @CookieValue(value = SignUpConstants.VERIFIED_TRANSACTION_ID, defaultValue = EMTPY) String transactionId)
            throws SignUpException {
        ResponseWrapper<RegisterResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(registrationService.register(requestWrapper.getRequest(), transactionId));
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return  responseWrapper;
    }

    @GetMapping("/status")
    public ResponseWrapper<RegistrationStatusResponse> getRegistrationStatus(
            @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
            @CookieValue(value = SignUpConstants.VERIFIED_TRANSACTION_ID, defaultValue = EMTPY) String transactionId) {
        ResponseWrapper<RegistrationStatusResponse> responseWrapper = new ResponseWrapper<RegistrationStatusResponse>();
        responseWrapper.setResponse(registrationService.getRegistrationStatus(transactionId));
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }
}
