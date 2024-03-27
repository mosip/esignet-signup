package io.mosip.signup.services;

import io.micrometer.core.annotation.Timed;
import io.mosip.esignet.api.spi.CaptchaValidator;
import io.mosip.signup.dto.ReCaptchaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    @Timed(value = "validatecaptcha.api.timer", percentiles = {0.9})
    @Override
    public boolean validateCaptcha(String captchaToken) {

        if (!requiredCaptcha) return true;

        if(StringUtils.isEmpty(captchaToken))
            return false;

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("secret", verifierSecret);
        param.add("response", captchaToken.trim());

        ReCaptchaResponse reCaptchaResponse = restTemplate.postForObject(captchaVerifyUrl, param,
                ReCaptchaResponse.class);

        if(reCaptchaResponse != null && reCaptchaResponse.isSuccess()) {
            return true;
        }

        log.error("Recaptcha validation failed with errors : {}", reCaptchaResponse != null ?
                reCaptchaResponse.getErrorCodes() : "Response is null");
        return false;
    }
}
