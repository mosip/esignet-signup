package io.mosip.signup.services;

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
        switch (supportedGenerateChallengeType) {
            case "OTP" :
                return generateOTPChallenge(transaction.getChallengeTransactionId());
        }
        throw new SignUpException(ErrorConstants.UNSUPPORTED_CHALLENGE_TYPE);
    }

    private String generateOTPChallenge(String challengeTransactionId) {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setKey(challengeTransactionId);
        RestRequestWrapper<OtpRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequestWrapper.setRequest(otpRequest);

        RestResponseWrapper<OtpResponse> restResponseWrapper = selfTokenRestTemplate
                .exchange(generateChallengeUrl, HttpMethod.POST,
                        new HttpEntity<>(restRequestWrapper),
                        new ParameterizedTypeReference<RestResponseWrapper<OtpResponse>>() {})
                .getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                !StringUtils.isEmpty(restResponseWrapper.getResponse().getOtp()) &&
                !restResponseWrapper.getResponse().getOtp().equals("null")) {
            return restResponseWrapper.getResponse().getOtp();
        }

        log.error("Generate OTP failed with response {}", restResponseWrapper);
        throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.GENERATE_CHALLENGE_FAILED);
    }
}