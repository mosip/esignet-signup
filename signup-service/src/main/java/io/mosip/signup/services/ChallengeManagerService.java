package io.mosip.signup.services;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.ApiChallengeRequest;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.dto.RestRequestWrapper;
import io.mosip.signup.dto.RestResponseWrapper;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Service
@Slf4j
public class ChallengeManagerService {
    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Value("${mosip.signup.generate-challenge.endpoint}")
    private String generateChallengeUrl;

    public String generateChallenge(RegistrationTransaction transaction) throws SignUpException {
        ApiChallengeRequest apiChallengeRequest = new ApiChallengeRequest();
        apiChallengeRequest.setKey(transaction.getChallengeTransactionId());
        RestRequestWrapper<ApiChallengeRequest> restRequest = new RestRequestWrapper<>();
        restRequest.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequest.setRequest(apiChallengeRequest);
        RestResponseWrapper<LinkedHashMap<String, String>> restResponseWrapper = (RestResponseWrapper<LinkedHashMap<String, String>>) selfTokenRestTemplate
                .postForObject(generateChallengeUrl, restRequest, RestResponseWrapper.class);
        if (restResponseWrapper == null) {
            log.error("generate-challenge Failed wrapper returned null");
            throw new SignUpException(ErrorConstants.SEND_CHALLENGE_FAILED);
        }

        if(restResponseWrapper.getErrors() != null) {
            log.error("generate-challenge Failed wrapper returned errors {}!", restResponseWrapper.getErrors());
            throw new SignUpException(ErrorConstants.SEND_CHALLENGE_FAILED);
        }

        String challenge = restResponseWrapper.getResponse().get("otp");
        if (challenge == null || challenge.isEmpty()) {
            log.error("generate-challenge Failed challenge returned null");
            throw new SignUpException(ErrorConstants.SEND_CHALLENGE_FAILED);
        }

        return challenge;
    }
}
