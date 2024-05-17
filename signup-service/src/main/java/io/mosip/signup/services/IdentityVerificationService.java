package io.mosip.signup.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;


@Slf4j
@Service
public class IdentityVerificationService {

    private static final String ALL_IDV_DETAILS_JSON_FILE_NAME = "signup-identity-verifier-details.json";
    private static final String IDV_DETAILS_JSON_FILE_NAME = "signup-idv_%s.json";

    @Value("${mosip.signup.config-server-url}")
    private String configServerUrl;

    @Value("${mosip.signup.identity-verification.txn.timeout}")
    private int identityVerificationTransactionTimeout;

    @Value("${mosip.signup.oauth.client-id}")
    private String oauthClientId;

    @Value("${mosip.signup.oauth.redirect-uri}")
    private String oauthRedirectUri;

    @Value("${mosip.signup.oauth.issuer-uri}")
    private String oauthIssuerUri;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheUtilService cacheUtilService;

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public InitiateIdentityVerificationResponse initiateIdentityVerification(InitiateIdentityVerificationRequest request,
                                                                             HttpServletResponse response) {
        //fetch access token from esignet with auth-code in the request
        String subject = fetchAndVerifyAccessToken(request.getAuthorizationCode());

        //if successful, start the transaction
        String transactionId = IdentityProviderUtil.createTransactionId(null);
        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setIndividualId(subject);
        transaction.setSlotId(IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                transactionId));
        cacheUtilService.setIdentityVerificationTransaction(transactionId, transaction);

        Cookie cookie = new Cookie(SignUpConstants.IDV_TRANSACTION_ID, transactionId);
        cookie.setMaxAge(identityVerificationTransactionTimeout);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        InitiateIdentityVerificationResponse dto = new InitiateIdentityVerificationResponse();
        dto.setIdentityVerifiers(getIdentityVerifierDetailsFromConfigServer());
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

        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        Optional<IdentityVerifierDetail> result = Arrays.stream(verifierDetails)
                .filter( idv -> idv.isActive() && idv.getId().equals(identityVerifierId))
                .findFirst();

        if(result.isPresent()) {
            String fileName = String.format(IDV_DETAILS_JSON_FILE_NAME, identityVerifierId);
            return restTemplate.getForObject(configServerUrl+fileName, JsonNode.class);
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
    public SlotResponse getSlot(String transactionId, SlotRequest slotRequest) {
        IdentityVerificationTransaction transaction = cacheUtilService.getIdentityVerificationTransaction(transactionId);
        if(transaction == null)
            throw new InvalidTransactionException();

        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        Optional<IdentityVerifierDetail> result = Arrays.stream(verifierDetails)
                .filter( idv -> idv.isActive() && idv.getId().equals(slotRequest.getVerifierId()))
                .findFirst();

        if(!result.isPresent())
            throw new SignUpException(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID);

        //save verifierId in  cache
        //Check for current queue size
        //return slot if queue size is less than the upper limit
        //If not available throw exception

        log.info("Slot available and assigned to the requested transaction {}", transactionId);
        SlotResponse slotResponse = new SlotResponse();
        slotResponse.setSlotId(transaction.getSlotId());
        return slotResponse;
    }

    private String fetchAndVerifyAccessToken(String authCode) {
        try {
            AuthorizationCode code = new AuthorizationCode(authCode);
            URI callback = new URI(oauthRedirectUri);
            AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

            long issuedTime = System.currentTimeMillis();
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(oauthClientId)
                    .issuer(oauthClientId)
                    .audience(oauthIssuerUri)
                    .issueTime(new Date(issuedTime))
                    .expirationTime(new Date(issuedTime+(60*1000)));
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, builder.build());
            //signedJWT.sign(new RSASSASigner()); TODO Read private key from keystore
            //ClientAuthentication clientAuthentication = new PrivateKeyJWT(signedJWT);
        } catch (URISyntaxException e) {
            log.error("Failed to exchange authorization grant for tokens", e);
            throw new SignUpException("token_exchange_failed");
        }
        log.info("Successfully fetched access-token and extracted subject from the access-token");
        return "subject";
    }

    private IdentityVerifierDetail[] getIdentityVerifierDetailsFromConfigServer() {
        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        if(verifierDetails == null || verifierDetails.length == 0) {
            verifierDetails = restTemplate.getForObject(configServerUrl+ALL_IDV_DETAILS_JSON_FILE_NAME, IdentityVerifierDetail[].class);
            return cacheUtilService.setIdentityVerifierDetails(CacheUtilService.COMMON_KEY, verifierDetails);
        }
        return verifierDetails;
    }
}
