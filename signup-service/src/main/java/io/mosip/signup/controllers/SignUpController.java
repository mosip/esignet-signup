package io.mosip.signup.controllers;


import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.RegistrationStatus;
import io.mosip.signup.util.SignUpConstants;
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
        responseWrapper.setResponse(registrationService.updatePassword(requestWrapper.getRequest(), transactionId));
        responseWrapper.setResponseTime(IdentityProviderUtil.getUTCDateTime());
        return responseWrapper;
    }
}
