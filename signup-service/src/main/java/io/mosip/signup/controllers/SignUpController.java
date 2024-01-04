package io.mosip.signup.controllers;


import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

import static io.mosip.signup.util.SignUpConstants.EMTPY;

@Slf4j
@RestController
public class SignUpController {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    AuditHelper auditHelper;

    @Value("#{${mosip.signup.ui.config.key-values}}")
    private Map<String, Object> signUpUIConfigMap;

    @GetMapping("/settings")
    public ResponseWrapper<SettingsResponse> getSignUpDetails() {
        ResponseWrapper<SettingsResponse> responseWrapper = new ResponseWrapper<SettingsResponse>();
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        SettingsResponse response = new SettingsResponse();
        response.setConfigs(signUpUIConfigMap);
        responseWrapper.setResponse(response);
        return responseWrapper;
    }

    @PostMapping("/reset-password")
    public ResponseWrapper<RegistrationStatusResponse> resetPassword(@Valid @RequestBody RequestWrapper<ResetPasswordRequest> requestWrapper,
                                                                     @Valid @NotBlank(message = ErrorConstants.INVALID_TRANSACTION)
                                                                     @CookieValue(value = SignUpConstants.VERIFIED_TRANSACTION_ID, defaultValue = EMTPY) String transactionId){
        ResponseWrapper<RegistrationStatusResponse> responseWrapper = new ResponseWrapper<>();
        try{
            responseWrapper.setResponse(registrationService.updatePassword(requestWrapper.getRequest(), transactionId));
        }catch (SignUpException signUpException){
            auditHelper.sendAuditTransaction(AuditEvent.RESET_PASSWORD, AuditEventType.ERROR, transactionId, signUpException);
            throw signUpException;
        }
        auditHelper.sendAuditTransaction(AuditEvent.RESET_PASSWORD, AuditEventType.SUCCESS, transactionId, null);
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }
}
