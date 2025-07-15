/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.mosip.signup.api.util.VerificationStatus.*;
import static io.mosip.signup.util.SignUpConstants.VALUE_SEPARATOR;
import java.security.Key;

@Slf4j
@Service
public class IdentityVerificationService {

    private static final String ALL_IDV_DETAILS_JSON_FILE_NAME = "signup-identity-verifier-details.json";
    private static final String IDV_DETAILS_JSON_FILE_NAME = "signup-idv_%s.json";

    @Value("${mosip.signup.config-server-url}")
    private String configServerUrl;

    @Value("${mosip.signup.identity-verification.txn.timeout}")
    private int identityVerificationTransactionTimeout;

    @Value("${mosip.signup.slot-allotted.timeout:1000}")
    private int slotAllottedTimeout;

    @Value("${mosip.signup.verified-slot.timeout:1000}")
    private int verifiedSlotTimeout;

    @Value("${mosip.signup.oauth.client-id}")
    private String oauthClientId;

    @Value("${mosip.signup.oauth.redirect-uri}")
    private String oauthRedirectUri;

    @Value("${mosip.signup.oauth.issuer-uri}")
    private String oauthIssuerUri;

    @Value("${mosip.signup.oauth.userinfo-uri}")
    private String oauthUserinfoUri;

    @Value("${mosip.signup.oauth.token-uri}")
    private String oauthTokenUri;

    @Value("${mosip.signup.oauth.key-alias}")
    private String privateKeyAlias;

    @Value("${mosip.signup.oauth.keystore-password}")
    private String p12FilePassword;

    @Value("${mosip.signup.oauth.keystore-path}")
    private String p12FilePath;

    @Value("${mosip.signup.oauth.audience}")
    private String audience;

    @Value("${mosip.signup.slot.max-count:50}")
    private int slotMaxCount;

    @Value("${mosip.signup.slot.expire-in-seconds}")
    private Integer slotExpireInSeconds;

    @Autowired
    private CacheUtilService cacheUtilService;

    @Autowired
    private ProfileRegistryPlugin profileRegistryPlugin;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Fetches the access token using the authorization grant and gets userinfo from eSignet.
     * If valid access token, starts the Identity verification transaction.
     * Sets cookie with newly generated transaction ID.
     * @param request
     * @param response
     * @return
     */
    public InitiateIdentityVerificationResponse initiateIdentityVerification(InitiateIdentityVerificationRequest request,
                                                                             HttpServletResponse response) {
        //fetch access token from esignet with auth-code in the request
        //access token subject is the Halted transactionId
        //userinfo subject holds the actual user's individualId
        AccessToken accessToken = fetchAndVerifyAccessToken(request.getAuthorizationCode());
        String subject = getUsername(accessToken);

        //if successful, start the transaction
        String transactionId = IdentityProviderUtil.createTransactionId(null);
        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setAccessToken(accessToken.toJSONString());
        transaction.setAccessTokenSubject(extractSubject(accessToken));
        transaction.setIndividualId(subject); //TODO encrypt
        transaction.setSlotId(IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                transactionId));
        transaction.setApplicationId(UUID.randomUUID().toString());
        cacheUtilService.setIdentityVerificationTransaction(transactionId, transaction);

        Cookie cookie = new Cookie(SignUpConstants.IDV_TRANSACTION_ID, transactionId);
        cookie.setMaxAge(identityVerificationTransactionTimeout);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        InitiateIdentityVerificationResponse dto = new InitiateIdentityVerificationResponse();
        dto.setIdentityVerifiers(getIdentityVerifierDetails());
        return dto;
    }

    /**
     *
     * @param transactionId
     * @param identityVerifierId
     * @return
     */
    public JsonNode getIdentityVerifierDetails(String transactionId, String identityVerifierId) {
        IdentityVerificationTransaction transaction = cacheUtilService.getIdentityVerificationTransaction(transactionId);
        if(transaction == null)
            throw new InvalidTransactionException();

        IdentityVerifierDetail[] verifierDetails = getIdentityVerifierDetails();
        Optional<IdentityVerifierDetail> result = Arrays.stream(verifierDetails)
                .filter( idv -> idv.isActive() && idv.getId().equals(identityVerifierId))
                .findFirst();

        if(result.isPresent()) {
           return getIdentityVerifierMetadata(identityVerifierId);
        }
        log.error("Invalid identity verifier ID provided!");
        throw new SignUpException(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID);
    }


    /**
     *
     * @param transactionId
     * @param slotRequest
     * @return
     */
    public SlotResponse  getSlot(String transactionId, SlotRequest slotRequest, HttpServletResponse response) {
       IdentityVerificationTransaction transaction = cacheUtilService.getIdentityVerificationTransaction(transactionId);
        if (transaction == null)
            throw new InvalidTransactionException();

        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        Optional<IdentityVerifierDetail> result = Arrays.stream(verifierDetails)
                .filter(idv -> idv.isActive() && idv.getId().equals(slotRequest.getVerifierId()))
                .findFirst();

        if (result.isEmpty())
            throw new SignUpException(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID);

        try{
            String cookieValue = transactionId.concat(VALUE_SEPARATOR).concat(transaction.getSlotId());
            long slotCount = cacheUtilService.getSetSlotCount(cookieValue, getVerificationProcessExpireTimeInMillis(result.get()), slotMaxCount);
            if(slotCount < 0) {
                log.error("**** Maximum slot capacity reached! slotCount result: {} ****", slotCount);
                throw new SignUpException(ErrorConstants.SLOT_NOT_AVAILABLE);
            }

            transaction.setVerifierId(slotRequest.getVerifierId());
            transaction.setDisabilityType(slotRequest.getDisabilityType());
            transaction.setStatus(STARTED);
            transaction = cacheUtilService.setSlotAllottedTransaction(transactionId, transaction);

            addSlotAllottedCookie(cookieValue, result.get(), response);

            log.info("Slot({}) available and assigned to the requested transaction {}", slotCount, transactionId);
            SlotResponse slotResponse = new SlotResponse();
            slotResponse.setSlotId(transaction.getSlotId());
            return slotResponse;

        }catch (SignUpException ex){
            log.error("Failed to assign slot to the requested transaction {}", transactionId, ex);
        }
        throw new SignUpException(ErrorConstants.SLOT_NOT_AVAILABLE);
    }

    /**
     * Get the status of identity verification process with a valid allotted slotId
     * @param transactionId
     * @return
     */
    public IdentityVerificationStatusResponse getStatus(String transactionId) {
        try {
        if(transactionId.split(VALUE_SEPARATOR).length <= 1)
            throw new InvalidTransactionException();

        final String slotId = transactionId.split(VALUE_SEPARATOR)[1];
        IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(slotId);
        if(transaction == null)
            throw new InvalidTransactionException();

        log.info("Transaction retrieved: {}", transaction);

        ProfileCreateUpdateStatus registrationStatus;
        if(transaction.getStatus() == null) {
            registrationStatus = profileRegistryPlugin.getProfileCreateUpdateStatus(transaction.getApplicationId());
            transaction.setStatus(ProfileCreateUpdateStatus.getVerificationStatus(registrationStatus));
            log.debug("Updated transaction status from registry: {}", transaction.getStatus());
        }

        IdentityVerificationStatusResponse identityVerificationStatusResponse = new IdentityVerificationStatusResponse();
        log.info("Processing transaction with status: {}", transaction.getStatus());
        switch (transaction.getStatus()) {
            case COMPLETED:
            case FAILED:
                identityVerificationStatusResponse.setStatus(transaction.getStatus());
                break;
            case UPDATE_PENDING:
                registrationStatus = profileRegistryPlugin.getProfileCreateUpdateStatus(transaction.getApplicationId());
                transaction.setStatus(ProfileCreateUpdateStatus.getVerificationStatus(registrationStatus));
                cacheUtilService.updateVerifiedSlotTransaction(slotId, transaction);
                break;
        }

        cacheUtilService.updateVerificationStatus(transaction.getAccessTokenSubject(), transaction.getStatus().toString(),
                transaction.getErrorCode());
        log.info("Setting final response status to: {}", transaction.getStatus());
        identityVerificationStatusResponse.setStatus(transaction.getStatus());
        return identityVerificationStatusResponse;
        } catch (Exception e) {
            log.error("Error while processing transactionId {}: {}", transactionId, e.getMessage(), e);
            throw new SignUpException(ErrorConstants.INVALID_TRANSACTION);
        }
    }

    private void addSlotAllottedCookie(String value, IdentityVerifierDetail identityVerifierDetail,
                                       HttpServletResponse response) {
        Cookie unsetCookie = new Cookie(SignUpConstants.IDV_TRANSACTION_ID, "");
        unsetCookie.setMaxAge(0);
        unsetCookie.setHttpOnly(true);
        unsetCookie.setSecure(true);
        unsetCookie.setPath("/");
        response.addCookie(unsetCookie);

        Cookie cookie = new Cookie(SignUpConstants.IDV_SLOT_ALLOTTED, value);
        int maxAge = identityVerifierDetail.getProcessDuration() > 0 ? identityVerifierDetail.getProcessDuration() : verifiedSlotTimeout;
        cookie.setMaxAge(slotAllottedTimeout+maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getUsername(AccessToken accessToken) {
        try {
            UserInfoRequest userInfoRequest = new UserInfoRequest(new URI(oauthUserinfoUri), accessToken);
            UserInfoResponse userInfoResponse = UserInfoResponse.parse(userInfoRequest.toHTTPRequest().send());
            log.info("userinfo call completed with response : {}", userInfoResponse.toHTTPResponse().getStatusCode());
            if(userInfoResponse.indicatesSuccess()) {
                JWT jwt =  userInfoResponse.toSuccessResponse().toHTTPResponse().getBodyAsJWT();
                log.debug("userinfo as JWT : {}", jwt);
                return jwt.getJWTClaimsSet().getSubject();
            }
        } catch (Exception e) {
            log.error("Failed to fetch userinfo", e);
        }
        throw new SignUpException(ErrorConstants.USERINFO_FAILED);
    }


    private AccessToken fetchAndVerifyAccessToken(String authCode) {
        try {
            long issuedTime = System.currentTimeMillis();
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(oauthClientId)
                    .issuer(oauthClientId)
                    .audience(audience)
                    .jwtID(IdentityProviderUtil.createTransactionId(null))
                    .issueTime(new Date(issuedTime))
                    .expirationTime(new Date(issuedTime+(60*1000)));
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, builder.build());
            PrivateKey privateKey = loadPrivateKey(privateKeyAlias, p12FilePassword);
            signedJWT.sign(new RSASSASigner((RSAPrivateKey) privateKey));

            // Create a Token Request with the authorization code
            AuthorizationCode code = new AuthorizationCode(authCode);
            AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, new URI(oauthRedirectUri));
            ClientAuthentication clientAuthentication = new PrivateKeyJWT(signedJWT);
            TokenRequest request = new TokenRequest(new URI(oauthTokenUri), clientAuthentication, codeGrant);
            TokenResponse tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());
            if (tokenResponse.indicatesSuccess()) {
                log.info("Access token received successfully");
                AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
                return accessTokenResponse.getTokens().getAccessToken();
            }
            log.error("Failed to exchange authorization grant for tokens: {} ", tokenResponse.toErrorResponse());
        } catch (Exception e) {
            log.error("Failed to exchange authorization grant for tokens", e);
        }
        throw new SignUpException(ErrorConstants.GRANT_EXCHANGE_FAILED);
    }

    private IdentityVerifierDetail[] getIdentityVerifierDetails() {
        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        if(verifierDetails == null || verifierDetails.length == 0) {
            verifierDetails = getResource(configServerUrl+ALL_IDV_DETAILS_JSON_FILE_NAME, IdentityVerifierDetail[].class);
            return cacheUtilService.setIdentityVerifierDetails(CacheUtilService.COMMON_KEY, verifierDetails);
        }
        return verifierDetails;
    }

    private JsonNode getIdentityVerifierMetadata(String identityVerifierId) {
        JsonNode jsonNode = cacheUtilService.getIdentityVerifierMetadata(identityVerifierId);
        if(jsonNode == null) {
            String fileName = String.format(IDV_DETAILS_JSON_FILE_NAME, identityVerifierId);
            jsonNode = getResource(configServerUrl+fileName, JsonNode.class);
            return cacheUtilService.setIdentityVerifierMetadata(identityVerifierId, jsonNode);
        }
        return jsonNode;
    }

    private PrivateKey loadPrivateKey(String alias, String password) {
        try {
            Path path = Paths.get(p12FilePath);
            try (InputStream inputStream = Files.newInputStream(path)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(inputStream, password.toCharArray());
                // Retrieve the private key
                Key key = keyStore.getKey(alias, password.toCharArray());
                if(key!=null) {
                    return (PrivateKey)key;
                }
            }
        } catch (Exception e) {
            log.error("Failed to load private key from keystore", e);
            throw new SignUpException(ErrorConstants.PRIVATE_KEY_LOAD_FAILED);
        }
        log.error("No private key found in keystore for alias '{}'", alias);
        return null;
    }

    private <T> T getResource(String url, Class<T> clazz) {
        Resource resource = resourceLoader.getResource(url);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            return objectMapper.readValue(content, clazz);
        } catch (IOException e) {
            log.error("Failed to parse data: {}", url, e);
        }
        throw new IdentityVerifierException("invalid_configuration");
    }

    private String extractSubject(AccessToken accessToken) {
        String tokenString = accessToken.getValue();
        try {
            JWT jwt = JWTParser.parse(tokenString);
            if (jwt instanceof SignedJWT) {
                SignedJWT signedJWT = (SignedJWT) jwt;
                return signedJWT.getJWTClaimsSet().getSubject();
            }
        } catch (ParseException e) {
            log.error("Failed to parse access token: {}", tokenString, e);
        }
        return null;
    }

    private long getVerificationProcessExpireTimeInMillis(IdentityVerifierDetail identityVerifierDetail) {
        int processDurationInSeconds = identityVerifierDetail.getProcessDuration() <= 0 ? slotExpireInSeconds : identityVerifierDetail.getProcessDuration();
        return System.currentTimeMillis() + ( processDurationInSeconds * 1000L );
    }
}
