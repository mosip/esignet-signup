package io.mosip.signup.services;

import io.mosip.signup.dto.RegistrationStatusResponse;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.dto.VerifyChallengeRequest;
import io.mosip.signup.dto.VerifyChallengeResponse;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ActionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    @Autowired
    CacheUtilService cacheUtilService;

    public VerifyChallengeResponse verifyChallenge(VerifyChallengeRequest verifyChallengeRequest,
                                                   String transactionId) throws SignUpException {
        RegistrationTransaction transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
        if(transaction == null)
            throw new InvalidTransactionException();
        if(!verifyChallengeRequest.getIdentifier().equals(transaction.getIdentifier()))
            throw new InvalidIdentifierException();
        if(!verifyChallengeRequest.getChallengeInfo().getChallenge().equals(transaction.getOtp()))
            throw new ChallengeFailedException();
        cacheUtilService.setChallengeVerifiedTransaction(transactionId, transaction);
        VerifyChallengeResponse verifyChallengeResponse = new VerifyChallengeResponse();
        verifyChallengeResponse.setStatus(ActionStatus.SUCCESS);
        return verifyChallengeResponse;
    }

    public RegistrationStatusResponse getRegistrationStatus(String transactionId)
            throws SignUpException {
        if (transactionId == null || transactionId.isEmpty())
            throw new InvalidTransactionException();

        RegistrationTransaction registrationTransaction = cacheUtilService.getRegisteredTransaction(
                transactionId);
        if (registrationTransaction == null)
            throw new InvalidTransactionException();

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(registrationTransaction.getRegistrationStatus());
        return registrationStatusResponse;
    }
}
