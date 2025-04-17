/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.util.CaptchaHelper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.api.dto.ProfileDto;
import io.mosip.signup.api.exception.ProfileException;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.*;
import io.mosip.signup.exception.GenerateChallengeException;
import io.mosip.signup.helper.NotificationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static io.mosip.signup.util.SignUpConstants.*;

@Slf4j
@Service
public class RegistrationService {

    @Autowired
    private CacheUtilService cacheUtilService;

    @Autowired
    private ChallengeManagerService challengeManagerService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HttpServletResponse response;

    @Autowired
    private CryptoHelper cryptoHelper;

    @Autowired
    private CaptchaHelper captchaHelper;

    @Autowired
    private ProfileRegistryPlugin profileRegistryPlugin;

    @Value("${mosip.signup.challenge.resend-attempt}")
    private int resendAttempts;

    @Value("${mosip.signup.challenge.verification-attempt}")
    private int verificationAttempts;

    @Value("${mosip.signup.challenge.resend-delay}")
    private long resendDelay;

    @Value("${mosip.signup.challenge.timeout}")
    private long challengeTimeout;

    @Value("${mosip.signup.unauthenticated.txn.timeout}")
    private int unauthenticatedTransactionTimeout;

    @Value("${mosip.signup.verified.txn.timeout}")
    private int registerTransactionTimeout;

    @Value("${mosip.signup.status-check.txn.timeout}")
    private int statusCheckTransactionTimeout;

    @Value("${mosip.signup.send-challenge.captcha-required:true}")
    private boolean captchaRequired;


    /**
     * Generate and regenerate challenge based on the "regenerate" flag in the request.
     * if regenerate is false - always creates a new transaction and set-cookie header is sent in the response.
     * if regenerate is true - expects a valid transaction Id in the cookie
     * @param generateChallengeRequest
     * @param transactionId
     * @return
     * @throws SignUpException
     */
    public GenerateChallengeResponse generateChallenge(GenerateChallengeRequest generateChallengeRequest, String transactionId) throws SignUpException {
        if (captchaRequired)
            captchaHelper.validateCaptcha(generateChallengeRequest.getCaptchaToken());

        String identifier = generateChallengeRequest.getIdentifier();
        RegistrationTransaction transaction = null;

        if(cacheUtilService.isIdentifierBlocked(identifier))
            throw new SignUpException(ErrorConstants.IDENTIFIER_BLOCKED);

        if(!generateChallengeRequest.isRegenerateChallenge()) {
            transactionId = IdentityProviderUtil.createTransactionId(null);
            transaction = new RegistrationTransaction(identifier, generateChallengeRequest.getPurpose());
            //Need to set cookie only when regenerate is false.
            addCookieInResponse(transactionId, unauthenticatedTransactionTimeout);
        }
        else {
            transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
            validateTransaction(transaction, identifier, generateChallengeRequest, transactionId);
            transaction.setVerificationAttempts(0);
        }

        // generate Challenge
        String challenge = challengeManagerService.generateChallenge(transaction);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challenge);
        transaction.setChallengeHash(challengeHash);

        transaction.increaseAttempt();
        if(transaction.getChallengeRetryAttempts() > resendAttempts) {
            //Resend attempts exhausted, block the identifier for configured time.
            cacheUtilService.blockIdentifier(transactionId, transaction.getIdentifier(), "blocked");
        }

        transaction.setLocale(generateChallengeRequest.getLocale());
        cacheUtilService.createUpdateChallengeGeneratedTransaction(transactionId, transaction);

        HashMap<String, String> hashMap = new LinkedHashMap<>();
        hashMap.put("{challenge}", challenge);
        notificationHelper.sendSMSNotification(generateChallengeRequest.getIdentifier(), transaction.getLocale(),
                SEND_OTP_SMS_NOTIFICATION_TEMPLATE_KEY, hashMap);
        return new GenerateChallengeResponse(ActionStatus.SUCCESS);
    }

    public VerifyChallengeResponse verifyChallenge(VerifyChallengeRequest verifyChallengeRequest,
                                                   String transactionId) throws SignUpException {

        log.debug("Transaction {} : start verify challenge", transactionId);
        RegistrationTransaction transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeGeneratedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        if(!transaction.isValidIdentifier(verifyChallengeRequest.getIdentifier())) {
            log.error("Transaction {} : contain identifier not the same with identifier user request", transactionId);
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }

        for (ChallengeInfo challengeInfo: verifyChallengeRequest.getChallengeInfo()){
            validateChallengeFormatAndType(challengeInfo);
        }

        Optional<ChallengeInfo> otpChallengeInfo = verifyChallengeRequest.getChallengeInfo()
                .stream().filter(challengeInfo -> challengeInfo.getType().equals("OTP")).findFirst();

        if(otpChallengeInfo.isEmpty()) throw new SignUpException(ErrorConstants.INVALID_CHALLENGE);

        if(transaction.getLastRetryToNow() >= challengeTimeout) {
            throw new SignUpException(ErrorConstants.CHALLENGE_EXPIRED);
        }
        if(transaction.getVerificationAttempts() >= verificationAttempts){
            throw new SignUpException(ErrorConstants.TOO_MANY_VERIFY_ATTEMPTS);
        }

        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                otpChallengeInfo.get().getChallenge());
        if(!challengeHash.equals(transaction.getChallengeHash())) {
            transaction.incrementVerificationAttempt();
            cacheUtilService.createUpdateChallengeGeneratedTransaction(transactionId, transaction);
            log.error("Transaction {} : challenge not match", transactionId);
            throw new ChallengeFailedException();
        }

        fetchAndCheckIdentity(transaction, verifyChallengeRequest);

        //After successful verification of the user, change the transactionId
        String verifiedTransactionId = IdentityProviderUtil.createTransactionId(null);
        addVerifiedCookieInResponse(verifiedTransactionId, registerTransactionTimeout+statusCheckTransactionTimeout);

        cacheUtilService.setChallengeVerifiedTransaction(transactionId, verifiedTransactionId, transaction);
        log.debug("Transaction {} : verify challenge status {}", verifiedTransactionId, ActionStatus.SUCCESS);
        return new VerifyChallengeResponse(ActionStatus.SUCCESS);
    }

    public RegisterResponse register(RegisterRequest registerRequest, String transactionId) throws SignUpException {
        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeVerifiedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        if (!Purpose.REGISTRATION.equals(transaction.getPurpose())) {
            log.error("Transaction {} : is not for Registration Purpose", transactionId);
            throw new SignUpException(ErrorConstants.UNSUPPORTED_PURPOSE);
        }
        if(!CONSENT_AGREE.equals(registerRequest.getConsent())) {
            log.error("Transaction {} : disagrees consent", transactionId);
            throw new SignUpException(ErrorConstants.CONSENT_REQUIRED);
        }

        try {
            ProfileDto profileDto = new ProfileDto();
            profileDto.setIndividualId(registerRequest.getUsername());
            profileDto.setActive(true);
            profileDto.setConsent(registerRequest.getConsent());
            profileDto.setIdentity(registerRequest.getUserInfo());
            profileRegistryPlugin.createProfile(transaction.getApplicationId(), profileDto);
        }
        catch (ProfileException exception) {
            throw new SignUpException(exception.getErrorCode());
        }

        transaction.setRegistrationStatus(ProfileCreateUpdateStatus.PENDING);
        cacheUtilService.setStatusCheckTransaction(transactionId, transaction);

        String locale = registerRequest.getLocale() == null ? transaction.getLocale() : registerRequest.getLocale();
        notificationHelper.sendSMSNotificationAsync(registerRequest.getUsername(), locale,
                REGISTRATION_SMS_NOTIFICATION_TEMPLATE_KEY, null);

        RegisterResponse registration = new RegisterResponse();
        registration.setStatus(ActionStatus.PENDING);
        log.debug("Transaction {} : registration status {}", transactionId, ProfileCreateUpdateStatus.PENDING);
        return registration;
    }

    public RegistrationStatusResponse updatePassword(ResetPasswordRequest resetPasswordRequest,
                                                     String transactionId) throws SignUpException {
        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            throw new InvalidTransactionException();
        }
        if(!transaction.isValidIdentifier(resetPasswordRequest.getIdentifier().toLowerCase(Locale.ROOT))) {
            log.error("reset password failed: invalid identifier");
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }
        if(!transaction.getPurpose().equals(Purpose.RESET_PASSWORD)) {
            log.error("reset password failed: purpose mismatch in transaction");
            throw new SignUpException(ErrorConstants.UNSUPPORTED_PURPOSE);
        }

        try {
            ProfileDto profileDto = new ProfileDto();
            profileDto.setIndividualId(cryptoHelper.symmetricDecrypt(transaction.getUin()));
            Map<String, String> map = new HashMap<>();
            map.put("password", resetPasswordRequest.getPassword());
            profileDto.setIdentity(objectMapper.valueToTree(map));
            profileRegistryPlugin.validate("UPDATE", profileDto);
            profileRegistryPlugin.updateProfile(transaction.getApplicationId(), profileDto);
        }catch (ProfileException exception) {
            throw new SignUpException(exception.getErrorCode());
        }

        transaction.setRegistrationStatus(ProfileCreateUpdateStatus.PENDING);
        cacheUtilService.setStatusCheckTransaction(transactionId, transaction);

        String locale = resetPasswordRequest.getLocale() == null ? transaction.getLocale() : resetPasswordRequest.getLocale();
        notificationHelper.sendSMSNotificationAsync(resetPasswordRequest.getIdentifier(), locale,
                FORGOT_PASSWORD_SMS_NOTIFICATION_TEMPLATE_KEY, null);

        RegistrationStatusResponse resetPassword = new RegistrationStatusResponse();
        resetPassword.setStatus(ProfileCreateUpdateStatus.PENDING);
        return resetPassword;
    }

    public RegistrationStatusResponse getRegistrationStatus(String transactionId)
            throws SignUpException {
        if (transactionId == null || transactionId.isEmpty())
            throw new InvalidTransactionException();

        RegistrationTransaction transaction = cacheUtilService.getStatusCheckTransaction(
                transactionId);
        if (transaction == null)
            throw new InvalidTransactionException();

        if(!ProfileCreateUpdateStatus.getEndStatuses().contains(transaction.getRegistrationStatus())) {
            ProfileCreateUpdateStatus registrationStatus = profileRegistryPlugin.getProfileCreateUpdateStatus(transaction.getApplicationId());
            transaction.setRegistrationStatus(registrationStatus);
            cacheUtilService.updateStatusCheckTransaction(transactionId, transaction);
        }

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(transaction.getRegistrationStatus());
        return registrationStatusResponse;
    }

    public JsonNode getSchema(String transactionId) {
        if (transactionId == null || transactionId.isEmpty())
            throw new InvalidTransactionException();

        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeVerifiedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        return profileRegistryPlugin.getUISpecification();
    }

    private void fetchAndCheckIdentity(RegistrationTransaction registrationTransaction,
                                       VerifyChallengeRequest verifyChallengeRequest) {
        ProfileDto profileDto = profileRegistryPlugin.getProfile(verifyChallengeRequest.getIdentifier());

        switch (registrationTransaction.getPurpose()) {
            case REGISTRATION:
                if(profileDto.isActive() || profileDto.getIdentity() != null)
                    throw new SignUpException(ErrorConstants.IDENTIFIER_ALREADY_REGISTERED);
                break;
            case RESET_PASSWORD:
                if(!profileDto.isActive() || profileDto.getIdentity() == null)
                    throw new SignUpException(ErrorConstants.IDENTIFIER_NOT_FOUND);

                validateKBAChallenge(profileDto, verifyChallengeRequest);
                //set UIN in the cache to be further used for update UIN endpoint
                registrationTransaction.setUin(cryptoHelper.symmetricEncrypt(profileDto.getIndividualId()));
                break;
            default:
                throw new SignUpException(ErrorConstants.UNSUPPORTED_PURPOSE);
        }
    }

    private void validateKBAChallenge(ProfileDto profileDto, VerifyChallengeRequest verifyChallengeRequest) {
        Optional<ChallengeInfo> kbaChallenge = verifyChallengeRequest.getChallengeInfo().stream()
                .filter(challengeInfo -> challengeInfo.getType().equals("KBI"))
                .findFirst();
        if (kbaChallenge.isEmpty()){
            throw new SignUpException(ErrorConstants.KBI_CHALLENGE_NOT_FOUND);
        }

        try {
            String json = new String(Base64.getUrlDecoder().decode(kbaChallenge.get().getChallenge().getBytes()));
            if (!profileRegistryPlugin.isMatch(profileDto.getIdentity(), objectMapper.readTree(json))){
                throw new SignUpException(ErrorConstants.KNOWLEDGEBASE_MISMATCH);
            }
        }catch (JsonProcessingException exception){
            throw new SignUpException(ErrorConstants.INVALID_KBI_CHALLENGE);
        }
    }


    private void validateTransaction(RegistrationTransaction transaction, String identifier,
                                     GenerateChallengeRequest generateChallengeRequest, String transactionId) {
        if(transaction == null) {
            log.error("generate-challenge failed: validate transaction null");
            throw new InvalidTransactionException();
        }

        if(!transaction.isValidIdentifier(identifier)) {
            log.error("generate-challenge failed: invalid identifier");
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }

        if(transaction.getLastRetryToNow() <= resendDelay) {
            log.error("generate-challenge failed: too early attempts");
            throw new GenerateChallengeException(ErrorConstants.TOO_EARLY_ATTEMPT);
        }

        if(!transaction.getPurpose().equals(generateChallengeRequest.getPurpose())) {
            log.error("generate-challenge failed: purpose mismatch");
            throw new GenerateChallengeException(ErrorConstants.INVALID_PURPOSE);
        }
    }


    private void addCookieInResponse(String transactionId, int maxAge) {
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, transactionId);
        cookie.setMaxAge(maxAge); // 60 = 1 minute
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void addVerifiedCookieInResponse(String transactionId, int maxAge) {
        Cookie cookie = new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, transactionId);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        Cookie unsetCookie = new Cookie(SignUpConstants.TRANSACTION_ID, "");
        unsetCookie.setMaxAge(0);
        unsetCookie.setHttpOnly(true);
        unsetCookie.setSecure(true);
        unsetCookie.setPath("/");
        response.addCookie(unsetCookie);
    }


    private void validateChallengeFormatAndType(ChallengeInfo challengeInfo) throws SignUpException{
        if (challengeInfo.getType().equals("OTP") && !challengeInfo.getFormat().equals("alpha-numeric")){
            throw new SignUpException(ErrorConstants.CHALLENGE_FORMAT_AND_TYPE_MISMATCH);
        }

        if (challengeInfo.getType().equals("KBI") && !challengeInfo.getFormat().equals("base64url-encoded-json")){
            throw new SignUpException(ErrorConstants.CHALLENGE_FORMAT_AND_TYPE_MISMATCH);
        }
    }
}
