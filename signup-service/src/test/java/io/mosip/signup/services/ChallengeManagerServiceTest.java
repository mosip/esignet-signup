/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import brave.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.Purpose;
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

    ObjectMapper objectMapper = new ObjectMapper();

    private String generateChallengeUrl = "https://api.net/v1/otpmanager/otp/generate";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(challengeManagerService, "generateChallengeUrl", generateChallengeUrl);
        ReflectionTestUtils.setField(challengeManagerService, "supportedGenerateChallengeType", "OTP");
    }

    @Test
    public void doGenerateChallenge_allValid_thenPass() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
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
    public void doGenerateChallenge_withUnsupportedChallengeType_thenFail() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        ReflectionTestUtils.setField(challengeManagerService, "supportedGenerateChallengeType", "TELEGRAM");
        try{
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex){
            Assert.assertEquals("unsupported_challenge_type", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withApiResponseEmptyChallenge_thenFail() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
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
    public void doGenerateChallenge_withApiResponseNullChallenge_thenFail() throws SignUpException, IOException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        RestResponseWrapper<OtpResponse> challengeResponse = objectMapper.readValue("{\"id\":\"string\",\"version\":\"string\",\"responsetime\":\"2023-11-14T10:59:16.574Z\",\"metadata\":null,\"response\":{\"otp\": \"null\"},\"errors\":null}", TypeFactory.defaultInstance().constructParametricType(RestResponseWrapper.class, OtpResponse.class));

        when(selfTokenRestTemplate.exchange(
                eq(generateChallengeUrl),
                eq(HttpMethod.POST),
                any(),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<RestResponseWrapper<OtpResponse>>(challengeResponse, HttpStatus.OK));

        try {
            challengeManagerService.generateChallenge(transaction);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("generate_challenge_failed", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withApiNullResponse_thenFail() throws SignUpException {
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
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
        RegistrationTransaction transaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
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
