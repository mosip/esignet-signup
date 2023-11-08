package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.ChallengeInfo;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.dto.VerifyChallengeRequest;
import io.mosip.signup.dto.VerifyChallengeResponse;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.services.CacheUtilService;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.ActionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(value = RegistrationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class RegistrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegistrationService registrationService;

    @MockBean
    CacheUtilService cacheUtilService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void verifyChallenge_thenPass() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";

        VerifyChallengeResponse verifyChallengeResponse = new VerifyChallengeResponse();
        verifyChallengeResponse.setStatus(ActionStatus.SUCCESS);

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID))
                .thenReturn(verifyChallengeResponse);

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("SUCCESS"));
    }

    @Test
    public void verifyChallenge_withInvalidChallenge_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("1234567");
        challengeInfo.setFormat("alpha-numeric");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_challenge"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.challenge: invalid_challenge"));
    }

    @Test
    public void verifyChallenge_withInvalidChallenge_ChallengeSizeMoreThen6_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_challenge"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.challenge: invalid_challenge"));
    }

    @Test
    public void verifyChallenge_withInvalidChallengeFormat_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_challenge_format"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.format: invalid_challenge_format"));
    }

    @Test
    public void verifyChallenge_withInvalidChallengeFormat_allowlist_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("sms");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_challenge_format"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.format: invalid_challenge_format"));

    }

    @Test
    public void verifyChallenge_withInvalidTimestamp_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_request"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("requestTime: invalid_request"));

    }

    @Test
    public void verifyChallenge_withLongIntervalTimestamp_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime("2022-11-08T02:46:51.160Z");
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_request"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("requestTime: invalid_request"));

    }

    @Test
    public void verifyChallenge_withInvalidChallengeInfo_returnErrorResponse() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie("TRANSACTION_ID", mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(wrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_challenge_info"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo: invalid_challenge_info"));

    }

    @Test
    public void verifyChallenge_withoutIdentifier_returnErrorResponse() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("mock");
        registrationTransaction.setIdentifier("mock");

        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionID)).thenReturn(null);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_identifier"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.identifier: invalid_identifier"));
    }

    @Test
    public void verifyChallenge_withInvalidTransaction_returnErrorResponse() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("mock");
        registrationTransaction.setIdentifier("mock");

        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionID)).thenReturn(null);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidTransactionException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_transaction"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("invalid_transaction"));
    }

    @Test
    public void verifyChallenge_withChallengeFailedException_returnErrorResponse() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("mock");
        registrationTransaction.setIdentifier("mock");

        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionID)).thenReturn(null);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new ChallengeFailedException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("challenge_failed"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("challenge_failed"));
    }

    @Test
    public void verifyChallenge_withInvalidIdentifierException_returnErrorResponse() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("mock");
        registrationTransaction.setIdentifier("mock");

        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionID)).thenReturn(null);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value("invalid_identifier"))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("invalid_identifier"));
    }

    @Test
    public void verifyChallenge_withMultipleInvalidRequest_returnErrorResponse() throws Exception {
        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("8551212312");

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        RequestWrapper<VerifyChallengeRequest> wrapper = new RequestWrapper<>();
        wrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("mock");
        registrationTransaction.setIdentifier("mock");

        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionID)).thenReturn(null);
        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidIdentifierException());

        mockMvc.perform(post("/registration/verify-challenge")
                        .content(objectMapper.writeValueAsString(wrapper))
                        .cookie(new Cookie("TRANSACTION_ID", mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }
}
