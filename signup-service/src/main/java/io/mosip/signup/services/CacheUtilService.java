package io.mosip.signup.services;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Slf4j
@Service
public class CacheUtilService {

    public static final String COMMON_KEY = "KEY";

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public long countEntriesInSlotAllotted(){
        String pattern = SignUpConstants.SLOT_ALLOTTED + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(connection ->
                connection.scan(options))) {
            long count = 0;
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
            return count;
        } catch (Exception e) {
            log.error("Error while counting entries of allotted slot", e);
            throw new SignUpException(ErrorConstants.SLOT_NOT_AVAILABLE);
        }
    }

    //---Setter---

    @CacheEvict(value = SignUpConstants.CHALLENGE_GENERATED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.CHALLENGE_VERIFIED, key = "#verifiedTransactionId")
    public RegistrationTransaction setChallengeVerifiedTransaction(String transactionId, String verifiedTransactionId,
                                                                   RegistrationTransaction registrationTransaction) {
        return registrationTransaction;
    }

    @CacheEvict(value = SignUpConstants.CHALLENGE_VERIFIED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.STATUS_CHECK, key = "#transactionId")
    public RegistrationTransaction setStatusCheckTransaction(String transactionId,
                                                             RegistrationTransaction registrationTransaction) {
        return registrationTransaction;
    }

    @CacheEvict(value = SignUpConstants.CHALLENGE_GENERATED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.BLOCKED_IDENTIFIER, key = "#key")
    public String blockIdentifier(String transactionId, String key, String value) {
        return value;
    }

    @Cacheable(value = SignUpConstants.KEYSTORE, key = "#key")
    public String setSecretKey(String key, String secretKey) {
        return secretKey;
    }

    @CachePut(value = SignUpConstants.KEY_ALIAS, key = "#key")
    public String setActiveKeyAlias(String key, String alias) {
        return alias;
    }

    @Cacheable(value = SignUpConstants.IDENTITY_VERIFIERS, key = "#key")
    public IdentityVerifierDetail[] setIdentityVerifierDetails(String key, IdentityVerifierDetail[] identityVerifierDetails) {
        return identityVerifierDetails;
    }

    @Cacheable(value = SignUpConstants.IDENTITY_VERIFICATION, key = "#transactionId")
    public IdentityVerificationTransaction setIdentityVerificationTransaction(String transactionId,
                                                                       IdentityVerificationTransaction identityVerificationTransaction) {
        return identityVerificationTransaction;
    }

    //----- cache update is separated
    //----- we are not using @cacheput as @cacheput extends the TTL on cache entry

    public RegistrationTransaction createUpdateChallengeGeneratedTransaction(String transactionId,
                                                                       RegistrationTransaction registrationTransaction) {
        cacheManager.getCache(SignUpConstants.CHALLENGE_GENERATED).put(transactionId, registrationTransaction); //NOSONAR getCache() will not be returning null here.
        return registrationTransaction;
    }

    public void updateStatusCheckTransaction(String transactionId,
                                                    RegistrationTransaction registrationTransaction) {
        cacheManager.getCache(SignUpConstants.STATUS_CHECK).put(transactionId, registrationTransaction);    //NOSONAR getCache() will not be returning null here.
    }

    //---Getter---
    public RegistrationTransaction getChallengeGeneratedTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.CHALLENGE_GENERATED).get(transactionId, RegistrationTransaction.class);    //NOSONAR getCache() will not be returning null here.
    }

    public RegistrationTransaction getChallengeVerifiedTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.CHALLENGE_VERIFIED).get(transactionId, RegistrationTransaction.class); //NOSONAR getCache() will not be returning null here.
    }

    public RegistrationTransaction getStatusCheckTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.STATUS_CHECK).get(transactionId, RegistrationTransaction.class);	//NOSONAR getCache() will not be returning null here.
    }

    public boolean isIdentifierBlocked(String identifier) {
        String identifierHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                identifier.toLowerCase(Locale.ROOT));
        return cacheManager.getCache(SignUpConstants.BLOCKED_IDENTIFIER).get(identifierHash, String.class) != null;	//NOSONAR getCache() will not be returning null here.
    }

    public String getSecretKey(String keyAlias) {
        return cacheManager.getCache(SignUpConstants.KEYSTORE).get(keyAlias, String.class);	//NOSONAR getCache() will not be returning null here.
    }

    public String getActiveKeyAlias() {
        return cacheManager.getCache(SignUpConstants.KEY_ALIAS).get(CryptoHelper.ALIAS_CACHE_KEY, String.class);	//NOSONAR getCache() will not be returning null here.
    }

    public IdentityVerifierDetail[] getIdentityVerifierDetails() {
        return cacheManager.getCache(SignUpConstants.IDENTITY_VERIFIERS).get(COMMON_KEY, IdentityVerifierDetail[].class);	//NOSONAR getCache() will not be returning null here.
    }

    public IdentityVerificationTransaction getIdentityVerificationTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.IDENTITY_VERIFICATION).get(transactionId, IdentityVerificationTransaction.class); //NOSONAR getCache() will not be returning null here.
    }

    @Cacheable(value = SignUpConstants.SLOT_ALLOTTED, key = "#slotId")
    @CacheEvict(value = SignUpConstants.IDENTITY_VERIFICATION, key = "#transactionId")
    public IdentityVerificationTransaction setAllottedIdentityVerificationTransaction(String transactionId, String slotId,
                                                                              IdentityVerificationTransaction identityVerificationTransaction) {
        return identityVerificationTransaction;
    }

    @CacheEvict(value = SignUpConstants.SLOT_ALLOTTED, key = "#transactionId")
    public void removeAllottedIdentityVerificationTransaction(String transactionId) {
    }
}
