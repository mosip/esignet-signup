package io.mosip.signup.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.ResponseWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.helper.NotificationHelper;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.Purpose;
import io.mosip.signup.util.RegistrationStatus;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.SignUpConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.persistence.Id;
import javax.servlet.http.HttpServletResponse;

import io.mosip.esignet.core.exception.EsignetException;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RunWith(SpringRunner.class)
@ActiveProfiles(value = {"test"})
public class RegistrationServiceTest {

    @InjectMocks
    RegistrationService registrationService;

    @Mock
    CallEndpointService callEndpointService;

    @Mock
    CacheUtilService cacheUtilService;

    @Mock
    RestTemplate selfTokenRestTemplate;

    @Mock
    ChallengeManagerService challengeManagerService;

    @Mock
    HttpServletResponse response;

    @Mock
    GoogleRecaptchaValidatorService googleRecaptchaValidatorService;

    @Mock
    NotificationHelper notificationHelper;

    @Mock
    CryptoHelper cryptoHelper;

    private final String identityEndpoint = "identityEndpoint";
    private final String generateHashEndpoint = "generateHashEndpoint";
    private final String getIdentityEndpoint = "getIdentityEndpoint";
    private final String getUinEndpoint = "getUinEndpoint";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(registrationService, identityEndpoint, identityEndpoint);
        ReflectionTestUtils.setField(registrationService, generateHashEndpoint, generateHashEndpoint);
        ReflectionTestUtils.setField(registrationService, getUinEndpoint, getUinEndpoint);
        ReflectionTestUtils.setField(
                registrationService, "resendAttempts", 3);
        ReflectionTestUtils.setField(
                registrationService, "resendDelay", 30);
        ReflectionTestUtils.setField(
                registrationService, "challengeTimeout", 60);
        ReflectionTestUtils.setField(registrationService, getIdentityEndpoint, getIdentityEndpoint);
        ReflectionTestUtils.setField(registrationService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(registrationService, "otpLength", 6);
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

        RestResponseWrapper<IdentityResponse> restResponseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);

        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        when(selfTokenRestTemplate.exchange(
                eq(getIdentityEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(new RestResponseWrapper<>(), HttpStatus.OK));

        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

        VerifyChallengeResponse verifyChallengeResponse = registrationService.
                verifyChallenge(verifyChallengeRequest, mockTransactionId);
        Assert.assertNotNull(verifyChallengeResponse);
        Assert.assertEquals("SUCCESS", verifyChallengeResponse.getStatus());
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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

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


        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("fetch_identity_failed", signUpException.getErrorCode());
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
    public void doVerifyChallengeInResetPassword_withKBAMismatchFormat_throwChallengeFormatAndTypeMismatch() {
        ChallengeInfo challengeInfoKBA = new ChallengeInfo();
        challengeInfoKBA.setFormat("base64url-json");
        challengeInfoKBA.setChallenge("111111");
        challengeInfoKBA.setType("KBA");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBA);

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeList);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.RESET_PASSWORD);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                challengeInfoKBA.getChallenge());
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
    public void doVerifyChallengeInResetPassword_withInvalidKBAChallenge_throwInvalidKBAChallenge() {

        ChallengeInfo challengeInfoKBA = new ChallengeInfo();
        challengeInfoKBA.setFormat("base64url-encoded-json");
        challengeInfoKBA.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z==");
        challengeInfoKBA.setType("KBA");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBA);
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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("invalid_KBA_challenge", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withFullNameMismatch_throwKnowledgeBaseMismatch() {

        ChallengeInfo challengeInfoKBA = new ChallengeInfo();
        challengeInfoKBA.setFormat("base64url-encoded-json");
        challengeInfoKBA.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICJNYW5val9raG0ifV0gfQ==");
        challengeInfoKBA.setType("KBA");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBA);
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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("knowledgebase_mismatch", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_thenSuccess() {

        ChallengeInfo challengeInfoKBA = new ChallengeInfo();
        challengeInfoKBA.setFormat("base64url-encoded-json");
        challengeInfoKBA.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z-J4Z-B4Z6E4Z6b4Z624Z6EIn1dIH0");
        challengeInfoKBA.setType("KBA");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBA);
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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

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

        ChallengeInfo challengeInfoKBA = new ChallengeInfo();
        challengeInfoKBA.setFormat("base64url-encoded-json");
        challengeInfoKBA.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z-J4Z-B4Z6E4Z6b4Z624Z6EIn1dIH0");
        challengeInfoKBA.setType("KBA");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBA);
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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identity_inactive", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_withKBAChallengeNotFound_throwIdentityInactive() {

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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetIdentityEndpoint = new ResponseEntity<>(restResponseWrapper,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getIdentityEndpoint), any())).thenReturn(responseEntityOfGetIdentityEndpoint.getBody());

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("identity_inactive", signUpException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallengeInResetPassword_whenGetIdentityNullResponse_throwFetchFailed() {

        ChallengeInfo challengeInfoKBA = new ChallengeInfo();
        challengeInfoKBA.setFormat("base64url-encoded-json");
        challengeInfoKBA.setChallenge("eyAiZnVsbE5hbWUiOiBbeyJsYW5ndWFnZSI6ImtobSIsICJ2YWx1ZSI6ICLhnoThnpPhn4sg4Z6Y4Z-J4Z-B4Z6E4Z6b4Z624Z6EIn1dIH0");
        challengeInfoKBA.setType("KBA");

        ChallengeInfo challengeInfoOTP = new ChallengeInfo();
        challengeInfoOTP.setFormat("alpha-numeric");
        challengeInfoOTP.setChallenge("111111");
        challengeInfoOTP.setType("OTP");

        List<ChallengeInfo> challengeList = new ArrayList<>();
        challengeList.add(challengeInfoKBA);
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


        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("fetch_identity_failed", signUpException.getErrorCode());
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
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(userInfo.getPhone(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

//        IdentityResponse identityResponse = new IdentityResponse();
//        identityResponse.setStatus("ACTIVATED");
        HashMap<String, String> identityResponse = new HashMap<>();
        identityResponse.put("status", "ACTIVATED");

        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<HashMap<String, String>> mockRestResponseWrapperAddIdentityResponse = new RestResponseWrapper<>();
        mockRestResponseWrapperAddIdentityResponse.setResponse(identityResponse);
        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(passwordHash);



        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockRestResponseWrapperPasswordHash,HttpStatus.OK);

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
//        when(selfTokenRestTemplate.exchange(
//                eq(identityEndpoint),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperAddIdentityResponse, HttpStatus.OK));

        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

        ResponseEntity<RestResponseWrapper> restResponseWrapperWithIdentityResponse =
                new ResponseEntity<RestResponseWrapper>(mockRestResponseWrapperAddIdentityResponse, HttpStatus.OK);

        when(callEndpointService.callEndpoint(any(),any(), eq(identityEndpoint), any())).thenReturn(
                restResponseWrapperWithIdentityResponse.getBody());

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        RegisterResponse registerResponse = registrationService.register(registerRequest, mockTransactionID);
        Assert.assertNotNull(registerResponse);
        Assert.assertEquals("PENDING", registerResponse.getStatus());
    }

    @Test
    public void register_whenUinEndpointResponseNullBody_throwGetUINFailed() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.GET_UIN_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenUinEndpointResponseNullUIN_throwGetUINFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN(null);
        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperUINResponse, HttpStatus.OK));

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals(ErrorConstants.GET_UIN_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenUinEndpointResponseErrors_throwServerError() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<>();
        ArrayList<RestError> errors = new ArrayList<>();
        errors.add(new RestError("server_error", "server_error"));
        mockRestResponseWrapperUINResponse.setErrors(errors);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperUINResponse, HttpStatus.OK));

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("server_error", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenUinEndpointResponseWithNullErrors_throwGetUINFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<>();
        mockRestResponseWrapperUINResponse.setErrors(null);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperUINResponse, HttpStatus.OK));

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("get_uin_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenGenerateHashEndpointResponseNullBody_throwHashGenerateFailed() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperUINResponse, HttpStatus.OK));
        when(selfTokenRestTemplate.exchange(
                eq(generateHashEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.HASH_GENERATE_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenGenerateHashEndpointResponseErrors_throwServerError() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        ArrayList<RestError> errors = new ArrayList<>();
        errors.add(new RestError("server_error", "server_error"));
        mockRestResponseWrapperPasswordHash.setErrors(errors);

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockRestResponseWrapperPasswordHash,HttpStatus.OK);

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
        
        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("server_error", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenGenerateHashEndpointResponseNullErrors_throwHashGenerateFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setErrors(null);

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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("hash_generate_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenGenerateHashEndpointResponseNullSaltedPassword_throwHashGenerateFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(new Password.PasswordHash());

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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("hash_generate_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenGenerateHashEndpointResponseNullSalt_throwHashGenerateFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt(null);
        passwordHash.setHashValue("mockHashValue");

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

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("hash_generate_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenAddIdentityEndpointResponseNullBody_throwAddIdentityFailed() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(passwordHash);

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockRestResponseWrapperPasswordHash,HttpStatus.OK);

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
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("add_identity_failed", signUpException.getMessage());
        }
    }

    @Test
    public void register_whenAddIdentityEndpointResponseStatusNotEqualsACTIVATED_throwAddIdentityFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(userInfo.getPhone(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("mock");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<IdentityResponse> mockRestResponseWrapperIdentityResponse = new RestResponseWrapper<IdentityResponse>();
        mockRestResponseWrapperIdentityResponse.setResponse(identityResponse);
        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(passwordHash);

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockRestResponseWrapperPasswordHash,HttpStatus.OK);

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
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperIdentityResponse, HttpStatus.OK));

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("add_identity_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenAddIdentityEndpointResponseErrors_throwServerError() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<IdentityResponse> mockRestResponseWrapperIdentityResponse = new RestResponseWrapper<IdentityResponse>();
        ArrayList<RestError> errors = new ArrayList<>();
        errors.add(new RestError("server_error", "server_error"));
        mockRestResponseWrapperIdentityResponse.setErrors(errors);
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
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperIdentityResponse, HttpStatus.OK));

        when(callEndpointService.callEndpoint(any(), any(), any(), any())).thenThrow(
                new SignUpException(ErrorConstants.ADD_IDENTITY_FAILED));

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.ADD_IDENTITY_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_whenAddIdentityEndpointResponseNullErrors_throwAddIdentityFailed() throws SignUpException{
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        IdentityResponse identityResponse = new IdentityResponse();
        identityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<IdentityResponse> mockRestResponseWrapperIdentityResponse = new RestResponseWrapper<IdentityResponse>();
        mockRestResponseWrapperIdentityResponse.setErrors(null);
        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        mockRestResponseWrapperPasswordHash.setResponse(passwordHash);

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockRestResponseWrapperPasswordHash,HttpStatus.OK);

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
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockRestResponseWrapperIdentityResponse, HttpStatus.OK));

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

        ResponseEntity<RestResponseWrapper> responseEntityOfGetUinEndpoint =
                new ResponseEntity<>(mockRestResponseWrapperUINResponse,HttpStatus.OK);
        when(callEndpointService.callEndpoint(any(),any(), eq(getUinEndpoint), any())).thenReturn(
                responseEntityOfGetUinEndpoint.getBody());

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("add_identity_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidTransaction_throwInvalidTransaction() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

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
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855321444123");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("AGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        try {
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals(ErrorConstants.IDENTIFIER_MISMATCH, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidConsent_throwConsentRequired() throws SignUpException {
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("DISAGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.REGISTRATION);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

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
        UserInfoMap userInfo = new UserInfoMap();
        userInfo.setPreferredLang("khm");
        userInfo.setFullName(List.of(new LanguageTaggedValue("eng", "Panharith AN")));
        userInfo.setPhone("+855219718732");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setUsername("+855219718732");
        registerRequest.setPassword("123123");
        registerRequest.setConsent("DISAGREE");

        String mockTransactionID = "123456789";

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername(), Purpose.RESET_PASSWORD);
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

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
        generateChallengeRequest.setRegenerate(false);
        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

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
        generateChallengeRequest.setRegenerate(false);
        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        when(notificationHelper.sendSMSNotification(any(), any(), any(), any()))
                .thenThrow(new RestClientException("failed to send notification"));

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
        generateChallengeRequest.setRegenerate(true);
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(40));
        transaction.setChallengeRetryAttempts(3);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

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
        generateChallengeRequest.setRegenerate(true);
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(40));

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

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
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-12341";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setLastRetryAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(40));

        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
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
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(false);
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
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(null);
        when(googleRecaptchaValidatorService.validateCaptcha(
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
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(other_identifier, Purpose.REGISTRATION);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(googleRecaptchaValidatorService.validateCaptcha(
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
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.setChallengeRetryAttempts(4);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(googleRecaptchaValidatorService.validateCaptcha(
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
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, Purpose.REGISTRATION);
        transaction.increaseAttempt();

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(googleRecaptchaValidatorService.validateCaptcha(
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
        generateChallengeRequest.setRegenerate(false);
        generateChallengeRequest.setPurpose(Purpose.REGISTRATION);

        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

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
        generateChallengeRequest.setRegenerate(false);
        generateChallengeRequest.setPurpose(Purpose.RESET_PASSWORD);

        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);
        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

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
        registrationTransaction.setRegistrationStatus(RegistrationStatus.COMPLETED);
        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.COMPLETED);
    }

    @Test
    public void doGetRegistrationStatus_withPendingTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(RegistrationStatus.PENDING);
        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.PENDING);
    }

    @Test
    public void doGetRegistrationStatus_withFailedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", Purpose.REGISTRATION);
        registrationTransaction.setRegistrationStatus(RegistrationStatus.FAILED);
        when(cacheUtilService.getStatusCheckTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setStatusCheckTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.FAILED);
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

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockPasswordHashRestResponseWrapper,HttpStatus.OK);
        ResponseEntity<RestResponseWrapper> responseEntityOfIdentityEndpoint = new ResponseEntity<>(mockIdentityResponseRestResponseWrapper,HttpStatus.OK);

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

        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());
        when(callEndpointService.callEndpoint(any(),any(), eq(identityEndpoint), any())).thenReturn(responseEntityOfIdentityEndpoint.getBody());

        RegistrationStatusResponse registrationStatusResponse = registrationService.updatePassword(resetPasswordRequest,
                verifiedTransactionId);
        Assert.assertEquals(RegistrationStatus.PENDING, registrationStatusResponse.getStatus());
    }

    @Test
    public void doUpdatePassword_withInvalidTransaction_thenSuccess() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");

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

        RegistrationTransaction transaction = new RegistrationTransaction(resetPasswordRequest.getIdentifier(),
                Purpose.RESET_PASSWORD);
        transaction.setUin("mockUin");

        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");
        RestResponseWrapper<Password.PasswordHash> mockPasswordHashRestResponseWrapper = new RestResponseWrapper<>();
        mockPasswordHashRestResponseWrapper.setResponse(passwordHash);

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockPasswordHashRestResponseWrapper,HttpStatus.OK);

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
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

        try {
            registrationService.updatePassword(resetPasswordRequest, verifiedTransactionId);
            Assert.fail();
        } catch (SignUpException signUpException) {
            Assert.assertEquals("reset_pwd_failed", signUpException.getErrorCode());
        }
    }

    @Test
    public void doUpdatePassword_whenIdentityEndpointReturnInvalidResponse_throwResetPasswordFailed() {

        String verifiedTransactionId = "VERIFIED_TRANSACTION_ID";
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("Password@2002");
        resetPasswordRequest.setIdentifier("+85512345678");

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

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockPasswordHashRestResponseWrapper,HttpStatus.OK);

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

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());

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

        ResponseEntity<RestResponseWrapper> responseEntity = new ResponseEntity<>(mockPasswordHashRestResponseWrapper,HttpStatus.OK);
        ResponseEntity<RestResponseWrapper> responseEntityOfIdentityEndpoint = new ResponseEntity<>(mockIdentityResponseRestResponseWrapper,HttpStatus.OK);

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

        when(callEndpointService.callEndpoint(any(),any(), eq(generateHashEndpoint), any())).thenReturn(responseEntity.getBody());
        when(callEndpointService.callEndpoint(any(),any(), eq(identityEndpoint), any())).thenReturn(responseEntityOfIdentityEndpoint.getBody());

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
}
