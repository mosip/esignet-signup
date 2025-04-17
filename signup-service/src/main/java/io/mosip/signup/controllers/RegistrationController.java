/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import io.mosip.signup.util.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.RestController;

import static io.mosip.signup.util.SignUpConstants.EMTPY;

@Slf4j
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    AuditHelper auditHelper;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if(binder.getTarget() != null && RequestWrapper.class.equals(binder.getTarget().getClass())) {
            RequestWrapper dto = (RequestWrapper) binder.getTarget();
            if(dto.getRequest() != null && RegisterRequest.class.equals(dto.getRequest().getClass())) {
                RegisterRequest registerRequest = (RegisterRequest) dto.getRequest();
                //TODO remove this logic after changes in the UI is done to pass password inside userinfo
                registerRequest.setUserInfo(
                        ((ObjectNode)registerRequest.getUserInfo()).set("password", JsonNodeFactory.instance.textNode(registerRequest.getPassword()))
                );
                ((RequestWrapper) binder.getTarget()).setRequest(registerRequest);
            }
        }
    }


    @PostMapping("/generate-challenge")
    public ResponseWrapper<GenerateChallengeResponse> generateChallenge (@Valid @RequestBody RequestWrapper<GenerateChallengeRequest> requestWrapper,
                                                                         @CookieValue(name = SignUpConstants.TRANSACTION_ID, defaultValue = EMTPY) String transactionId)
            throws SignUpException {
        ResponseWrapper<GenerateChallengeResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        try{
            responseWrapper.setResponse(registrationService.generateChallenge(requestWrapper.getRequest(), transactionId));
        }catch (SignUpException signUpException){
            auditHelper.sendAuditTransaction(AuditEvent.GENERATE_CHALLENGE, AuditEventType.ERROR,
                    transactionId, signUpException);
            throw signUpException;
        }
        auditHelper.sendAuditTransaction(AuditEvent.GENERATE_CHALLENGE, AuditEventType.SUCCESS,
                transactionId, null);
        return responseWrapper;
    }

    @PostMapping("/verify-challenge")
    public ResponseWrapper<VerifyChallengeResponse> verifyChallenge(@Valid @RequestBody RequestWrapper<VerifyChallengeRequest> requestWrapper,
                                                                   @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                                   @CookieValue(value = SignUpConstants.TRANSACTION_ID, defaultValue = EMTPY) String transactionId)
            throws SignUpException {
        ResponseWrapper<VerifyChallengeResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        try{
            responseWrapper.setResponse(registrationService.verifyChallenge(requestWrapper.getRequest(),transactionId));
        }catch (SignUpException signUpException){
            auditHelper.sendAuditTransaction(AuditEvent.VERIFY_CHALLENGE, AuditEventType.ERROR,
                    transactionId, signUpException);
            throw signUpException;
        }
        auditHelper.sendAuditTransaction(AuditEvent.VERIFY_CHALLENGE, AuditEventType.SUCCESS,
                transactionId, null);
        return  responseWrapper;
    }


    @PostMapping("/register")
    public ResponseWrapper<RegisterResponse> register(@Valid @RequestBody RequestWrapper<RegisterRequest> requestWrapper,
                                                      @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                      @CookieValue(value = SignUpConstants.VERIFIED_TRANSACTION_ID, defaultValue = EMTPY) String transactionId)
            throws SignUpException {
        ResponseWrapper<RegisterResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        try {
            responseWrapper.setResponse(registrationService.register(requestWrapper.getRequest(), transactionId));
        }catch (SignUpException signUpException){
            auditHelper.sendAuditTransaction(AuditEvent.REGISTER, AuditEventType.ERROR, transactionId, signUpException);
            throw signUpException;
        }
        auditHelper.sendAuditTransaction(AuditEvent.REGISTER, AuditEventType.SUCCESS, transactionId, null);
        return  responseWrapper;
    }

    @GetMapping("/status")
    public ResponseWrapper<RegistrationStatusResponse> getRegistrationStatus(
            @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
            @CookieValue(value = SignUpConstants.VERIFIED_TRANSACTION_ID, defaultValue = EMTPY) String transactionId) {
        //TODO Need to change the response to List<RegistrationStatusResponse>
        //As it will be easier to give out credential issuance status for each handle type.
        ResponseWrapper<RegistrationStatusResponse> responseWrapper = new ResponseWrapper<>();
        try {
            responseWrapper.setResponse(registrationService.getRegistrationStatus(transactionId));
            responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        }catch (SignUpException signUpException){
            auditHelper.sendAuditTransaction(AuditEvent.REGISTER_STATUS_CHECK, AuditEventType.ERROR,
                    transactionId, signUpException);
            throw signUpException;
        }
        auditHelper.sendAuditTransaction(AuditEvent.REGISTER_STATUS_CHECK, AuditEventType.SUCCESS,
                transactionId, null);
        return responseWrapper;
    }

    @GetMapping("/ui-spec")
    public ResponseWrapper<JsonNode> getUiSpec(
            @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
            @CookieValue(value = SignUpConstants.VERIFIED_TRANSACTION_ID, defaultValue = EMTPY) String transactionId) {
        ResponseWrapper<JsonNode> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        responseWrapper.setResponse(registrationService.getSchema(transactionId));
        return responseWrapper;
    }

}
