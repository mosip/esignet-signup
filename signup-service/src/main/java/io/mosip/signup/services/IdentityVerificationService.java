package io.mosip.signup.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
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

    @Value("${mosip.signup.oauth.key-alias}")
    private String privateKeyAlias;

    @Value("${mosip.signup.oauth.keystore-password}")
    private String p12FilePassword;

    @Value("${mosip.signup.oauth.keystore-path}")
    private String p12FilePath;

    @Value("${mosip.signup.oauth.audience}")
    private String audience;

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
    	//TODO Made this fix temporarily, this piece of code needs to be taken care of later.
//        String subject = fetchAndVerifyAccessToken(request.getAuthorizationCode());
    	String subject = "individual_id";

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
            long issuedTime = System.currentTimeMillis();
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(oauthClientId)
                    .issuer(oauthClientId)
                    .audience(audience)
                    .issueTime(new Date(issuedTime))
                    .expirationTime(new Date(issuedTime+(60*1000)));
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, builder.build());
            PrivateKey privateKey = loadPrivateKey(privateKeyAlias, p12FilePassword);
            signedJWT.sign(new RSASSASigner((RSAPrivateKey) privateKey));

            // Create a Token Request with the authorization code
            AuthorizationCode code = new AuthorizationCode(authCode);
            URI callback = new URI(oauthRedirectUri);
            URI tokenEndpoint = new URI(audience);
            AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);
            ClientAuthentication clientAuthentication = new PrivateKeyJWT(signedJWT);
            TokenRequest request = new TokenRequest(tokenEndpoint,clientAuthentication, codeGrant);
            HTTPRequest toHTTPRequest = request.toHTTPRequest();
            TokenResponse tokenResponse= OIDCTokenResponseParser.parse(toHTTPRequest.send());
            if (tokenResponse.indicatesSuccess()) {
                OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
                AccessToken accessToken = successResponse.getOIDCTokens().getAccessToken();
                log.info("Access token received successfully");
                return accessToken.toJSONString();
            }
            log.error("Failed to exchange authorization grant for tokens: "+tokenResponse.toErrorResponse());
        }catch (Exception e) {
            log.error("Failed to exchange authorization grant for tokens", e);
        }
        throw new SignUpException(ErrorConstants.TOKEN_EXCHANGE_FAILED);
    }

    private IdentityVerifierDetail[] getIdentityVerifierDetailsFromConfigServer() {
        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        if(verifierDetails == null || verifierDetails.length == 0) {
            verifierDetails = restTemplate.getForObject(configServerUrl+ALL_IDV_DETAILS_JSON_FILE_NAME, IdentityVerifierDetail[].class);
            return cacheUtilService.setIdentityVerifierDetails(CacheUtilService.COMMON_KEY, verifierDetails);
        }
        return verifierDetails;
    }

    private PrivateKey loadPrivateKey(String alias, String cryptoPassword) {
        try {
            Path path = Paths.get(p12FilePath);
            try (InputStream inputStream = Files.newInputStream(path)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(inputStream, cryptoPassword.toCharArray());
                // Retrieve the private key
                return (PrivateKey) keyStore.getKey(alias, cryptoPassword.toCharArray());
            }
        } catch (Exception e) {
            log.error("Failed to load private key from keystore", e);
            throw new SignUpException(ErrorConstants.PRIVATE_KEY_LOAD_FAILED);
        }
    }
}
