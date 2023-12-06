package io.mosip.signup.services;

import io.mosip.signup.dto.OtpResponse;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.dto.RestError;
import io.mosip.signup.dto.RestResponseWrapper;
import io.mosip.signup.exception.SignUpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private String generateChallengeUrl = "https://api.net/v1/otpmanager/otp/generate";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(challengeManagerService, "generateChallengeUrl", generateChallengeUrl);
        ReflectionTestUtils.setField(challengeManagerService, "challengeType", "OTP");
    }


    @Test
    public void doGenerateChallenge_allValid_thenPass() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541");
        RestResponseWrapper<OtpResponse> challengeResponse = new RestResponseWrapper<>();
        OtpResponse otpResponse = new OtpResponse();
        otpResponse.setOtp("1111");
        challengeResponse.setResponse(otpResponse);

        when(selfTokenRestTemplate.exchange(
                eq(generateChallengeUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(challengeResponse, HttpStatus.OK));

        String challenge = challengeManagerService.generateChallenge(transaction);
        Assert.assertEquals(challenge, "1111");
    }

    @Test
    public void doGenerateChallenge_withApiResponseEmptyChallenge_thenFail() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541");
        RestResponseWrapper<OtpResponse> challengeResponse = new RestResponseWrapper<>();
        OtpResponse otpResponse = new OtpResponse();
        otpResponse.setOtp("");
        challengeResponse.setResponse(otpResponse);

        when(selfTokenRestTemplate.exchange(
                eq(generateChallengeUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(challengeResponse, HttpStatus.OK));

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("generate_challenge_failed", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withApiNullResponse_thenFail() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541");
        when(selfTokenRestTemplate.exchange(
                eq(generateChallengeUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity(null, HttpStatus.OK));

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("generate_challenge_failed", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withApiResponseErrors_thenFail() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541");
        ArrayList<RestError> errors= new ArrayList<RestError>();
        errors.add(new RestError("401", "401"));
        RestResponseWrapper<OtpResponse> challengeResponse = new RestResponseWrapper<>();
        challengeResponse.setErrors(errors);

        when(selfTokenRestTemplate.exchange(
                eq(generateChallengeUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(challengeResponse, HttpStatus.OK));

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("401", ex.getErrorCode());
        }
    }
}
