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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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

    @Value("${mosip.signup.cookie.max-age}")
    private int cookieMaxAge;

    @Value("${mosip.signup.challenge.resend-attempt}")
    private int resendAttempts;

    @Value("${mosip.signup.challenge.resend-delay}")
    private long resendDelay;

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
            throw new InvalidIdentifierException();
        }
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, verifyChallengeRequest.getChallengeInfo().getChallenge());
        if(!challengeHash.equals(transaction.getChallengeHash())) {
            log.error("Transaction {} : challenge not match", transactionId);
            throw new ChallengeFailedException();
        }
        cacheUtilService.setChallengeVerifiedTransaction(transactionId, transaction);
        VerifyChallengeResponse verifyChallengeResponse = new VerifyChallengeResponse();
        verifyChallengeResponse.setStatus(ActionStatus.SUCCESS);
        log.debug("Transaction {} : verify challenge status {}", transactionId, ActionStatus.SUCCESS);
        return verifyChallengeResponse;
    }

    public RegisterResponse register(RegisterRequest registerRequest, String transactionId) throws SignUpException {

        log.debug("Transaction {} : start do registration", transactionId);
        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeVerifiedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        if(!registerRequest.getUsername().equals(registerRequest.getUserInfo().getPhone())) {
            log.error("Transaction {} : given unsupported username in L1", transactionId);
            throw new SignUpException(ErrorConstants.UNSUPPORTED_USERNAME);
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

    public GenerateChallengeResponse generateChallenge(GenerateChallengeRequest generateChallengeRequest, String transactionId) throws SignUpException {
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
            throw new GenerateChallengeException(ErrorConstants.ACTIVE_CHALLENGE_FOUND);
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
