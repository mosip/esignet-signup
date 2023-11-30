package io.mosip.signup.services;

import io.mosip.signup.dto.*;
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

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RegistrationServiceTest {
    @InjectMocks
    RegistrationService registrationService;
    @Mock
    CacheUtilService cacheUtilService;

    @Test
    public void verifyChallenge_thenPass() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp(challengeInfo.getChallenge());
        registrationTransaction.setIdentifier(verifyChallengeRequest.getIdentifier());
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        VerifyChallengeResponse verifyChallengeResponse = registrationService.
                verifyChallenge(verifyChallengeRequest, mockTransactionId);
        Assert.assertNotNull(verifyChallengeResponse);
        Assert.assertEquals("SUCCESS", verifyChallengeResponse.getStatus());
    }

    @Test
    public void verifyChallenge_withInvalidTransaction_throwsException() throws Exception{
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
    public void verifyChallenge_withChallengeFailed_throwsException() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("failed");
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
    public void verifyChallenge_withInvalidIdentifier_throwsException() throws Exception{
        ChallengeInfo challengeInfo = new ChallengeInfo();
        challengeInfo.setFormat("alpha-numeric");
        challengeInfo.setChallenge("123456");

        VerifyChallengeRequest verifyChallengeRequest = new VerifyChallengeRequest();
        verifyChallengeRequest.setIdentifier("123456");
        verifyChallengeRequest.setChallengeInfo(challengeInfo);

        String mockTransactionId = "mock-transactionId";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp(challengeInfo.getChallenge());
        registrationTransaction.setIdentifier("failed");
        when(cacheUtilService.getChallengeGeneratedTransaction(mockTransactionId)).thenReturn(registrationTransaction);

        try {
            registrationService.verifyChallenge(verifyChallengeRequest, mockTransactionId);
            Assert.fail();
        }catch (InvalidIdentifierException invalidIdentifierException){
            Assert.assertEquals("invalid_identifier", invalidIdentifierException.getErrorCode());
        }
    }

    @Test
    public void getRegistrationStatus_withCompletedTransaction() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setRegistrationStatus(RegistrationStatus.COMPLETED);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.COMPLETED);
    }

    @Test
    public void getRegistrationStatus_withPendingTransaction() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setRegistrationStatus(RegistrationStatus.PENDING);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.PENDING);
    }

    @Test
    public void getRegistrationStatus_withFailedTransaction() {
        String transactionId = "TRAN-1234";
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setRegistrationStatus(RegistrationStatus.FAILED);
        when(cacheUtilService.getRegisteredTransaction(transactionId)).thenReturn(registrationTransaction);
        RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);

        Assert.assertNotNull(registrationStatusResponse);
        Assert.assertEquals(registrationStatusResponse.getStatus(), RegistrationStatus.FAILED);
    }

    @Test
    public void getRegistrationStatus_withInvalidTransaction() {
        String transactionId = "TRAN-1234";
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(transactionId);
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void getRegistrationStatus_withEmptyTransactionId() {
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus("");
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }

    @Test
    public void getRegistrationStatus_withNullTransactionId() {
        try {
            RegistrationStatusResponse registrationStatusResponse = registrationService.getRegistrationStatus(null);
            Assert.fail();
        } catch (InvalidTransactionException exception) {
            Assert.assertEquals("invalid_transaction", exception.getErrorCode());
        }
    }
}
