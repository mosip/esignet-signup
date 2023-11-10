package io.mosip.signup.controllers;


import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.SettingsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class SignUpSettingsController {

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
}
