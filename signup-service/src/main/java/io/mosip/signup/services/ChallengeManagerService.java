/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import io.micrometer.core.annotation.Timed;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class ChallengeManagerService {

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Value("${mosip.signup.generate-challenge.endpoint}")
    private String generateChallengeUrl;

    @Value("${mosip.signup.supported.generate-challenge-type:OTP}")
    private String supportedGenerateChallengeType;


    public String generateChallenge(RegistrationTransaction transaction) throws SignUpException {
        if (supportedGenerateChallengeType.equals("OTP")) {
            return generateOTPChallenge(transaction.getChallengeTransactionId());
        }
        throw new SignUpException(ErrorConstants.UNSUPPORTED_CHALLENGE_TYPE);
    }

    @Timed(value = "generateotp.api.timer", percentiles = {0.9})
    private String generateOTPChallenge(String challengeTransactionId) {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setKey(challengeTransactionId);
        RestRequestWrapper<OtpRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequestWrapper.setRequest(otpRequest);

        try {
            RestResponseWrapper<OtpResponse> restResponseWrapper = selfTokenRestTemplate
                    .exchange(generateChallengeUrl, HttpMethod.POST,
                            new HttpEntity<>(restRequestWrapper),
                            new ParameterizedTypeReference<RestResponseWrapper<OtpResponse>>() {
                            })
                    .getBody();

            if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                    !StringUtils.isEmpty(restResponseWrapper.getResponse().getOtp()) &&
                    !restResponseWrapper.getResponse().getOtp().equals("null")) {
                return restResponseWrapper.getResponse().getOtp();
            }

            log.error("Generate OTP failed with response {}", restResponseWrapper);
            throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                    restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.GENERATE_CHALLENGE_FAILED);
        } catch (RestClientException e) {
            log.error("Endpoint {} is unreachable.", generateChallengeUrl);
            throw new SignUpException(ErrorConstants.SERVER_UNREACHABLE);
        }
    }
}
