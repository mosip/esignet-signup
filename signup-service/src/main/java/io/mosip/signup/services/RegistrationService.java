package io.mosip.signup.services;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidIdentifierException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ActionStatus;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.RegistrationStatus;
import io.mosip.signup.exception.CaptchaException;
import io.mosip.signup.util.SignUpConstants;
import io.mosip.signup.exception.GenerateChallengeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.CompletableFuture;

import static io.mosip.signup.util.SignUpConstants.CONSENT_DISAGREE;

@Slf4j
@Service
public class RegistrationService {

    @Autowired
    private CacheUtilService cacheUtilService;

    @Autowired
    private GoogleRecaptchaValidatorService googleRecaptchaValidatorService;

    @Autowired
    private ChallengeManagerService challengeManagerService;

    @Autowired
    HttpServletResponse response;

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Value("${mosip.signup.id-schema.version}")
    private float idSchemaVersion;

    @Value("${mosip.signup.add-identity.request.id}")
    private String addIdentityRequestID;

    @Value("${mosip.signup.add-identity.request.version}")
    private String addIdentityRequestVersion;

    @Value("${mosip.signup.add-identity.endpoint}")
    private String addIdentityEndpoint;

    @Value("${mosip.signup.generate-hash.endpoint}")
    private String generateHashEndpoint;

    @Value("${mosip.signup.get-uin.endpoint}")
    private String getUinEndpoint;

    @Value("${mosip.signup.send-notification.endpoint}")
    private String sendNotificationEndpoint;

    @Value("${mosip.signup.challenge.resend-attempt}")
    private int resendAttempts;

    @Value("${mosip.signup.challenge.resend-delay}")
    private long resendDelay;

    @Value("${mosip.signup.otp-registration.sms.khm}")
    private String otpRegistrationKhmMessage;

    @Value("${mosip.signup.otp-registration.sms.eng}")
    private String otpRegistrationEngMessage;

    @Value("${mosip.signup.successfully.registration.sms.khm}")
    private String registrationSuccessKhmMessage;

    @Value("${mosip.signup.successfully.registration.sms.eng}")
    private String registrationSuccessEngMessage;

    @Value("${mosip.signup.unauthenticated.txn.timeout}")
    private int unauthenticatedTransactionTimeout;

    @Value("${mosip.signup.register.txn.timeout}")
    private int registerTransactionTimeout;

    @Value("${mosip.signup.status-check.txn.timeout}")
    private int statusCheckTransactionTimeout;

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
        if (!googleRecaptchaValidatorService.validateCaptcha(generateChallengeRequest.getCaptchaToken())) {
            log.error("generate-challenge failed: invalid captcha");
            throw new CaptchaException(ErrorConstants.INVALID_CAPTCHA);
        }

        String identifier = generateChallengeRequest.getIdentifier();
        RegistrationTransaction transaction = null;

        if(generateChallengeRequest.isRegenerate() == false) {
            transactionId = IdentityProviderUtil.createTransactionId(null);
            transaction = new RegistrationTransaction(identifier);
            //Need to set cookie only when regenerate is false.
            addCookieInResponse(transactionId, unauthenticatedTransactionTimeout);
        }
        else {
            transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
            validateTransaction(transaction, identifier);
        }

        // generate Challenge
        String challenge = challengeManagerService.generateChallenge(transaction);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challenge);
        transaction.setChallengeHash(challengeHash);
        transaction.increaseAttempt();
        transaction.setLocale(generateChallengeRequest.getLocale());
        cacheUtilService.setChallengeGeneratedTransaction(transactionId, transaction);

        sendNotificationAsync(generateChallengeRequest.getIdentifier(), transaction.getLocale(), challenge)
                .thenAccept(notificationResponseRestResponseWrapper -> {
                    log.debug("Notification response -> {}", notificationResponseRestResponseWrapper);
                });
        return new GenerateChallengeResponse(ActionStatus.SUCCESS);
    }

    public VerifyChallengeResponse verifyChallenge(VerifyChallengeRequest verifyChallengeRequest,
                                                   String transactionId) throws SignUpException {

        log.debug("Transaction {} : start verify challenge", transactionId);
        RegistrationTransaction transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
        if(transaction == null){
            log.error("Transaction {} : not found in ChallengeGeneratedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        if(!verifyChallengeRequest.getIdentifier().equals(transaction.getIdentifier())) {
            log.error("Transaction {} : contain identifier not the same with identifier user request", transactionId);
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, verifyChallengeRequest.getChallengeInfo().getChallenge());
        if(!challengeHash.equals(transaction.getChallengeHash())) {
            log.error("Transaction {} : challenge not match", transactionId);
            throw new ChallengeFailedException();
        }

        //After successful verification of the user, change the transactionId
        transactionId = IdentityProviderUtil.createTransactionId(null);
        addVerifiedCookieInResponse(transactionId, registerTransactionTimeout+statusCheckTransactionTimeout);

        cacheUtilService.setChallengeVerifiedTransaction(transactionId, transaction);
        log.debug("Transaction {} : verify challenge status {}", transactionId, ActionStatus.SUCCESS);
        return new VerifyChallengeResponse(ActionStatus.SUCCESS);
    }

    public RegisterResponse register(RegisterRequest registerRequest, String transactionId) throws SignUpException {

        log.debug("Transaction {} : start do registration", transactionId);
        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeVerifiedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        if(!transaction.getIdentifier().equals(registerRequest.getUsername()) ||
                !registerRequest.getUsername().equals(registerRequest.getUserInfo().getPhone())) {
            log.error("Transaction {} : given unsupported username in L1", transactionId);
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }
        if(registerRequest.getConsent().equals(CONSENT_DISAGREE)) {
            log.error("Transaction {} : disagrees consent", transactionId);
            throw new SignUpException(ErrorConstants.CONSENT_REQUIRED);
        }

        saveIdentityData(registerRequest, transactionId, transaction.getApplicationId());

        transaction.setRegistrationStatus(RegistrationStatus.PENDING);
        cacheUtilService.setRegisteredTransaction(transactionId, transaction);

        RegisterResponse registration = new RegisterResponse();
        registration.setStatus(ActionStatus.PENDING);
        log.debug("Transaction {} : registration status {}", transactionId, RegistrationStatus.PENDING);
        return registration;
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

    private void saveIdentityData(RegisterRequest registerRequest, String transactionId, String applicationId) throws SignUpException{

        UserInfoMap userInfoMap = registerRequest.getUserInfo();

        Identity identity = new Identity();
        identity.setPreferredLang(userInfoMap.getPreferredLang());
        identity.setPhone(userInfoMap.getPhone());
        identity.setFullName(userInfoMap.getFullName());
        identity.setIDSchemaVersion(idSchemaVersion);
        identity.setRegistrationType("L1");

        String uin = getUniqueIdentifier(transactionId);
        identity.setUIN(uin);

        Password password = generateSaltedHash(registerRequest.getPassword(), transactionId);
        identity.setPassword(password);

        AddIdentityRequest addIdentityRequest = new AddIdentityRequest();
        addIdentityRequest.setRegistrationId(applicationId);
        addIdentityRequest.setIdentity(identity);

        addIdentity(addIdentityRequest, transactionId);
    }

    private void addIdentity(AddIdentityRequest addIdentityRequest, String transactionId) throws SignUpException{

        RestRequestWrapper<AddIdentityRequest> restRequest = new RestRequestWrapper<>();
        restRequest.setId(addIdentityRequestID);
        restRequest.setVersion(addIdentityRequestVersion);
        restRequest.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequest.setRequest(addIdentityRequest);

        log.debug("Transaction {} : start add identity", transactionId);
        HttpEntity<RestRequestWrapper<AddIdentityRequest>> resReq = new HttpEntity<>(restRequest);
        RestResponseWrapper<AddIdentityResponse> restResponseWrapper = selfTokenRestTemplate.exchange(addIdentityEndpoint, HttpMethod.POST, resReq, new ParameterizedTypeReference<RestResponseWrapper<AddIdentityResponse>>() {}).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                restResponseWrapper.getResponse().getStatus().equals("ACTIVATED")) {
            return;
        }

        log.error("Transaction {} : Add identity failed with response {}", transactionId, restResponseWrapper);
        throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.ADD_IDENTITY_FAILED);
    }

    private Password generateSaltedHash(String password, String transactionId) throws SignUpException{

        RestRequestWrapper<Password.PasswordPlaintext> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequestWrapper.setRequest(new Password.PasswordPlaintext(password));

        HttpEntity<RestRequestWrapper<Password.PasswordPlaintext>> resReq = new HttpEntity<>(restRequestWrapper);
        log.debug("Transaction {} : Generate salted hash started", transactionId);
        RestResponseWrapper<Password.PasswordHash> restResponseWrapper = selfTokenRestTemplate.exchange(generateHashEndpoint, HttpMethod.POST, resReq, new ParameterizedTypeReference<RestResponseWrapper<Password.PasswordHash>>(){}).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                !StringUtils.isEmpty(restResponseWrapper.getResponse().getHashValue()) &&
                !StringUtils.isEmpty(restResponseWrapper.getResponse().getSalt())) {
            return new Password(restResponseWrapper.getResponse().getHashValue(),
                    restResponseWrapper.getResponse().getSalt());
        }

        log.error("Transaction {} : Generate salted hash failed with response {}", transactionId, restResponseWrapper);
        throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.HASH_GENERATE_FAILED);
    }

    private String getUniqueIdentifier(String transactionId) throws SignUpException {

        RestResponseWrapper<UINResponse> restResponseWrapper = selfTokenRestTemplate.exchange(getUinEndpoint,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<RestResponseWrapper<UINResponse>>() {}).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                !StringUtils.isEmpty(restResponseWrapper.getResponse().getUIN()) ) {
            return restResponseWrapper.getResponse().getUIN();
        }

        log.error("Transaction {} : Get unique identifier failed with response {}", transactionId, restResponseWrapper);
        throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.GET_UIN_FAILED);
    }

    @Async
    private CompletableFuture<RestResponseWrapper<NotificationResponse>> sendNotificationAsync
            (String number, String locale, String challenge) {

        NotificationRequest notificationRequest = new NotificationRequest(number.substring(4),
                locale == null || locale.equals("khm") ? otpRegistrationKhmMessage : otpRegistrationEngMessage );

        notificationRequest.setMessage(notificationRequest.getMessage().replace("XXXXXX", challenge));

        RestRequestWrapper<NotificationRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequest(notificationRequest);

        return CompletableFuture.supplyAsync(() -> selfTokenRestTemplate
                .exchange(sendNotificationEndpoint,
                        HttpMethod.POST,
                        new HttpEntity<>(restRequestWrapper),
                        new ParameterizedTypeReference<RestResponseWrapper<NotificationResponse>>(){}).getBody());
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
            throw new GenerateChallengeException(ErrorConstants.ACTIVE_CHALLENGE_FOUND);
        }
    }

    private void addCookieInResponse(String transactionId, int maxAge) {
        Cookie cookie = new Cookie(SignUpConstants.TRANSACTION_ID, transactionId);
        cookie.setMaxAge(maxAge); // 60 = 1 minute
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private void addVerifiedCookieInResponse(String transactionId, int maxAge) {
        Cookie cookie = new Cookie(SignUpConstants.VERIFIED_TRANSACTION_ID, transactionId);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        Cookie unsetCookie = new Cookie(SignUpConstants.TRANSACTION_ID, "");
        unsetCookie.setMaxAge(0);
        response.addCookie(unsetCookie);
    }
}
