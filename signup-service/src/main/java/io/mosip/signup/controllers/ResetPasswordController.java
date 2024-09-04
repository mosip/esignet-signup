/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.RegistrationStatusResponse;
import io.mosip.signup.dto.ResetPasswordRequest;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.AuditEvent;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static io.mosip.signup.util.SignUpConstants.EMTPY;

@Slf4j
@RestController
public class ResetPasswordController {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    AuditHelper auditHelper;

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
