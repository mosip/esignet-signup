package io.mosip.signup.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.dto.RestResponseWrapper;
import io.mosip.signup.exception.SignUpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeManagerServiceTest {
    @InjectMocks
    ChallengeManagerService challengeManagerService;

    @Mock
    RestTemplate selfTokenRestTemplate;

    private RestResponseWrapper challengeResponse;

    ObjectMapper objectMapper = new ObjectMapper();
    private String generateChallengeUrl = "https://api.net/v1/otpmanager/otp/generate";

    @Before
    public void setUp() throws IOException {
        ReflectionTestUtils.setField(
                challengeManagerService, "generateChallengeUrl", generateChallengeUrl);
        challengeResponse = objectMapper.readValue("{\"id\":\"string\",\"version\":\"string\",\"responsetime\":\"2023-11-14T10:59:16.574Z\",\"metadata\":null,\"response\":{\"status\":\"GENERATION_SUCCESSFUL\",\"otp\":\"1111\"},\"errors\":null}".getBytes(), RestResponseWrapper.class);
    }


    @Test
    public void doGenerateChallenge_allValid_thenPass() throws SignUpException, IOException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", "TRAN-ID");
        when(selfTokenRestTemplate.postForObject(eq(generateChallengeUrl), any(), eq(RestResponseWrapper.class)))
                .thenReturn(challengeResponse);

        String challenge = challengeManagerService.generateChallenge(transaction);
        Assert.assertEquals(challenge, "1111");
    }

    @Test
    public void doGenerateChallenge_withApiResponseEmptyChallenge_thenFail() throws SignUpException, IOException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", "TRAN-ID");
        challengeResponse = objectMapper.readValue("{\"id\":\"string\",\"version\":\"string\",\"responsetime\":\"2023-11-14T10:59:16.574Z\",\"metadata\":null,\"response\":{\"status\":\"GENERATION_SUCCESSFUL\",\"otp\":\"\"},\"errors\":null}".getBytes(), RestResponseWrapper.class);
        when(selfTokenRestTemplate.postForObject(eq(generateChallengeUrl), any(), eq(RestResponseWrapper.class)))
                .thenReturn(challengeResponse);

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("send_challenge_failed", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withApiNullResponse_thenFail() throws SignUpException, IOException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", "TRAN-ID");
        when(selfTokenRestTemplate.postForObject(eq(generateChallengeUrl), any(), eq(RestResponseWrapper.class)))
                .thenReturn(null);

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("send_challenge_failed", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withApiResponseErrors_thenFail() throws SignUpException, IOException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", "TRAN-ID");
        ArrayList<Error> errors= new ArrayList<Error>();
        errors.add(new Error("401"));
        challengeResponse.setResponse(null);
        challengeResponse.setErrors(errors);

        when(selfTokenRestTemplate.postForObject(eq(generateChallengeUrl), any(), eq(RestResponseWrapper.class)))
                .thenReturn(challengeResponse);

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("send_challenge_failed", ex.getErrorCode());
        }
    }
}
