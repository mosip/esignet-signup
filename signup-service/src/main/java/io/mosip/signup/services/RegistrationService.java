package io.mosip.signup.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.ChallengeFailedException;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.*;
import io.mosip.signup.exception.CaptchaException;
import io.mosip.signup.exception.GenerateChallengeException;
import io.mosip.signup.helper.NotificationHelper;
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

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static io.mosip.signup.util.SignUpConstants.*;

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
    private NotificationHelper notificationHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HttpServletResponse response;

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Autowired
    private CryptoHelper cryptoHelper;

    @Value("${mosip.signup.supported.challenge.otp.length}")
    private int otpLength;

    @Value("${mosip.signup.id-schema.version}")
    private float idSchemaVersion;

    @Value("${mosip.signup.add-identity.request.id}")
    private String addIdentityRequestID;

    @Value("${mosip.signup.update-identity.request.id}")
    private String updateIdentityRequestID;

    @Value("${mosip.signup.identity.request.version}")
    private String identityRequestVersion;

    @Value("${mosip.signup.identity.endpoint}")
    private String identityEndpoint;

    @Value("${mosip.signup.get-identity.endpoint}")
    private String getIdentityEndpoint;

    @Value("${mosip.signup.generate-hash.endpoint}")
    private String generateHashEndpoint;

    @Value("${mosip.signup.get-uin.endpoint}")
    private String getUinEndpoint;

    @Value("${mosip.signup.send-notification.endpoint}")
    private String sendNotificationEndpoint;

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

    @Value("${mosip.signup.get-registration-status.endpoint}")
    private String getRegistrationStatusEndpoint;

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

        if(cacheUtilService.isIdentifierBlocked(identifier))
            throw new SignUpException(ErrorConstants.IDENTIFIER_BLOCKED);

        if(generateChallengeRequest.isRegenerate() == false) {
            transactionId = IdentityProviderUtil.createTransactionId(null);
            transaction = new RegistrationTransaction(identifier, generateChallengeRequest.getPurpose());
            //Need to set cookie only when regenerate is false.
            addCookieInResponse(transactionId, unauthenticatedTransactionTimeout);
        }
        else {
            transaction = cacheUtilService.getChallengeGeneratedTransaction(transactionId);
            validateTransaction(transaction, identifier, generateChallengeRequest);
            transaction.setVerificationAttempts(0);
        }

        // generate Challenge
        String challenge = challengeManagerService.generateChallenge(transaction);
        String challengeHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, challenge);
        transaction.setChallengeHash(challengeHash);
        transaction.increaseAttempt();
        transaction.setLocale(generateChallengeRequest.getLocale());
        cacheUtilService.createUpdateChallengeGeneratedTransaction(transactionId, transaction);

        //Resend attempts exhausted, block the identifier for configured time.
        if(transaction.getChallengeRetryAttempts() > resendAttempts)
            cacheUtilService.blockIdentifier(transactionId, transaction.getIdentifier(), "blocked");

        notificationHelper.sendSMSNotificationAsync(generateChallengeRequest.getIdentifier(), transaction.getLocale(),
                        SEND_OTP_SMS_NOTIFICATION_TEMPLATE_KEY, new HashMap<>(){{put("{challenge}", challenge);}})
                .thenAccept(notificationResponseRestResponseWrapper -> {
                    log.debug("Notification response -> {}", notificationResponseRestResponseWrapper);
                });
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
            log.error("Transaction {} : challenge not match", transactionId);
            throw new ChallengeFailedException();
        }

        fetchAndCheckIdentity(transactionId, transaction, verifyChallengeRequest);

        //After successful verification of the user, change the transactionId
        String verifiedTransactionId = IdentityProviderUtil.createTransactionId(null);
        addVerifiedCookieInResponse(verifiedTransactionId, registerTransactionTimeout+statusCheckTransactionTimeout);

        cacheUtilService.setChallengeVerifiedTransaction(transactionId, verifiedTransactionId, transaction);
        log.debug("Transaction {} : verify challenge status {}", verifiedTransactionId, ActionStatus.SUCCESS);
        return new VerifyChallengeResponse(ActionStatus.SUCCESS);
    }

    public RegisterResponse register(RegisterRequest registerRequest, String transactionId) throws SignUpException {

        log.debug("Transaction {} : start do registration", transactionId);
        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeVerifiedTransaction cache", transactionId);
            throw new InvalidTransactionException();
        }
        if(!transaction.isValidIdentifier(registerRequest.getUsername()) ||
                !registerRequest.getUsername().equals(registerRequest.getUserInfo().getPhone())) {
            log.error("Transaction {} : given unsupported username in L1", transactionId);
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }
        if (!transaction.getPurpose().equals(Purpose.REGISTRATION)) {
            log.error("Transaction {} : is not for Registration Purpose", transactionId);
            throw new SignUpException(ErrorConstants.UNSUPPORTED_PURPOSE);
        }
        if(registerRequest.getConsent().equals(CONSENT_DISAGREE)) {
            log.error("Transaction {} : disagrees consent", transactionId);
            throw new SignUpException(ErrorConstants.CONSENT_REQUIRED);
        }

        saveIdentityData(registerRequest, transactionId, transaction);

        transaction.setRegistrationStatus(RegistrationStatus.PENDING);
        cacheUtilService.setStatusCheckTransaction(transactionId, transaction);

        notificationHelper.sendSMSNotificationAsync(registerRequest.getUserInfo().getPhone(), transaction.getLocale(),
                        REGISTRATION_SMS_NOTIFICATION_TEMPLATE_KEY, null)
                .thenAccept(notificationResponseRestResponseWrapper -> {
                    log.debug("Notification response -> {}", notificationResponseRestResponseWrapper);
                });

        RegisterResponse registration = new RegisterResponse();
        registration.setStatus(ActionStatus.PENDING);
        log.debug("Transaction {} : registration status {}", transactionId, RegistrationStatus.PENDING);
        return registration;
    }

    public RegistrationStatusResponse updatePassword(ResetPasswordRequest resetPasswordRequest,
                                           String transactionId) throws SignUpException{

        log.debug("Transaction {} : start reset password", transactionId);
        RegistrationTransaction transaction = cacheUtilService.getChallengeVerifiedTransaction(transactionId);
        if(transaction == null) {
            log.error("Transaction {} : not found in ChallengeVerifiedTransaction cache", transactionId);
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

        Identity identity = new Identity();
        identity.setUIN(cryptoHelper.symmetricDecrypt(transaction.getUin()));
        identity.setIDSchemaVersion(idSchemaVersion);

        Password password = generateSaltedHash(resetPasswordRequest.getPassword(), transactionId);
        identity.setPassword(password);
        identity.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC));

        IdentityRequest identityRequest = new IdentityRequest();
        identityRequest.setRegistrationId(transaction.getApplicationId());
        identityRequest.setIdentity(identity);

        RestRequestWrapper<IdentityRequest> restRequest = new RestRequestWrapper<>();
        restRequest.setId(updateIdentityRequestID);
        restRequest.setVersion(identityRequestVersion);
        restRequest.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequest.setRequest(identityRequest);

        log.debug("Transaction {} : start reset password", transactionId);
        HttpEntity<RestRequestWrapper<IdentityRequest>> resReq = new HttpEntity<>(restRequest);
        RestResponseWrapper<IdentityResponse> restResponseWrapper = selfTokenRestTemplate.exchange(identityEndpoint,
                HttpMethod.PATCH,
                resReq,
                new ParameterizedTypeReference<RestResponseWrapper<IdentityResponse>>() {}).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getErrors() != null &&
                !CollectionUtils.isEmpty(restResponseWrapper.getErrors())){
            log.error("Transaction {} : reset password failed with response {}", transactionId, restResponseWrapper);
            throw new SignUpException(restResponseWrapper.getErrors().get(0).getErrorCode());
        }

        if (restResponseWrapper == null || restResponseWrapper.getResponse() == null){
            log.error("Transaction {} : reset password failed with response {}", transactionId, restResponseWrapper);
            throw new SignUpException(ErrorConstants.RESET_PWD_FAILED);
        }

        transaction.getHandlesStatus().put(getHandleRequestId(transaction.getApplicationId(),
                "phone", resetPasswordRequest.getIdentifier()), RegistrationStatus.PENDING);
        transaction.setRegistrationStatus(RegistrationStatus.PENDING);
        cacheUtilService.setStatusCheckTransaction(transactionId, transaction);

        notificationHelper.sendSMSNotificationAsync(resetPasswordRequest.getIdentifier(), transaction.getLocale(),
                        FORGOT_PASSWORD_SMS_NOTIFICATION_TEMPLATE_KEY, null)
                .thenAccept(notificationResponseRestResponseWrapper -> {
                    log.debug("Notification response -> {}", notificationResponseRestResponseWrapper);
                });

        RegistrationStatusResponse resetPassword = new RegistrationStatusResponse();
        resetPassword.setStatus(RegistrationStatus.PENDING);
        return resetPassword;
    }

    public RegistrationStatusResponse getRegistrationStatus(String transactionId)
            throws SignUpException {
        if (transactionId == null || transactionId.isEmpty())
            throw new InvalidTransactionException();

        RegistrationTransaction registrationTransaction = cacheUtilService.getStatusCheckTransaction(
                transactionId);
        if (registrationTransaction == null)
            throw new InvalidTransactionException();

        //For L1 only phone is considered to be handle, later other fields can also be used as handles.
        //We should know the credential issuance status of each handle.
        for(String handleRequestId : registrationTransaction.getHandlesStatus().keySet()) {
            if(!RegistrationStatus.getEndStatuses().contains(registrationTransaction.getHandlesStatus().get(handleRequestId))) {
                RegistrationStatus registrationStatus = getRegistrationStatusFromServer(registrationTransaction.getApplicationId());
                registrationTransaction.getHandlesStatus().put(handleRequestId, registrationStatus);
                //TODO This is temporary fix, we need to remove this field later from registrationTransaction DTO.
                registrationTransaction.setRegistrationStatus(registrationStatus);
                cacheUtilService.updateStatusCheckTransaction(transactionId, registrationTransaction);
            }
        }
        registrationTransaction = cacheUtilService.getStatusCheckTransaction(transactionId);
        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(registrationTransaction.getRegistrationStatus());
        return registrationStatusResponse;
    }

    private void fetchAndCheckIdentity(String transactionId, RegistrationTransaction registrationTransaction,
                                       VerifyChallengeRequest verifyChallengeRequest) {

        String endpoint = String.format(getIdentityEndpoint, verifyChallengeRequest.getIdentifier());
        RestResponseWrapper<IdentityResponse> restResponseWrapper = selfTokenRestTemplate
                .exchange(endpoint, HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestResponseWrapper<IdentityResponse>>() {}).getBody();

        if (restResponseWrapper == null) throw new SignUpException(ErrorConstants.FETCH_IDENTITY_FAILED);

        switch (registrationTransaction.getPurpose()){
            case REGISTRATION: checkIdentityExists(restResponseWrapper);
                break;
            case RESET_PASSWORD: checkActiveIdentityExists(transactionId, restResponseWrapper, registrationTransaction,
                    verifyChallengeRequest);
                break;
            default: throw new SignUpException(ErrorConstants.UNSUPPORTED_PURPOSE);
        }
    }

    private void checkActiveIdentityExists(String transactionId,
                                           RestResponseWrapper<IdentityResponse> restResponseWrapper,
                                           RegistrationTransaction registrationTransaction,
                                           VerifyChallengeRequest verifyChallengeRequest){
        if (restResponseWrapper.getResponse() == null){
            throw new SignUpException(restResponseWrapper.getErrors()
                    .stream()
                    .anyMatch(restError -> restError.getErrorCode().equals("IDR-IDC-007")) ?
                    ErrorConstants.IDENTIFIER_NOT_FOUND : ErrorConstants.FETCH_IDENTITY_FAILED);
        }

        if (!restResponseWrapper.getResponse().getStatus().equals(SignUpConstants.ACTIVATED)){
            throw new SignUpException(ErrorConstants.IDENTITY_INACTIVE);
        }

        Optional<ChallengeInfo> kbaChallenge = verifyChallengeRequest.getChallengeInfo().stream()
                .filter(challengeInfo -> challengeInfo.getType().equals("KBA"))
                .findFirst();
        if (kbaChallenge.isEmpty()){
            throw new SignUpException(ErrorConstants.KBA_CHALLENGE_NOT_FOUND);
        }

        List<LanguageTaggedValue> fullNameFromIdRepo = restResponseWrapper.getResponse().getIdentity()
                .getFullName().stream()
                .filter(fullName -> fullName.getLanguage().equals("khm"))
                .collect(Collectors.toList());

        String jsonObject = new String(Base64.getUrlDecoder().decode(kbaChallenge.get().getChallenge().getBytes()));
        KnowledgeBaseChallenge knowledgeBaseChallenge = null;
        try {
            knowledgeBaseChallenge = objectMapper.readValue(jsonObject, KnowledgeBaseChallenge.class);
        }catch (JsonProcessingException exception){
            throw new SignUpException(ErrorConstants.INVALID_KBA_CHALLENGE);
        }

        if (!knowledgeBaseChallenge.getFullName().equals(fullNameFromIdRepo)){
            throw new SignUpException(ErrorConstants.KNOWLEDGEBASE_MISMATCH);
        }

        //set UIN in the cache to be further used for update UIN endpoint
        registrationTransaction.setUin(cryptoHelper.symmetricEncrypt(restResponseWrapper.getResponse().getIdentity().getUIN()));

    }

    private void checkIdentityExists(RestResponseWrapper<IdentityResponse> restResponseWrapper){
        if (restResponseWrapper.getResponse() != null && restResponseWrapper.getResponse().getStatus().equals("ACTIVATED")){
            throw new SignUpException(ErrorConstants.IDENTIFIER_ALREADY_REGISTERED);
        }
    }

    private void saveIdentityData(RegisterRequest registerRequest, String transactionId,
                                  RegistrationTransaction transaction) throws SignUpException{

        UserInfoMap userInfoMap = registerRequest.getUserInfo();

        Identity identity = new Identity();
        identity.setPreferredLang(userInfoMap.getPreferredLang());
        identity.setPhone(userInfoMap.getPhone());
        identity.setFullName(userInfoMap.getFullName());
        identity.setIDSchemaVersion(idSchemaVersion);
        identity.setRegistrationType("L1");
        identity.setPhoneVerified(true);
        identity.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC));

        String uin = getUniqueIdentifier(transactionId);
        identity.setUIN(uin);

        Password password = generateSaltedHash(registerRequest.getPassword(), transactionId);
        identity.setPassword(password);

        //By default, phone is set as the selected handle.
        identity.setSelectedHandles(Arrays.asList("phone"));
        transaction.getHandlesStatus().put(getHandleRequestId(transaction.getApplicationId(),
                "phone", userInfoMap.getPhone()), RegistrationStatus.PENDING);

        IdentityRequest identityRequest = new IdentityRequest();
        identityRequest.setRegistrationId(transaction.getApplicationId());
        identityRequest.setIdentity(identity);

        addIdentity(identityRequest, transactionId);
    }

    @Timed(value = "addidentity.api.timer", percentiles = {0.95, 0.99})
    private void addIdentity(IdentityRequest identityRequest, String transactionId) throws SignUpException{

        RestRequestWrapper<IdentityRequest> restRequest = new RestRequestWrapper<>();
        restRequest.setId(addIdentityRequestID);
        restRequest.setVersion(identityRequestVersion);
        restRequest.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequest.setRequest(identityRequest);

        log.debug("Transaction {} : start add identity", transactionId);
        HttpEntity<RestRequestWrapper<IdentityRequest>> resReq = new HttpEntity<>(restRequest);
        RestResponseWrapper<IdentityResponse> restResponseWrapper = selfTokenRestTemplate.exchange(identityEndpoint,
                HttpMethod.POST,
                resReq,
                new ParameterizedTypeReference<RestResponseWrapper<IdentityResponse>>() {}).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                restResponseWrapper.getResponse().getStatus().equals("ACTIVATED")) {
            return;
        }

        log.error("Transaction {} : Add identity failed with response {}", transactionId, restResponseWrapper);
        throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.ADD_IDENTITY_FAILED);
    }

    @Timed(value = "generatehash.api.timer", percentiles = {0.95, 0.99})
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

    @Timed(value = "getuin.api.timer", percentiles = {0.95, 0.99})
    private String getUniqueIdentifier(String transactionId) throws SignUpException {

        RestResponseWrapper<UINResponse> restResponseWrapper = selfTokenRestTemplate.exchange(getUinEndpoint,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<RestResponseWrapper<UINResponse>>() {}).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                !StringUtils.isEmpty(restResponseWrapper.getResponse().getUIN()) ) {
            return restResponseWrapper.getResponse().getUIN();
        }

        log.error("Transaction {} : Get unique identifier(UIN) failed with response {}", transactionId, restResponseWrapper);
        throw new SignUpException(restResponseWrapper != null && !CollectionUtils.isEmpty(restResponseWrapper.getErrors()) ?
                restResponseWrapper.getErrors().get(0).getErrorCode() : ErrorConstants.GET_UIN_FAILED);
    }

    private void validateTransaction(RegistrationTransaction transaction, String identifier,
                                     GenerateChallengeRequest generateChallengeRequest) {
        if(transaction == null) {
            log.error("generate-challenge failed: validate transaction null");
            throw new InvalidTransactionException();
        }

        if(!transaction.isValidIdentifier(identifier)) {
            log.error("generate-challenge failed: invalid identifier");
            throw new SignUpException(ErrorConstants.IDENTIFIER_MISMATCH);
        }

        if(transaction.getChallengeRetryAttempts() > resendAttempts) {
            log.error("generate-challenge failed: too many attempts");
            throw new GenerateChallengeException(ErrorConstants.TOO_MANY_ATTEMPTS);
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

    @Timed(value = "getstatus.api.timer", percentiles = {0.95, 0.99})
    private RegistrationStatus getRegistrationStatusFromServer(String applicationId) {
        RestResponseWrapper<Map<String,String>> restResponseWrapper = selfTokenRestTemplate.exchange(getRegistrationStatusEndpoint,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<RestResponseWrapper<Map<String,String>>>() {}, applicationId).getBody();

        if (restResponseWrapper != null && restResponseWrapper.getResponse() != null &&
                !StringUtils.isEmpty(restResponseWrapper.getResponse().get("statusCode")) ) {
            switch (restResponseWrapper.getResponse().get("statusCode")) {
                case "STORED" : return RegistrationStatus.COMPLETED;
                case "FAILED" : return RegistrationStatus.FAILED;
                case "ISSUED" :
                default: return RegistrationStatus.PENDING;
            }
        }
        log.error("Transaction {} : Get registration status failed with response {}", applicationId, restResponseWrapper);
        return RegistrationStatus.PENDING;
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
        cookie.setPath("/v1/signup/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        Cookie unsetCookie = new Cookie(SignUpConstants.TRANSACTION_ID, "");
        unsetCookie.setMaxAge(0);
        response.addCookie(unsetCookie);
    }

    private String getHandleRequestId(String requestId, String handleFieldId, String handle) {
        //TODO need to take the tag from configuration based on fieldId
        String handleWithTaggedHandleType = handle.concat("@").concat(handleFieldId).toLowerCase(Locale.ROOT);
        String handleRequestId = requestId.concat(handleWithTaggedHandleType);
        try {
            return HMACUtils2.digestAsPlainText(handleRequestId.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate handleRequestId", e);
        }
        return requestId;
    }

    private void validateChallengeFormatAndType(ChallengeInfo challengeInfo) throws SignUpException{
        if (challengeInfo.getType().equals("OTP") && !challengeInfo.getFormat().equals("alpha-numeric")){
            throw new SignUpException(ErrorConstants.CHALLENGE_FORMAT_AND_TYPE_MISMATCH);
        }

        if (challengeInfo.getType().equals("KBA") && !challengeInfo.getFormat().equals("base64url-encoded-json")){
            throw new SignUpException(ErrorConstants.CHALLENGE_FORMAT_AND_TYPE_MISMATCH);
        }
    }
}
