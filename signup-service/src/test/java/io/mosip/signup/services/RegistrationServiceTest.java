package io.mosip.signup.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.util.CaptchaHelper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.api.dto.ProfileDto;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.helper.NotificationHelper;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.Purpose;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.SignUpConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletResponse;

import io.mosip.esignet.core.exception.EsignetException;

import java.time.LocalDateTime;
import java.util.Map;


@RunWith(SpringRunner.class)
@ActiveProfiles(value = {"test"})
public class RegistrationServiceTest {

    @InjectMocks
    RegistrationService registrationService;

    @Mock
    CacheUtilService cacheUtilService;

    @Mock
    RestTemplate selfTokenRestTemplate;

    @Mock
    ChallengeManagerService challengeManagerService;

    @Mock
    HttpServletResponse response;

    @Mock
    NotificationHelper notificationHelper;

    @Mock
    CryptoHelper cryptoHelper;

    @Mock
    ProfileRegistryPlugin profileRegistryPlugin;

    @MockBean
    CaptchaHelper captchaHelper;

    ObjectMapper objectMapper = new ObjectMapper();

    private final String identityEndpoint = "identityEndpoint";
    private final String generateHashEndpoint = "generateHashEndpoint";
    private final String getIdentityEndpoint = "getIdentityEndpoint";
    private final String getUinEndpoint = "getUinEndpoint";

    private final String getRegistrationStatusEndpoint = "getRegistrationStatusEndpoint";

    private String locale = "khm";


    @Before
    public void setUp() {
        ReflectionTestUtils.setField(
                registrationService, "resendAttempts", 3);
        ReflectionTestUtils.setField(
                registrationService, "verificationAttempts", 3);
        ReflectionTestUtils.setField(
                registrationService, "resendDelay", 30);
        ReflectionTestUtils.setField(
                registrationService, "challengeTimeout", 60);
        ReflectionTestUtils.setField(registrationService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(registrationService, "captchaRequired", false);
        ReflectionTestUtils.setField(registrationService, "captchaHelper", captchaHelper);
    }

    @Test
    public void doVerifyChallenge_thenPass() {

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challengeInfo.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);


        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(new RestResponseWrapper<>(), HttpStatus.OK));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(false);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        VerifyChallengeResponse verifyChallengeResponse = registrationService.
                verifyChallenge(verifyChallengeRequest, mockTransactionId);
        Assert.assertNotNull(verifyChallengeResponse);
        Assert.assertEquals("SUCCESS", verifyChallengeResponse.getStatus());
    }

    @Test
    public void doVerifyChallenge_withExceededVerifyAttempt_thenFail() throws SignUpException {
        ReflectionTestUtils.setField(registrationService, "verificationAttempts", 3);
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("failed");
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        registrationTransaction.setVerificationAttempts(4);
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.TOO_MANY_VERIFY_ATTEMPTS, signUpException.getErrorCode());
        }
    }
    @Test
    public void doVerifyChallenge_whenIdentifierAlreadyRegisterError_throwIdentityAlreadyRegister() {

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challengeInfo.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);
        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        restResponseWrapper.setResponse(identityResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ObjectMapper objectMapper = new ObjectMapper();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(true);
        JsonNode jsonNode = objectMapper.valueToTree(identityResponse);
        profileDto.setIdentity(jsonNode);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_already_registered", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_whenResponseNoRecordFromIDRepo_throwIdentifierNotFound() {

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challengeInfo.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        ArrayList<RestError> restErrorArrayList = new ArrayList<>();
        RestError restError = new RestError();
        restError.setErrorCode("IDR-IDC-007");
        restErrorArrayList.add(restError);
        restResponseWrapper.setErrors(restErrorArrayList);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(false);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);


        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_not_found", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_whenErrorFromIDRepo_throwFetchFailed() {

        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challengeInfo.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        ArrayList<RestError> restErrorArrayList = new ArrayList<>();
        RestError restError = new RestError();
        restError.setErrorCode("IDR-IDC-008");
        restErrorArrayList.add(restError);
        restResponseWrapper.setErrors(restErrorArrayList);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(false);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);
        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_not_found", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withOTPMismatchFormat_throwChallengeFormatAndTypeMismatch() {
        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-encoded");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("challenge_format_and_type_mismatch", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withKBIMismatchFormat_throwChallengeFormatAndTypeMismatch() {
        ChallengeInfo challengeInfoKBI = new ChallengeInfo();
        challengeInfoKBI.setFormat("base64url-json");
        challengeInfoKBI.setChallenge("111111");
        challengeInfoKBI.setType("KBI");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBI);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoKBI.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("challenge_format_and_type_mismatch", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withInvalidKBIChallenge_throwInvalidKBIChallenge() {

        ChallengeInfo challengeInfoKBI = new ChallengeInfo();
        challengeInfoKBI.setFormat("base64url-encoded-json");
        challengeInfoKBI.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z==");
        challengeInfoKBI.setType("KBI");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBI);
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse identityResponse = new IdentityResponse();
        Identity identity = new Identity();
        List<LanguageTaggedValue> fullName = new ArrayList<>();
        LanguageTaggedValue fullNameKhm = new LanguageTaggedValue();
        fullNameKhm.setLanguage("khm");
        fullNameKhm.setValue("ងន់ ម៉េងលាង");
        fullName.add(fullNameKhm);
        identity.setFullName(fullName);
        identityResponse.setStatus("ACTIVATED");
        identityResponse.setIdentity(identity);
        restResponseWrapper.setResponse(identityResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ObjectMapper objectMapper = new ObjectMapper();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(true);
        JsonNode jsonNode = objectMapper.valueToTree(identity);
        profileDto.setIdentity(jsonNode);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("invalid_KBI_challenge", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withFullNameMismatch_throwKnowledgeBaseMismatch() {

        ChallengeInfo challengeInfoKBI = new ChallengeInfo();
        challengeInfoKBI.setFormat("base64url-encoded-json");
        challengeInfoKBI.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICJNYW5val9raG0ifV0gfQ==");
        challengeInfoKBI.setType("KBI");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBI);
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse identityResponse = new IdentityResponse();
        Identity identity = new Identity();
        List<LanguageTaggedValue> fullName = new ArrayList<>();
        LanguageTaggedValue fullNameKhm = new LanguageTaggedValue();
        fullNameKhm.setLanguage("khm");
        fullNameKhm.setValue("ហេង ពិទូ");
        fullName.add(fullNameKhm);
        identity.setFullName(fullName);
        identityResponse.setIdentity(identity);
        identityResponse.setStatus("ACTIVATED");
        restResponseWrapper.setResponse(identityResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ObjectMapper objectMapper = new ObjectMapper();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(true);
        JsonNode jsonNode = objectMapper.valueToTree(identity);
        profileDto.setIdentity(jsonNode);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("knowledgebase_mismatch", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_thenSuccess() {

        ChallengeInfo challengeInfoKBI = new ChallengeInfo();
        challengeInfoKBI.setFormat("base64url-encoded-json");
        challengeInfoKBI.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z-J4Z-B4Z6E4Z6b4Z624Z6EIn1dIH0");
        challengeInfoKBI.setType("KBI");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBI);
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse identityResponse = new IdentityResponse();
        Identity identity = new Identity();
        List<LanguageTaggedValue> fullName = new ArrayList<>();
        LanguageTaggedValue fullNameKhm = new LanguageTaggedValue();
        fullNameKhm.setLanguage("khm");
        fullNameKhm.setValue("ងន់ ម៉េងលាង");
        fullName.add(fullNameKhm);
        identity.setFullName(fullName);
        identityResponse.setIdentity(identity);
        identityResponse.setStatus("ACTIVATED");
        restResponseWrapper.setResponse(identityResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ObjectMapper objectMapper = new ObjectMapper();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(true);
        JsonNode jsonNode = objectMapper.valueToTree(identity);
        profileDto.setIdentity(jsonNode);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);
        when(profileRegistryPlugin.isMatch(any(), any())).thenReturn(true);

        VerifyChallengeResponse verifyChallengeResponse = registrationService.
                verifyChallenge(verifyChallengeRequest, mockTransactionId);
        Assert.assertNotNull(verifyChallengeResponse);
        Assert.assertEquals("SUCCESS", verifyChallengeResponse.getStatus());
    }

    @Test
    public void doVerifyChallenge_withInvalidFormatForOTPChallenge_throwChallengeFormatAndTypeMismatch() {
        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-encoded");
        challengeInfoOTP.setChallenge("1623");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("challenge_format_and_type_mismatch", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withInactiveIdentity_throwIdentityInactive() {

        ChallengeInfo challengeInfoKBI = new ChallengeInfo();
        challengeInfoKBI.setFormat("base64url-encoded-json");
        challengeInfoKBI.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z-J4Z-B4Z6E4Z6b4Z624Z6EIn1dIH0");
        challengeInfoKBI.setType("KBI");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBI);
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse identityResponse = new IdentityResponse();
        Identity identity = new Identity();
        List<LanguageTaggedValue> fullName = new ArrayList<>();
        LanguageTaggedValue fullNameKhm = new LanguageTaggedValue();
        fullNameKhm.setLanguage("khm");
        fullNameKhm.setValue("ងន់ ម៉េងលាង");
        fullName.add(fullNameKhm);
        identity.setFullName(fullName);
        identityResponse.setIdentity(identity);
        identityResponse.setStatus("mock");
        restResponseWrapper.setResponse(identityResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(false);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_not_found", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withKBIChallengeNotFound_throwIdentityInactive() {

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse identityResponse = new IdentityResponse();
        Identity identity = new Identity();
        List<LanguageTaggedValue> fullName = new ArrayList<>();
        LanguageTaggedValue fullNameKhm = new LanguageTaggedValue();
        fullNameKhm.setLanguage("khm");
        fullNameKhm.setValue("ងន់ ម៉េងលាង");
        fullName.add(fullNameKhm);
        identity.setFullName(fullName);
        identityResponse.setIdentity(identity);
        identityResponse.setStatus("mock");
        restResponseWrapper.setResponse(identityResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(restResponseWrapper, HttpStatus.OK));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(false);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_not_found", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_whenGetIdentityNullResponse_throwFetchFailed() {

        ChallengeInfo challengeInfoKBI = new ChallengeInfo();
        challengeInfoKBI.setFormat("base64url-encoded-json");
        challengeInfoKBI.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z-J4Z-B4Z6E4Z6b4Z624Z6EIn1dIH0");
        challengeInfoKBI.setType("KBI");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBI);
        challengeList.add(challengeInfoOTP);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoOTP.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(false);
        when(profileRegistryPlugin.getProfile(any())).thenReturn(profileDto);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_not_found", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallenge_withNullTransaction_thenFail() throws SignUpException {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(null);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (InvalidTransactionException invalidTransactionException) {
            Assert.assertEquals("invalid_transaction", invalidTransactionException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallenge_withChallengeNotMatch_thenFail() throws SignUpException {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("failed");
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (ChallengeFailedException challengeFailedException) {
            Assert.assertEquals("challenge_failed", challengeFailedException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallenge_withIdentifierNotMatch_throwsIdentifierMismatch() throws SignUpException {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash(challengeInfo.getChallenge());
        registrationTransaction.setIdentifier("failed");
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException e) {
            Assert.assertEquals(ErrorConstants.IDENTIFIER_MISMATCH, e.getErrorCode());
        }
    }

    // ## register---------------------------------
    @Test
    public void register_thenPass() throws SignUpException {
        String local = "khm";
        String identifier = "+855219718732";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");
        registerRequest.setLocale(local);

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(identifier);

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<IdentityResponse> mockRestResponseWrapperAddIdentityResponse = new RestResponseWrapper<IdentityResponse>();
        mockRestResponseWrapperAddIdentityResponse.setResponse(identityResponse);
        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(passwordHash);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperUINResponse, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperPasswordHash, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperAddIdentityResponse, HttpStatus.OK));

        RegisterResponse registerResponse = registrationService.register(registerRequest, mockTransactionID);
        Assert.assertNotNull(registerResponse);
        Assert.assertEquals("PENDING", registerResponse.getStatus());
    }

    @Test
    public void register_withNullLocale_thenPass() throws SignUpException {
        String identifier = "+855219718732";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");
        registerRequest.setLocale(null);

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(identifier);

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<IdentityResponse> mockRestResponseWrapperAddIdentityResponse = new RestResponseWrapper<IdentityResponse>();
        mockRestResponseWrapperAddIdentityResponse.setResponse(identityResponse);
        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(passwordHash);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperUINResponse, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperPasswordHash, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperAddIdentityResponse, HttpStatus.OK));

        RegisterResponse registerResponse = registrationService.register(registerRequest, mockTransactionID);
        Assert.assertNotNull(registerResponse);
        Assert.assertEquals("PENDING", registerResponse.getStatus());
    }


    @Test
    public void register_withInvalidTransaction_throwInvalidTransaction() throws SignUpException {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");
        registerRequest.setLocale(locale);

        String mockTransactionID = "123456789";

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (InvalidTransactionException invalidTransactionException) {
            Assert.assertEquals("invalid_transaction", invalidTransactionException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidUsername_throwIdentifierMismatch() throws SignUpException {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855321444123");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");
        registerRequest.setLocale(locale);

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction("+855219718732", Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenThrow(new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH));

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.IDENTIFIER_MISMATCH, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidConsent_throwConsentRequired() throws SignUpException {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("DISAGREE");
        registerRequest.setLocale(locale);

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("consent_required", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_with_ResetPasswordPurposeTransaction_throwUnsupportedPurpose() throws SignUpException {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(objectMapper.createObjectNode());
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("DISAGREE");
        registerRequest.setLocale(locale);

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.RESET_PASSWORD);
        mockRegistrationTransaction.setChallengeHash("123456");

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);
        when(challengeManagerService.generateChallenge(any())).thenThrow(
                new SignUpException(ErrorConstants.UNSUPPORTED_PURPOSE));

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals(ErrorConstants.UNSUPPORTED_PURPOSE, ex.getErrorCode());
        }
    }

    // Generate Challenge OTP test cases
    @Test
    public void doGenerateChallenge_withoutTransactionId_thenPass() throws SignUpException, IOException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(false);
        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        GenerateChallengeResponse generateChallengeResponse =
                registrationService.generateChallenge(
                        generateChallengeRequest, "");
        Assert.assertNotNull(generateChallengeResponse);
        Assert.assertEquals("SUCCESS", generateChallengeResponse.getStatus());
    }

    @Test
    public void doGenerateChallenge_withFailedSendNotification_thenFail() throws SignUpException, IOException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(false);
        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        doThrow(new SignUpException("otp_notification_failed")).when(notificationHelper).sendSMSNotification(any(), any(), any(), any());

        try{
            registrationService.generateChallenge(generateChallengeRequest, "");
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("otp_notification_failed", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withRetryAttemptsOver3time_thenFail() throws SignUpException{
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(40));
        transaction.setChallengeRetryAttempts(3);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        GenerateChallengeResponse generateChallengeResponse =
                registrationService.generateChallenge(
                        generateChallengeRequest, transactionId);
        Assert.assertNotNull(generateChallengeResponse);
        Assert.assertEquals("SUCCESS", generateChallengeResponse.getStatus());
    }

    @Test
    public void doGenerateChallenge_withTransactionId_thenPass() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(40));

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        GenerateChallengeResponse generateChallengeResponse =
                registrationService.generateChallenge(
                        generateChallengeRequest, transactionId);
        Assert.assertNotNull(generateChallengeResponse);
        Assert.assertEquals("SUCCESS", generateChallengeResponse.getStatus());
    }

    @Test
    public void doGenerateChallenge_regenerateWithInvalidTransactionId_throwInvalidTransaction() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        String transactionId = "TRAN-12341";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(40));

        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals("invalid_transaction", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withInvalidCaptcha_throwInvalidCaptcha() throws EsignetException {
        String identifier = "12345678";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-invalid-captcha");
        ReflectionTestUtils.setField(registrationService, "captchaRequired", true);
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(false);
        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        try {
            registrationService.generateChallenge(generateChallengeRequest, "");
            Assert.fail();
        } catch (CaptchaException ex) {
            Assert.assertEquals("invalid_captcha", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withInvalidTransactionId_throwInvalidTransaction() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(null);
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (InvalidTransactionException ex) {
            Assert.assertEquals("invalid_transaction", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withIdentifierNotMatchTransactionId_throwIdentifierMismatch() throws SignUpException {
        String identifier = "+85577410541";
        String other_identifier = "+85577410542";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(other_identifier, Purpose.REGISTRATION);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (SignUpException ex) {
            Assert.assertEquals(ErrorConstants.IDENTIFIER_MISMATCH, ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withTooManyAttemptTransactionId_throwTooManyAttempts() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setChallengeRetryAttempts(4);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (GenerateChallengeException ex) {
            Assert.assertEquals("too_many_attempts", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withToEarlyAttemptTransactionId_throwTooEarlyAttempt() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.increaseAttempt();

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (GenerateChallengeException ex) {
            Assert.assertEquals(ErrorConstants.TOO_EARLY_ATTEMPT, ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withRegistrationPurpose_thenPass() throws SignUpException, IOException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(false);
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);

        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        GenerateChallengeResponse generateChallengeResponse =
                registrationService.generateChallenge(
                        generateChallengeRequest, "");
        Assert.assertNotNull(generateChallengeResponse);
        Assert.assertEquals("SUCCESS", generateChallengeResponse.getStatus());
    }

    @Test
    public void doGenerateChallenge_with_ResetPasswordPurpose_thenPass() throws SignUpException, IOException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerateChallenge(false);
        generateChallengeRequest.setPurpose(Purpose.RESET_PASSWORD);

        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(captchaHelper.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        GenerateChallengeResponse generateChallengeResponse =
                registrationService.generateChallenge(
                        generateChallengeRequest, "");
        Assert.assertNotNull(generateChallengeResponse);
        Assert.assertEquals("SUCCESS", generateChallengeResponse.getStatus());
    }

    @Test
    public void doGetRegistrationStatus_withCompletedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), ProfileCreateUpdateStatus.COMPLETED);
    }

    @Test
    public void doGetRegistrationStatus_withPendingTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.PENDING);
        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(profileRegistryPlugin.getProfileCreateUpdateStatus(any())).thenReturn(ProfileCreateUpdateStatus.PENDING);

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), ProfileCreateUpdateStatus.PENDING);
    }

    @Test
    public void doGetRegistrationStatus_withFailedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.FAILED);
        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), ProfileCreateUpdateStatus.FAILED);
    }

    @Test
    public void doGetRegistrationStatus_withInvalidTransaction_throwInvalidTransaction() {
        String transactionId = "TRAN-1234";
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void doGetRegistrationStatus_withEmptyTransactionId_throwInvalidTransaction() {
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus("");
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void doGetRegistrationStatus_withNullTransactionId_throwInvalidTransaction() {
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(null);
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void doUpdatePassword_thenSuccess() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(locale);

        RegistrationTransaction transaction = new RegistrationTransaction(resetPasswordRequest.getIdentifier(),
                Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");

        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");
        RestResponseWrapper<Password.PasswordHash> mockPasswordHashRestResponseWrapper = new RestResponseWrapper<>();
        mockPasswordHashRestResponseWrapper.setResponse(passwordHash);

        RestResponseWrapper<IdentityResponse> mockIdentityResponseRestResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse mockIdentityResponse = new IdentityResponse();
        mockIdentityResponseRestResponseWrapper.setResponse(mockIdentityResponse);
        mockIdentityResponse.setStatus(SignUpConstants.ACTIVATED);
        mockIdentityResponseRestResponseWrapper.setErrors(new ArrayList<>());

        when(cacheUtilService.getChallengeVerifiedTransaction(verifiedTransactionId)).thenReturn(transaction);
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockPasswordHashRestResponseWrapper, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockIdentityResponseRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.updatePassword(resetPasswordRequest,
                verifiedTransactionId);
        Assert.assertEquals(ProfileCreateUpdateStatus.PENDING, registrationStatusResponse.getStatus());
    }

    @Test
    public void doUpdatePassword_withNullLocale_thenSuccess() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(null);

        RegistrationTransaction transaction = new RegistrationTransaction(resetPasswordRequest.getIdentifier(),
                Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");

        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");
        RestResponseWrapper<Password.PasswordHash> mockPasswordHashRestResponseWrapper = new RestResponseWrapper<>();
        mockPasswordHashRestResponseWrapper.setResponse(passwordHash);

        RestResponseWrapper<IdentityResponse> mockIdentityResponseRestResponseWrapper = new RestResponseWrapper<>();
        IdentityResponse mockIdentityResponse = new IdentityResponse();
        mockIdentityResponseRestResponseWrapper.setResponse(mockIdentityResponse);
        mockIdentityResponse.setStatus(SignUpConstants.ACTIVATED);
        mockIdentityResponseRestResponseWrapper.setErrors(new ArrayList<>());

        when(cacheUtilService.getChallengeVerifiedTransaction(verifiedTransactionId)).thenReturn(transaction);
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockPasswordHashRestResponseWrapper, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockIdentityResponseRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.updatePassword(resetPasswordRequest,
                verifiedTransactionId);
        Assert.assertEquals(ProfileCreateUpdateStatus.PENDING, registrationStatusResponse.getStatus());
    }

    @Test
    public void doUpdatePassword_withInvalidTransaction_thenSuccess() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(locale);

        try {
            registrationService.updatePassword(resetPasswordRequest, verifiedTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("invalid_transaction", signUpException.getErrorCode());
        }
    }

    @Test
    public void doUpdatePassword_withIdentifierMismatch_throwIdentifierMismatch() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(locale);

        RegistrationTransaction transaction = new RegistrationTransaction("****", Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");
        when(cacheUtilService.getChallengeVerifiedTransaction(verifiedTransactionId)).thenReturn(transaction);

        try {
            registrationService.updatePassword(resetPasswordRequest, verifiedTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identifier_mismatch", signUpException.getErrorCode());
        }
    }

    @Test
    public void doUpdatePassword_whenIdentityEndpointResponseIsNull_throwResetPasswordFailed() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(locale);

        RegistrationTransaction transaction = new RegistrationTransaction(resetPasswordRequest.getIdentifier(),
                Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");

        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");
        RestResponseWrapper<Password.PasswordHash> mockPasswordHashRestResponseWrapper = new RestResponseWrapper<>();
        mockPasswordHashRestResponseWrapper.setResponse(passwordHash);

        when(cacheUtilService.getChallengeVerifiedTransaction(verifiedTransactionId)).thenReturn(transaction);
        when(profileRegistryPlugin.updateProfile(any(), any()))
                .thenThrow(new SignUpException(ErrorConstants.RESET_PWD_FAILED));

        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockPasswordHashRestResponseWrapper, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        try {
            registrationService.updatePassword(resetPasswordRequest, verifiedTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.RESET_PWD_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void doUpdatePassword_whenIdentityEndpointReturnInvalidResponse_throwResetPasswordFailed() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(locale);

        RegistrationTransaction transaction = new RegistrationTransaction(resetPasswordRequest.getIdentifier(),
                Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");

        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");
        RestResponseWrapper<Password.PasswordHash> mockPasswordHashRestResponseWrapper = new RestResponseWrapper<>();
        mockPasswordHashRestResponseWrapper.setResponse(passwordHash);

        RestResponseWrapper<IdentityResponse> mockIdentityResponseRestResponseWrapper = new RestResponseWrapper<>();
        mockIdentityResponseRestResponseWrapper.setResponse(null);

        when(cacheUtilService.getChallengeVerifiedTransaction(verifiedTransactionId)).thenReturn(transaction);
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockPasswordHashRestResponseWrapper, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockIdentityResponseRestResponseWrapper, HttpStatus.OK));

        when(profileRegistryPlugin.updateProfile(any(), any()))
                .thenThrow(new SignUpException("reset_pwd_failed"));

        try {
            registrationService.updatePassword(resetPasswordRequest, verifiedTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("reset_pwd_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void doUpdatePassword_whenIdentityEndpointReturnsError_throwErrorFromAnotherService() {
        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");
        resetPasswordRequest.setLocale(locale);

        RegistrationTransaction transaction = new RegistrationTransaction(resetPasswordRequest.getIdentifier(),
                Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");

        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");
        RestResponseWrapper<Password.PasswordHash> mockPasswordHashRestResponseWrapper = new RestResponseWrapper<>();
        mockPasswordHashRestResponseWrapper.setResponse(passwordHash);

        RestResponseWrapper<IdentityResponse> mockIdentityResponseRestResponseWrapper = new RestResponseWrapper<>();
        ArrayList<RestError> restErrorArrayList = new ArrayList<>();
        RestError restError = new RestError();
        restError.setErrorCode("error_from_another_service");
        restErrorArrayList.add(restError);
        mockIdentityResponseRestResponseWrapper.setErrors(restErrorArrayList);

        when(cacheUtilService.getChallengeVerifiedTransaction(verifiedTransactionId)).thenReturn(transaction);
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockPasswordHashRestResponseWrapper, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(identityEndpoint),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockIdentityResponseRestResponseWrapper, HttpStatus.OK));

        when(profileRegistryPlugin.updateProfile(any(), any()))
                .thenThrow(new SignUpException("error_from_another_service"));
        try {
            registrationService.updatePassword(resetPasswordRequest, verifiedTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("error_from_another_service", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallenge_withExpiredChallenge_throwChallengeExpired() throws SignUpException {
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");
        challengeInfo.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfo);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfo.getChallenge());
        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        registrationTransaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(2));
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException exception) {
            Assert.assertEquals(ErrorConstants.CHALLENGE_EXPIRED, exception.getErrorCode());
        }
    }

    @Test
    public void doGetRegistrationStatusFromServer_withApplicationID_thenReturnPending() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        Map<String, ProfileCreateUpdateStatus> handlesStatus = new LinkedHashMap<>();
        handlesStatus.put(transactionId, ProfileCreateUpdateStatus.PENDING);
        registrationTransaction.setHandlesStatus(handlesStatus);
        RestResponseWrapper<Map<String,String>> mockRestResponseWrapper = new RestResponseWrapper<>();
        Map<String,String> response = new LinkedHashMap<>();
        response.put("statusCode", "ISSUED");
        mockRestResponseWrapper.setResponse(response);

        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getRegistrationStatusEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                any(String.class)))
                .thenReturn(new ResponseEntity<>(mockRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(ProfileCreateUpdateStatus.PENDING, registrationStatusResponse.getStatus());
    }

    @Test
    public void doGetRegistrationStatusFromServer_withApplicationID_thenReturnCompleted() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        Map<String, ProfileCreateUpdateStatus> handlesStatus = new LinkedHashMap<>();
        handlesStatus.put(transactionId, ProfileCreateUpdateStatus.PENDING);
        registrationTransaction.setHandlesStatus(handlesStatus);
        RestResponseWrapper<Map<String,String>> mockRestResponseWrapper = new RestResponseWrapper<>();
        Map<String,String> response = new LinkedHashMap<>();
        response.put("statusCode", "STORED");
        mockRestResponseWrapper.setResponse(response);

        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getRegistrationStatusEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                any(String.class)))
                .thenReturn(new ResponseEntity<>(mockRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(ProfileCreateUpdateStatus.COMPLETED, registrationStatusResponse.getStatus());
    }

    @Test
    public void doGetRegistrationStatusFromServer_withApplicationID_thenReturnFailed() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.FAILED);
        Map<String, ProfileCreateUpdateStatus> handlesStatus = new LinkedHashMap<>();
        handlesStatus.put(transactionId, ProfileCreateUpdateStatus.FAILED);
        registrationTransaction.setHandlesStatus(handlesStatus);
        RestResponseWrapper<Map<String,String>> mockRestResponseWrapper = new RestResponseWrapper<>();
        Map<String,String> response = new LinkedHashMap<>();
        response.put("statusCode", "FAILED");
        mockRestResponseWrapper.setResponse(response);

        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getRegistrationStatusEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                any(String.class)))
                .thenReturn(new ResponseEntity<>(mockRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(ProfileCreateUpdateStatus.FAILED, registrationStatusResponse.getStatus());
    }

    @Test
    public void doGetRegistrationStatusFromServer_withNullRegistrationStatus_thenReturnPending() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        Map<String, ProfileCreateUpdateStatus> handlesStatus = new LinkedHashMap<>();
        handlesStatus.put(transactionId, ProfileCreateUpdateStatus.PENDING);
        registrationTransaction.setHandlesStatus(handlesStatus);

        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getRegistrationStatusEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                any(String.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(ProfileCreateUpdateStatus.PENDING, registrationStatusResponse.getStatus());
    }

    @Test
    public void doGetRegistrationStatusFromServer_withNullResponseBodyRegistrationStatus_thenReturnPending() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        Map<String, ProfileCreateUpdateStatus> handlesStatus = new LinkedHashMap<>();
        handlesStatus.put(transactionId, ProfileCreateUpdateStatus.PENDING);
        registrationTransaction.setHandlesStatus(handlesStatus);
        RestResponseWrapper<Map<String,String>> mockRestResponseWrapper = new RestResponseWrapper<>();
        mockRestResponseWrapper.setResponse(null);

        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getRegistrationStatusEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                any(String.class)))
                .thenReturn(new ResponseEntity<>(mockRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(ProfileCreateUpdateStatus.PENDING, registrationStatusResponse.getStatus());
    }

    @Test
    public void doGetRegistrationStatusFromServer_withEmptyStatusCode_thenReturnPending() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(ProfileCreateUpdateStatus.COMPLETED);
        Map<String, ProfileCreateUpdateStatus> handlesStatus = new LinkedHashMap<>();
        handlesStatus.put(transactionId, ProfileCreateUpdateStatus.PENDING);
        registrationTransaction.setHandlesStatus(handlesStatus);
        RestResponseWrapper<Map<String,String>> mockRestResponseWrapper = new RestResponseWrapper<>();
        Map<String,String> response = new LinkedHashMap<>();
        response.put("statusCode", "");
        mockRestResponseWrapper.setResponse(response);

        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        when(selfTokenRestTemplate.exchange(
                eq(getRegistrationStatusEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class),
                any(String.class)))
                .thenReturn(new ResponseEntity<>(mockRestResponseWrapper, HttpStatus.OK));

        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);
        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(ProfileCreateUpdateStatus.PENDING, registrationStatusResponse.getStatus());
    }
}
