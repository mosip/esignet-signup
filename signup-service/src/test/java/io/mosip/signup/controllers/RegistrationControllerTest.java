package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.CaptchaException;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import static io.mosip.esignet.core.constants.Constants.UTC_DATETIME_PATTERN;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    ObjectMapper objectMapper = new ObjectMapper();

    private GenerateChallengeRequest generateChallengeRequest;
    private VerifyChallengeRequest verifyChallengeRequest;
    private RequestWrapper verifyRequestWrapper;
    private RequestWrapper wrapper;

    @Before
    public void init() {
        generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        generateChallengeRequest.setIdentifier("+85577410541");
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);
        wrapper = new RequestWrapper<>();
        wrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        wrapper.setRequest(generateChallengeRequest);

        verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("+85512123128");
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);
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
    public void doVerifyChallenge_withInvalidChallenge_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("1234567");
        challengeInfo.setFormat("alpha-numeric");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.challenge: invalid_challenge"));
    }

    @Test
    public void doVerifyChallenge_withChallengeSizeMoreThen6_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("1111111");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.challenge: invalid_challenge"));
    }

    @Test
    public void doVerifyChallenge_withInvalidChallengeFormat_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat(null);
        challengeInfo.setChallenge("111111");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_FORMAT))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.format: invalid_challenge_format"));
    }

    @Test
    public void doVerifyChallenge_withChallengeFormatNotInAllowlist_returnErrorResponse() throws Exception {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setChallenge("111111");
        challengeInfo.setFormat("sms");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);

        String mockTransactionID = "123456789";
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID);

        mockMvc.perform(post("/registration/verify-challenge").cookie(cookie)
                        .content(objectMapper.writeValueAsString(verifyRequestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_CHALLENGE_FORMAT))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo.format: invalid_challenge_format"));

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("requestTime: invalid_request"));

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("requestTime: invalid_request"));

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.challengeInfo: invalid_challenge_info"));

    }

    @Test
    public void doVerifyChallenge_withoutIdentifier_returnErrorResponse() throws Exception {
        verifyChallengeRequest.setIdentifier(null);
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

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new ChallengeFailedException());

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

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidIdentifierException());

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
        verifyChallengeRequest.setChallengeInfo(challengeInfo);
        verifyRequestWrapper.setRequest(verifyChallengeRequest);
        verifyRequestWrapper.setRequestTime(null);

        String mockTransactionID = "123456789";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123128", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("mock");
        registrationTransaction.setIdentifier("mock");

        when(registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionID)).thenThrow(new InvalidIdentifierException());

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
                .thenThrow(new CaptchaException(ErrorConstants.INVALID_CAPTCHA));
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
        registrationTransaction.setRegistrationStatus(RegistrationStatus.COMPLETED);
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
        registrationTransaction.setRegistrationStatus(RegistrationStatus.PENDING);
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
        registrationTransaction.setRegistrationStatus(RegistrationStatus.FAILED);
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

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("12312399a");
        registerRequest.setConsent("AGREE");

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
                .andExpect(jsonPath("$.response.status").value("PENDING"));
    }

    @Test
    public void register_withBlankConsent_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ"));
        userInfo.setFullName(fullNames);
        userInfo.setPhone("+85512345678");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12312333a");

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.consent: invalid_consent"));
    }

    @Test
    public void register_withUnsupportedConsent_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("12312388a");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.consent: invalid_consent"));
    }

    @Test
    public void register_withInvalidPhoneNumber_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ")));
        userInfo.setPhone("+8551234567890");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("123123ss");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.userInfo.phone: invalid_phone_number"));
    }

    @Test
    public void register_withBlankPhoneNumber_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ")));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("123123qq");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.userInfo.phone: invalid_phone_number"));
    }

    @Test
    public void register_withBlankPreferredLang_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ")));
        userInfo.setPhone("+855123456789");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12312322a");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.userInfo.preferredLang: unsupported_language"));
    }

    @Test
    public void register_withUnsupportedPreferredLang_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "អាន បញ្ញារិទ្ធ")));
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("usa");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("1231288s3");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.userInfo.preferredLang: unsupported_language"));
    }

    @Test
    public void register_withNullFullName_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12312773");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.userInfo.fullName: invalid_fullname"));
    }

    @Test
    public void register_withInvalidFullNameInKhm_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("khm", "qkITAu9BW5hfiZcLCwPuefQqu6QIthy2J9R")));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("123123123a");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.userInfo.fullName[0]: invalid_fullname"));
    }

    @Test
    public void register_withValidFullNameInKhmAndInvalidFullNameInEng_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "ងន់ ម៉េងលាង"));
        fullNames.add(new LanguageTaggedValue("eng", "qkITAu9BW5hfiZcLCwPuefQqu6QIthy2J9R"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("1231231234a");
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
                .andExpect(jsonPath("$.errors[0].errorMessage").value(
                        "request.userInfo.fullName[1]: invalid_fullname"));
    }

    @Test
    public void register_withInValidFullNameInKhm_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "Mengleang Ngoun"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12312311");
        registerRequest.setConsent("AGREE");

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
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage").value(
                        "request.userInfo.fullName[0]: invalid_fullname"));
    }

    @Test
    public void register_withValidFullName_returnSuccessResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "សុខ សាន្ត"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12312311a");
        registerRequest.setConsent("AGREE");

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
                .andExpect(jsonPath("$.response.status").value("PENDING"));
    }

    @Test
    public void register_withInvalidPassword_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "ងន់ ម៉េងលាង"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("qkITAu9BW5hfiZcLCwPuefQqu6QIthy2J9R");
        registerRequest.setConsent("AGREE");

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.password: invalid_password"));
    }

    @Test
    public void register_withBlankPassword_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "ងន់ ម៉េងលាង"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+85512345678");
        registerRequest.setConsent("AGREE");

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.password: invalid_password"));
    }

    @Test
    public void register_withBlankUsername_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+855123456789");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "ងន់ ម៉េងលាង"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setConsent("AGREE");
        registerRequest.setPassword("12345678");

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value("request.username: invalid_username"));
    }

    @Test
    public void register_withNotMatchUsernameRegex_returnErrorResponse() throws Exception{

        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPhone("+85512345678");
        userInfo.setPreferredLang("khm");
        List<LanguageTaggedValue> fullNames = new ArrayList<>();
        fullNames.add(new LanguageTaggedValue("khm", "Mengleang Ngoun"));
        userInfo.setFullName(fullNames);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setConsent("AGREE");
        registerRequest.setUsername("+85512345678");
        registerRequest.setPassword("12345678");

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
                .andExpect(jsonPath("$.errors[0].errorMessage").value(
                        "request.userInfo.fullName[0]: invalid_fullname"));
    }
}
