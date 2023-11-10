package io.mosip.signup.services;

import io.mosip.signup.dto.ChallengeInfo;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.dto.VerifyChallengeRequest;
import io.mosip.signup.dto.VerifyChallengeResponse;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
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
}
