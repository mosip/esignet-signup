package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.IdentityVerificationService;
import io.mosip.signup.util.AuditEvent;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static io.mosip.signup.util.SignUpConstants.EMTPY;

@Slf4j
@RestController
@RequestMapping("/identity-verification")
public class IdentityVerificationController {

    @Autowired
    AuditHelper auditHelper;

    @Autowired
    IdentityVerificationService identityVerificationService;

    @PostMapping("/initiate")
    public ResponseWrapper<InitiateIdentityVerificationResponse> initiateIdentityVerification(
            @Valid @RequestBody RequestWrapper<InitiateIdentityVerificationRequest> requestWrapper,
            HttpServletResponse response){
        ResponseWrapper<InitiateIdentityVerificationResponse> responseWrapper = new ResponseWrapper<>();
        try{
            responseWrapper.setResponse(identityVerificationService.initiateIdentityVerification(requestWrapper.getRequest(), response));
        }catch (SignUpException signUpException){
            auditHelper.sendAuditTransaction(AuditEvent.INITIATE_IDENTITY_VERIFICATION, AuditEventType.ERROR, null, signUpException);
            throw signUpException;
        }
        auditHelper.sendAuditTransaction(AuditEvent.INITIATE_IDENTITY_VERIFICATION, AuditEventType.SUCCESS, null, null);
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }

    @GetMapping("/identity-verifier/{id}")
    public ResponseWrapper<JsonNode> getIdentityVerifierDetails(@PathVariable String id,
                                             @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                 @CookieValue(value = SignUpConstants.IDV_TRANSACTION_ID, defaultValue = EMTPY) String transactionId) {
        ResponseWrapper<JsonNode> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(identityVerificationService.getIdentityVerifierDetails(transactionId, id));
        auditHelper.sendAuditTransaction(AuditEvent.IDENTITY_VERIFIER, AuditEventType.SUCCESS, transactionId, null);
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }

    @PostMapping("/slot")
    public ResponseWrapper<SlotResponse> getSlot(@Valid @RequestBody RequestWrapper<SlotRequest> requestWrapper,
                                                 @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                 @CookieValue(value = SignUpConstants.IDV_TRANSACTION_ID, defaultValue = EMTPY) String transactionId) {
        ResponseWrapper<SlotResponse> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(identityVerificationService.getSlot(transactionId, requestWrapper.getRequest()));
        auditHelper.sendAuditTransaction(AuditEvent.IDENTITY_VERIFICATION_SLOT, AuditEventType.SUCCESS, transactionId, null);
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }
}
