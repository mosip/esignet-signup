package io.mosip.signup.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.util.RegistrationStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Before;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletResponse;

import io.mosip.esignet.core.exception.EsignetException;

import java.io.IOException;
import java.time.LocalDateTime;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationServiceTest {
    @InjectMocks
    RegistrationService registrationService;

    @Mock
    CacheUtilService cacheUtilService;

    @Mock
    ChallengeManagerService challengeManagerService;

    @Mock
    HttpServletResponse response;

    @Mock
    GoogleRecaptchaValidatorService googleRecaptchaValidatorService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws IOException {
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
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", "");
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
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", "");
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
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", "");
        registrationTransaction.setChallengeHash(challengeInfo.getChallenge());
        registrationTransaction.setIdentifier("failed");
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        }catch (InvalidIdentifierException invalidIdentifierException){
            Assert.assertEquals("invalid_identifier", invalidIdentifierException.getErrorCode());
        }
    }

    // Generate Challenge OTP test cases
    @Test
    public void doGenerateChallenge_withoutTransactionId_thenPass() throws SignUpException, IOException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        when(challengeManagerService.generateChallenge(any())).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

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
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, transactionId);
        transaction.setLastRetryAt(LocalDateTime.now().minusSeconds(40));

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(challengeManagerService.generateChallenge(transaction)).thenReturn("1111");
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        GenerateChallengeResponse generateChallengeResponse =
                registrationService.generateChallenge(
                        generateChallengeRequest, transactionId);
        Assert.assertNotNull(generateChallengeResponse);
        Assert.assertEquals("SUCCESS", generateChallengeResponse.getStatus());
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
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, transactionId);

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
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(other_identifier, transactionId);

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (InvalidIdentifierException ex) {
            Assert.assertEquals("invalid_identifier", ex.getErrorCode());
        }
    }

    @Test
    public void doGenerateChallenge_withTooManyAttemptTransactionId_thenFail() throws SignUpException {
        String identifier = "+85577410541";
        GenerateChallengeRequest generateChallengeRequest = new GenerateChallengeRequest();
        generateChallengeRequest.setIdentifier(identifier);
        generateChallengeRequest.setCaptchaToken("mock-captcha");
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, transactionId);
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
        String transactionId = "TRAN-1234";
        RegistrationTransaction transaction = new RegistrationTransaction(identifier, transactionId);
        transaction.increaseAttempt();

        when(cacheUtilService.getChallengeGeneratedTransaction(transactionId)).thenReturn(transaction);
        when(googleRecaptchaValidatorService.validateCaptcha(
                generateChallengeRequest.getCaptchaToken())).thenReturn(true);

        try {
            registrationService.generateChallenge(generateChallengeRequest, transactionId);
            Assert.fail();
        } catch (GenerateChallengeException ex) {
            Assert.assertEquals("too_early_attempts", ex.getErrorCode());
        }
    }

    @Test
    public void doGetRegistrationStatus_withCompletedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", "TRAN_ID");
        registrationTransaction.setRegistrationStatus(RegistrationStatus.COMPLETED);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.COMPLETED);
    }

    @Test
    public void doGetRegistrationStatus_withPendingTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", "TRAN_ID");
        registrationTransaction.setRegistrationStatus(RegistrationStatus.PENDING);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.PENDING);
    }

    @Test
    public void doGetRegistrationStatus_withFailedTransaction_thenPass() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85577410541", "TRAN_ID");
        registrationTransaction.setRegistrationStatus(RegistrationStatus.FAILED);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
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
