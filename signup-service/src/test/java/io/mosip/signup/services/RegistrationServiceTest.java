package io.mosip.signup.services;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.helper.NotificationHelper;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.RegistrationStatus;
import io.mosip.signup.exception.SignUpException;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    private final String addIdentityEndpoint = "addIdentityEndpoint";
    private final String generateHashEndpoint = "generateHashEndpoint";
    private final String getUinEndpoint = "getUinEndpoint";

    @Before
    public void setUp(){
        ReflectionTestUtils.setField(registrationService, addIdentityEndpoint, addIdentityEndpoint);
        ReflectionTestUtils.setField(registrationService, generateHashEndpoint, generateHashEndpoint);
        ReflectionTestUtils.setField(registrationService, getUinEndpoint, getUinEndpoint);
        ReflectionTestUtils.setField(
                registrationService, "resendAttempts", 3);
        ReflectionTestUtils.setField(
                registrationService, "resendDelay", 30);
    }

    @Test
    public void doVerifyChallenge_thenPass() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123");
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challengeInfo.getChallenge());
        registrationTransaction.setChallengeHash(challengeHash);
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        VerifyChallengeResponse verifyChallengeResponse = registrationService.
                verifyChallenge(verifyChallengeRequest, mockTransactionId);
        Assert.assertNotNull(verifyChallengeResponse);
        Assert.assertEquals("SUCCESS", verifyChallengeResponse.getStatus());
    }

    @Test
    public void doVerifyChallenge_withNullTransaction_thenFail() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(null);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        }catch (InvalidTransactionException invalidTransactionException){
            Assert.assertEquals("invalid_transaction", invalidTransactionException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallenge_withChallengeNotMatch_thenFail() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123");
        registrationTransaction.setChallengeHash("failed");
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        }catch (ChallengeFailedException challengeFailedException){
            Assert.assertEquals("challenge_failed", challengeFailedException.getErrorCode());
        }
    }

    @Test
    public void doVerifyChallenge_withIdentifierNotMatch_throwsException() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123");
        registrationTransaction.setChallengeHash(challengeInfo.getChallenge());
        registrationTransaction.setIdentifier("failed");
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        }catch (SignUpException e){
            Assert.assertEquals(ErrorConstants.IDENTIFIER_MISMATCH, e.getErrorCode());
        }
    }

    // ## register---------------------------------
    @Test
    public void register_thenPass() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(userInfo.getPhone());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        AddIdentityResponse addIdentityResponse = new AddIdentityResponse();
        addIdentityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<AddIdentityResponse> mockRestResponseWrapperAddIdentityResponse = new RestResponseWrapper<AddIdentityResponse>();
        mockRestResponseWrapperAddIdentityResponse.setResponse(addIdentityResponse);
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
                eq(addIdentityEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperAddIdentityResponse, HttpStatus.OK));

        when(notificationHelper.sendSMSNotificationAsync(any(), any(), any(), any()))
                .thenReturn(new CompletableFuture<>());

        RegisterResponse registerResponse = registrationService.register(registerRequest, mockTransactionID);
        Assert.assertNotNull(registerResponse);
        Assert.assertEquals("PENDING", registerResponse.getStatus());
    }

    @Test
    public void register_thenUinEndpointResponseNullBody_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        when(selfTokenRestTemplate.exchange(
                eq(getUinEndpoint),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals(ErrorConstants.GET_UIN_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_thenUinEndpointResponseErrors_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
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

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("server_error", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_thenGenerateHashEndpointResponseNullBody_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        AddIdentityResponse addIdentityResponse = new AddIdentityResponse();
        addIdentityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

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

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals(ErrorConstants.HASH_GENERATE_FAILED, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_thenGenerateHashEndpointResponseErrors_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        AddIdentityResponse addIdentityResponse = new AddIdentityResponse();
        addIdentityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<UINResponse> mockRestResponseWrapperUINResponse = new RestResponseWrapper<UINResponse>();
        mockRestResponseWrapperUINResponse.setResponse(uinResponse);
        RestResponseWrapper<Password.PasswordHash> mockRestResponseWrapperPasswordHash = new RestResponseWrapper<Password.PasswordHash>();
        ArrayList<RestError> errors = new ArrayList<>();
        errors.add(new RestError("server_error","server_error"));
        mockRestResponseWrapperPasswordHash.setErrors(errors);

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

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("server_error", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_thenAddIdentityEndpointResponseNullBody_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        AddIdentityResponse addIdentityResponse = new AddIdentityResponse();
        addIdentityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
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
        when(selfTokenRestTemplate.exchange(
                eq(addIdentityEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("add_identity_failed", signUpException.getMessage());
        }
    }

    @Test
    public void register_thenAddIdentityEndpointResponseErrors_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        AddIdentityResponse addIdentityResponse = new AddIdentityResponse();
        addIdentityResponse.setStatus("ACTIVATED");
        UINResponse uinResponse = new UINResponse();
        uinResponse.setUIN("mockUIN");
        Password.PasswordHash passwordHash = new Password.PasswordHash();
        passwordHash.setSalt("mockSalt");
        passwordHash.setHashValue("mockHashValue");

        RestResponseWrapper<AddIdentityResponse> mockRestResponseWrapperAddIdentityResponse = new RestResponseWrapper<AddIdentityResponse>();
        ArrayList<RestError> errors = new ArrayList<>();
        errors.add(new RestError("server_error", "server_error"));
        mockRestResponseWrapperAddIdentityResponse.setErrors(errors);
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
                eq(addIdentityEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(mockRestResponseWrapperAddIdentityResponse, HttpStatus.OK));

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("server_error", signUpException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidTransaction_throwException() throws Exception{
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

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (InvalidTransactionException invalidTransactionException){
            Assert.assertEquals("invalid_transaction", invalidTransactionException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidUsername_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals(ErrorConstants.IDENTIFIER_MISMATCH, signUpException.getErrorCode());
        }
    }

    @Test
    public void register_withInvalidConsent_throwException() throws Exception{
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

        RegistrationTransaction mockRegistrationTransaction = new RegistrationTransaction(registerRequest.getUsername());
        mockRegistrationTransaction.setChallengeHash("123456");
        mockRegistrationTransaction.setIdentifier(userInfo.getPhone());

        when(cacheUtilService.getChallengeVerifiedTransaction(mockTransactionID))
                .thenReturn(mockRegistrationTransaction);

        try{
            registrationService.register(registerRequest, mockTransactionID);
            Assert.fail();
        }catch (SignUpException signUpException){
            Assert.assertEquals("consent_required", signUpException.getErrorCode());
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
    public void doGenerateChallenge_withTransactionId_thenPass() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier);
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
    public void doGenerateChallenge_regenerateWithInvalidTransactionId_thenFail() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-12341";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier);
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
    public void doGenerateChallenge_withInvalidCaptcha_thenFail() throws EsignetException {
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
    public void doGenerateChallenge_withInvalidTransactionId_thenFail() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier);

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
    public void doGenerateChallenge_withIdentifierNotMatchTransactionId_thenFail() throws SignUpException {
        String identifier = "+85577410541";
        String other_identifier = "+85577410542";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(other_identifier);

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
    public void doGenerateChallenge_withTooManyAttemptTransactionId_thenFail() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier);
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
    public void doGenerateChallenge_withToEarlyAttemptTransactionId_thenFail() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        generateChallengeRequest.setRegenerate(true);
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier);
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
    public void doGetRegistrationStatus_withCompletedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541");
        registrationTransaction.setRegistrationStatus(RegistrationStatus.COMPLETED);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setRegisteredTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.COMPLETED);
    }

    @Test
    public void doGetRegistrationStatus_withPendingTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541");
        registrationTransaction.setRegistrationStatus(RegistrationStatus.PENDING);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setRegisteredTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.PENDING);
    }

    @Test
    public void doGetRegistrationStatus_withFailedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541");
        registrationTransaction.setRegistrationStatus(RegistrationStatus.FAILED);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        when(cacheUtilService.setRegisteredTransaction(transactionId, registrationTransaction)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.FAILED);
    }

    @Test
    public void doGetRegistrationStatus_withInvalidTransaction_thenFail() {
        String transactionId = "TRAN-1234";
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void doGetRegistrationStatus_withEmptyTransactionId_thenFail() {
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus("");
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void doGetRegistrationStatus_withNullTransactionId_thenFail() {
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(null);
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }
}
