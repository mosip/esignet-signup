package io.mosip.signup.services;

import io.micrometer.core.annotation.Timed;
import io.mosip.esignet.api.spi.CaptchaValidator;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;

@ConditionalOnProperty(value = "mosip.signup.integration.captcha-validator", havingValue = "GoogleRecaptchaValidatorService")
@Component
@Slf4j
public class GoogleRecaptchaValidatorService implements CaptchaValidator {

    @Value("${mosip.signup.send-challenge.captcha-required}")
    private boolean requiredCaptcha;

    @Value("${mosip.signup.captcha-validator.url}")
    private String captchaVerifyUrl;

    @Value("${mosip.signup.captcha-validator.secret}")
    private String verifierSecret;

    @Value("${mosip.signup.captcha.id.validate}")
    public String mosipcaptchaValidateId;

    @Value("${mosip.signup.captcha.version}")
    public String captchaVersion;

    @Value("${mosip.signup.captcha.module}")
    public String captchaModule;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Timed(value = "validatecaptcha.api.timer", percentiles = {0.9})
    @Override
    public boolean validateCaptcha(String captchaToken) {

        // UNOFFICIAL CHANGE: temporarily comment out one line below for development
//        if (!requiredCaptcha) return true;

        if(StringUtils.isEmpty(captchaToken))
            return false;

        CaptchaRequest captchaRequest = new CaptchaRequest();
        captchaRequest.setModuleName(captchaModule);
        captchaRequest.setCaptchaToken(captchaToken.trim());

        RestRequestWrapper<CaptchaRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequestWrapper.setRequest(captchaRequest);
        restRequestWrapper.setVersion(captchaVersion);
        restRequestWrapper.setId(mosipcaptchaValidateId);

        HttpEntity<RestRequestWrapper<CaptchaRequest>> resReq = new HttpEntity<>(restRequestWrapper);
        try {
            RestResponseWrapper<CaptchaResponse> restResponseWrapper = selfTokenRestTemplate.exchange(
                    captchaVerifyUrl,
                    HttpMethod.POST,
                    resReq,
                    new ParameterizedTypeReference<RestResponseWrapper<CaptchaResponse>>() {
                    }).getBody();
            // ALWAYS GET Invalid Captcha
            if (restResponseWrapper != null && restResponseWrapper.getResponse() != null)
                return restResponseWrapper.getResponse().isSuccess();

        } catch (RestClientException ex) {
            log.error("Endpoint {} is unreachable.", captchaVerifyUrl);
            throw new SignUpException(ErrorConstants.SERVER_UNREACHABLE);
        }

        return false;
    }
}
