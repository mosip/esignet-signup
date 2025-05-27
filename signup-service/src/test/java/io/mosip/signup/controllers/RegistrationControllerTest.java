/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.exception.EsignetException;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.api.dto.ProfileDto;
import io.mosip.signup.api.exception.InvalidProfileException;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.config.SecurityConfig;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.*;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.CacheUtilService;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import static io.mosip.esignet.core.constants.Constants.UTC_DATETIME_PATTERN;
import static io.mosip.signup.util.ErrorConstants.INVALID_USERINFO;
import static io.mosip.signup.util.ErrorConstants.INVALID_USERNAME;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(value = RegistrationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}),
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class RegistrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegistrationService registrationService;

    @MockBean
    CacheUtilService cacheUtilService;

    @MockBean
    AuditHelper auditHelper;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    ProfileRegistryPlugin profileRegistryPlugin;

    ObjectMapper objectMapper = new ObjectMapper();

    private GenerateChallengeRequest generateChallengeRequest;
    private VerifyChallengeRequest verifyChallengeRequest;
    private RequestWrapper verifyRequestWrapper;
    private RequestWrapper wrapper;

    private String locale;

    @Before
    public void init() {
        generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        generateChallengeRequest.setIdentifier("+85577410541");
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);
        wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        wrapper.setRequest(generateChallengeRequest);



        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("+85512345678");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        verifyRequestWrapper = new RequestWrapper<>();
        verifyRequestWrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        verifyRequestWrapper.setRequest(verifyChallengeRequest);
    }
    @Test
    public void doVerifyChallenge_thenPass() throws Exception {
        String mockTransactionID = "123456789";
        VerifyChallengeResponse verifyChallengeResponse = new VerifyChallengeResponse(ActionStatus.SUCCESS);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenReturn(verifyChallengeResponse);

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value(ActionStatus.SUCCESS));
    }

    @Test
    public void doVerifyChallenge_withInvalidChallengeType_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setType(null);

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionID = "123456789";
        VerifyChallengeResponse verifyChallengeResponse = new VerifyChallengeResponse(ActionStatus.SUCCESS);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenReturn(verifyChallengeResponse);

        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);
        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_TYPE));
    }

    @Test
    public void doVerifyChallenge_withInvalidChallenge_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge(null);
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setType("OTP");
        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);
        verifyChallengeRequest.setChallengeInfo(challengeList);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.challengeInfo[0].challenge: invalid_challenge"));
    }

    @Test
    public void doVerifyChallenge_withChallengeSizeMoreThen6_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("1234567");
        challengeInfo.setType("OTP");
        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);
        verifyChallengeRequest.setChallengeInfo(challengeList);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        when(registrationService.verifyChallenge(any(), any())).thenThrow(new SignUpException(ErrorConstants.INVALID_CHALLENGE));

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value(ErrorConstants.INVALID_CHALLENGE));
    }
    @Test
    public void doVerifyChallenge_withInvalidChallengeFormat_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat(null);
        challengeInfo.setChallenge("111111");
        challengeInfo.setType("OTP");
        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);
        verifyChallengeRequest.setChallengeInfo(challengeList);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_FORMAT))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.challengeInfo[0].format: invalid_challenge_format"));
    }

    @Test
    public void doVerifyChallenge_withBlankChallengeFormat_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("");
        challengeInfo.setChallenge("111111");
        challengeInfo.setType("OTP");
        ArrayList<ChallengeInfo> challengeInfoArrayList = new ArrayList<>();
        challengeInfoArrayList.add(challengeInfo);
        verifyChallengeRequest.setChallengeInfo(challengeInfoArrayList);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_FORMAT))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.challengeInfo[0].format: invalid_challenge_format"));
    }

    @Test
    public void doVerifyChallenge_withChallengeFormatNotInAllowlist_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("sms");
        challengeInfo.setType("OTP");
        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);
        verifyChallengeRequest.setChallengeInfo(challengeList);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_FORMAT))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.challengeInfo[0].format: invalid_challenge_format"));

    }

    @Test
    public void doVerifyChallenge_withRequestTimeNull_returnErrorResponse() throws Exception {
        verifyRequestWrapper.setRequestTime(null);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_REQUEST))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("requestTime: invalid_request"));

    }

    @Test
    public void doVerifyChallenge_withPastRequestTime_returnErrorResponse() throws Exception {
        verifyRequestWrapper.setRequestTime("2022-11-08T02:46:51.160Z");

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_REQUEST))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("requestTime: invalid_request"));

    }

    @Test
    public void doVerifyChallenge_withInvalidChallengeInfo_returnErrorResponse() throws Exception {
        verifyChallengeRequest.setChallengeInfo(null);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_INFO))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.challengeInfo: invalid_challenge_info"));

    }

    @Test
    public void doVerifyChallenge_withoutIdentifier_returnErrorResponse() throws Exception {
        verifyChallengeRequest.setIdentifier(null);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER))
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.identifier: invalid_identifier"));
    }

    @Test
    public void doVerifyChallenge_BlankIdentifier_returnErrorResponse() throws Exception {
        verifyChallengeRequest.setIdentifier("");
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.identifier: invalid_identifier"));
    }

    @Test
    public void doVerifyChallenge_withInvalidTransaction_returnErrorResponse() throws Exception {
        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidTransactionException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_TRANSACTION))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ErrorConstants.INVALID_TRANSACTION));
    }

    @Test
    public void doVerifyChallenge_withVerifyChallengeRaiseChallengeFailedException_returnErrorResponse() throws Exception {
        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123128", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenThrow(new ChallengeFailedException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.CHALLENGE_FAILED))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ErrorConstants.CHALLENGE_FAILED));
    }

    @Test
    public void doVerifyChallenge_withVerifyChallengeRaiseInvalidIdentifierException_returnErrorResponse() throws Exception {
        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123128", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ErrorConstants.INVALID_IDENTIFIER));
    }

    @Test
    public void doVerifyChallenge_withMultipleInvalidRequest_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setType("OTP");
        challengeInfo.setChallenge(null);

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        verifyChallengeRequest.setChallengeInfo(challengeList);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);
        verifyRequestWrapper.setRequestTime(null);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123128", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    // Generate Challenge OTP test cases
    @Test
    public void doGenerateChallenge_thenPass() throws Exception {
        String status = "SUCCESSFUL";
        GenerateChallengeResponse generateChallengeResponse = new GenerateChallengeResponse(status);
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenReturn(generateChallengeResponse);

        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").isNotEmpty())
                .andExpect(jsonPath("$.response.status").value(status))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    public void doGenerateChallenge_withInvalidIdentifier_returnErrorResponse() throws Exception {
        generateChallengeRequest.setIdentifier("77410541");
        wrapper.setRequest(generateChallengeRequest);
        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER));
    }

    @Test
    public void doGenerateChallenge_withGenerateChallengeRaiseInvalidIdentifier_returnErrorResponse() throws Exception {
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER));
    }

    @Test
    public void doGenerateChallenge_withInvalidCaptchaToken_returnErrorResponse() throws Exception {
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenThrow(new EsignetException(ErrorConstants.INVALID_CAPTCHA));
        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CAPTCHA))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(ErrorConstants.INVALID_CAPTCHA));
    }

    @Test
    public void doGenerateChallenge_withRegistrationPurpose_thenPass() throws Exception {
        String status = "SUCCESSFUL";
        GenerateChallengeResponse generateChallengeResponse = new GenerateChallengeResponse(status);

        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenReturn(generateChallengeResponse);

        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").isNotEmpty())
                .andExpect(jsonPath("$.response.status").value(status))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    public void doGenerateChallenge_withNullPurpose_returnErrorResponse() throws Exception {
        String status = "SUCCESSFUL";
        GenerateChallengeResponse generateChallengeResponse = new GenerateChallengeResponse(status);
        generateChallengeRequest.setPurpose(null);
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenReturn(generateChallengeResponse);

        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_PURPOSE));
    }

    @Test
    public void doGenerateChallenge_withResetPasswordPurpose_thenPass() throws Exception {
        String status = "SUCCESSFUL";
        GenerateChallengeResponse generateChallengeResponse = new GenerateChallengeResponse(status);

        generateChallengeRequest.setPurpose(Purpose.RESET_PASSWORD);
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenReturn(generateChallengeResponse);

        mockMvc.perform(post("/registration/generate-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").isNotEmpty())
                .andExpect(jsonPath("$.response.status").value(status))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    public void doGenerateChallenge_withInvalidPurpose_thenFail() throws Exception {
        String status = "SUCCESSFUL";
        GenerateChallengeResponse generateChallengeResponse = new GenerateChallengeResponse(status);

        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        when(registrationService.generateChallenge(generateChallengeRequest, ""))
                .thenReturn(generateChallengeResponse);

        String requestBody = objectMapper.writeValueAsString(wrapper);
        requestBody = requestBody.replace("REGISTRATION", "Invalid-purpose");

        mockMvc.perform(post("/registration/generate-challenge")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_REQUEST));
    }

    @Test
    public void doGetRegistrationStatus_returnCompletedResponse() throws Exception {
        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        RegistrationStatusResponse response = new RegistrationStatusResponse();
        response.setStatus(registrationTransaction.getRegistrationStatus());

        when(registrationService.getRegistrationStatus(mockTransactionID)).thenReturn(response);
        mockMvc.perform(get("/registration/status")
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("COMPLETED"));
    }

    @Test
    public void doGetRegistrationStatus_returnPendingResponse() throws Exception {
        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.PENDING);
        RegistrationStatusResponse response = new RegistrationStatusResponse();
        response.setStatus(registrationTransaction.getRegistrationStatus());

        when(registrationService.getRegistrationStatus(mockTransactionID)).thenReturn(response);
        mockMvc.perform(get("/registration/status")
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("PENDING"));
    }

    @Test
    public void doGetRegistrationStatus_returnFailedResponse() throws Exception {
        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.FAILED);
        RegistrationStatusResponse response = new RegistrationStatusResponse();
        response.setStatus(registrationTransaction.getRegistrationStatus());

        when(registrationService.getRegistrationStatus(mockTransactionID)).thenReturn(response);
        mockMvc.perform(get("/registration/status")
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("FAILED"));
    }

    //  Register endpoint
    @Test
    public void register_thenPass() throws Exception{

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test")
                .put("password", "Password@2023"));
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("Password@2023");
        registerRequest.setConsent("AGREE");

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(eq(registerRequest), eq(mockTransactionID))).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("PENDING"));
    }

    @Test
    public void register_withNullConsent_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("Password@2023");

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.consent: invalid_consent"));
    }

    @Test
    public void register_withUnsupportedConsent_returnErrorResponse() throws Exception{

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("Password@2023");
        registerRequest.setConsent("not agree");

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.consent: invalid_consent"));
    }


    @Test
    public void register_withInvalidPassword_returnErrorResponse() throws Exception{

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12345678");
        registerRequest.setConsent("AGREE");
        registerRequest.setLocale(locale);

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(registerRequest, mockTransactionID)).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.password: invalid_password"));
    }

    @Test
    public void register_withBlankPassword_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setUsername("+85512345678");
        registerRequest.setConsent("AGREE");
        registerRequest.setPassword("");
        registerRequest.setLocale(locale);


        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(registerRequest, mockTransactionID)).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.password: invalid_password"));
    }

    @Test
    public void register_withNullPassword_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setUsername("+85512345678");
        registerRequest.setConsent("AGREE");
        registerRequest.setLocale(locale);


        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(registerRequest, mockTransactionID)).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage")
                        .value("request.password: invalid_password"));
    }

    @Test
    public void register_withBlankUsername_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setConsent("AGREE");
        registerRequest.setPassword("Password@2023");
        registerRequest.setUsername("");
        registerRequest.setLocale(locale);

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(registerRequest, mockTransactionID)).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.username: invalid_username"));
    }

    @Test
    public void register_withNullUsername_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setConsent("AGREE");
        registerRequest.setPassword("Password@2023");
        registerRequest.setLocale(locale);

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(registerRequest, mockTransactionID)).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode")
                        .value(INVALID_USERNAME));
    }

    @Test
    public void register_withNotMatchUsernameRegex_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode().put("name", "Test"));
        registerRequest.setConsent("AGREE");
        registerRequest.setUsername("+85502345678");
        registerRequest.setPassword("Password@2023");
        registerRequest.setLocale(locale);

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setStatus(ActionStatus.PENDING);
        when(registrationService.register(registerRequest, mockTransactionID)).thenReturn(registerResponse);

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(
                        ErrorConstants.INVALID_USERNAME));
    }

    @Test
    @Ignore //ignored becoz of initbinder in registrationController
    public void register_withNullUserInfo_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(null);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("Password@2023");
        registerRequest.setConsent("AGREE");

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode")
                        .value(INVALID_USERINFO));
    }

    @Test
    @Ignore //ignored becoz of initbinder in registrationController
    public void register_withEmptyUserInfo_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("Password@2023");
        registerRequest.setConsent("AGREE");

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        String mockTransactionID = "123456789";

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(INVALID_USERINFO));
    }

    @Test
    public void register_withInvalidUserInfo_returnErrorResponse() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest();
        JsonNode jsonNode = objectMapper.createObjectNode().put("name", "Test");
        registerRequest.setUserInfo(jsonNode);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("Password@2023");
        registerRequest.setConsent("AGREE");

        RequestWrapper<RegisterRequest> wrapper = new RequestWrapper<RegisterRequest>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(registerRequest);

        doThrow(new InvalidProfileException("invalid_input")).when(profileRegistryPlugin).validate(anyString(), any(ProfileDto.class));

        String mockTransactionID = "123456789";

        mockMvc.perform(post("/registration/register")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_input"));
    }
}
