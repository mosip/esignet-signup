/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
public class SignUpController {

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
