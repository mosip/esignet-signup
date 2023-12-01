package io.mosip.signup.services;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ActionStatus;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import io.mosip.signup.exception.CaptchaException;
import io.mosip.signup.exception.GenerateChallengeException;

@Service
@Slf4j
public class RegistrationService {

    @Autowired
    CacheUtilService cacheUtilService;

    @Autowired
    HttpServletResponse response;

    @Autowired
    GoogleRecaptchaValidatorService googleRecaptchaValidatorService;

    @Autowired
    ChallengeManagerService challengeManagerService;

    @Value("${mosip.signup.cookie.max-age}")
    private int cookieMaxAge;

    @Value("${mosip.signup.challenge.resend-attempt}")
    private int resendAttempts;

    @Value("${mosip.signup.challenge.resend-delay}")
    private long resendDelay;

    public VerifyChallengeResponse verifyChallenge(VerifyChallengeRequest verifyChallengeRequest,
                                                   String transactionId) throws SignUpException {
        RegistrationTransaction transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
        if(transaction == null) {
            log.error("verify-challenge failed: transaction null");
            throw new InvalidTransactionException();
        }
        if(!verifyChallengeRequest.getIdentifier().equals(transaction.getIdentifier())) {
            log.error("verify-challenge failed: invalid identifier");
            throw new InvalidIdentifierException();
        }

        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, verifyChallengeRequest.getChallengeInfo().getChallenge());
        if(!challengeHash.equals(transaction.getChallengeHash())) {
            log.error("verify-challenge failed: challenge not match");
            throw new ChallengeFailedException();
        }

        cacheUtilService.setChallengeVerifiedTransaction(transactionId, transaction);
        VerifyChallengeResponse verifyChallengeResponse = new VerifyChallengeResponse();
        verifyChallengeResponse.setStatus(ActionStatus.SUCCESS);
        return verifyChallengeResponse;
    }

    public GenerateChallengeResponse generateChallenge(
            GenerateChallengeRequest generateChallengeRequest, String transactionId)
            throws SignUpException {
        if (!googleRecaptchaValidatorService.validateCaptcha(generateChallengeRequest.getCaptchaToken())) {
            log.error("generate-challenge failed: invalid captcha");
            throw new CaptchaException(ErrorConstants.INVALID_CAPTCHA);
        }

        String identifier = generateChallengeRequest.getIdentifier();
        RegistrationTransaction transaction = null;
        if (!transactionId.isEmpty()) {
            transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
            validateTransaction(transaction, identifier);
        }

        if(transaction == null) {
            transactionId = IdentityProviderUtil.createTransactionId(null);
            transaction = new RegistrationTransaction(identifier, transactionId);
        }

        // generate Challenge
        String challenge = challengeManagerService.generateChallenge(transaction);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challenge);
        addCookieResponse(transactionId);
        transaction.setChallengeHash(challengeHash);
        transaction.increaseAttempt();
        cacheUtilService.setChallengeGeneratedTransaction(transactionId, transaction);
        return new GenerateChallengeResponse(ActionStatus.SUCCESS);
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

    private void validateTransaction(RegistrationTransaction transaction, String identifier) {
        if(transaction == null) {
            log.error("generate-challenge failed: validate transaction null");
            throw new InvalidTransactionException();
        }

        if(!transaction.getIdentifier().equals(identifier)) {
            log.error("generate-challenge failed: invalid identifier");
            throw new InvalidIdentifierException();
        }

        if(transaction.getChallengeRetryAttempts() >= resendAttempts) {
            log.error("generate-challenge failed: too many attempts");
            throw new GenerateChallengeException(ErrorConstants.TOO_MANY_ATTEMPTS);
        }

        if(transaction.getLastRetryToNow() <= resendDelay) {
            log.error("generate-challenge failed: too early attempts");
            throw new GenerateChallengeException(ErrorConstants.TOO_EARLY_ATTEMPTS);
        }
    }

    private void addCookieResponse(String transactionId) {
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, transactionId);
        cookie.setMaxAge(cookieMaxAge); // 60 = 1 minute
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
