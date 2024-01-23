package io.mosip.signup.services;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
public class CacheUtilService {
    @Autowired
    CacheManager cacheManager;

    //---Setter---
    @Cacheable(value = SignUpConstants.CHALLENGE_GENERATED, key = "#transactionId")
    public RegistrationTransaction setChallengeGeneratedTransaction(String transactionId,
                                                                    RegistrationTransaction registrationTransaction) {
        return registrationTransaction;
    }

    @CacheEvict(value = SignUpConstants.CHALLENGE_GENERATED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.CHALLENGE_VERIFIED, key = "#transactionId")
    public RegistrationTransaction setChallengeVerifiedTransaction(String transactionId,
                                                                   RegistrationTransaction registrationTransaction) {
        return registrationTransaction;
    }

    @CacheEvict(value = SignUpConstants.CHALLENGE_VERIFIED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.REGISTERED_CACHE, key = "#transactionId")
    public RegistrationTransaction setRegisteredTransaction(String transactionId,
                                                 RegistrationTransaction registrationTransaction) {
        return registrationTransaction;
    }

    @Cacheable(value = SignUpConstants.BLOCKED_IDENTIFIER, key = "#identifierHash")
    public String blockIdentifier(String identifierHash) {
        return identifierHash;
    }

    @Cacheable(value = SignUpConstants.KEYSTORE, key = "#key")
    public String setSecretKey(String key, String secretKey) {
        return secretKey;
    }

    @Cacheable(value = SignUpConstants.KEY_ALIAS, key = "#alias")
    public String setSecretKeyBasedOnAlias(String alias, String secretKey) {
        return secretKey;
    }

    //---Getter---
    public RegistrationTransaction getChallengeGeneratedTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.CHALLENGE_GENERATED).get(transactionId, RegistrationTransaction.class);
    }

    public RegistrationTransaction getChallengeVerifiedTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.CHALLENGE_VERIFIED).get(transactionId, RegistrationTransaction.class);
    }

    public RegistrationTransaction getRegisteredTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.REGISTERED_CACHE).get(transactionId, RegistrationTransaction.class);
    }

    public boolean isIdentifierBlocked(String identifier) {
        String identifierHash = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                identifier.toLowerCase(Locale.ROOT));
        String value = cacheManager.getCache(SignUpConstants.BLOCKED_IDENTIFIER).get(identifierHash, String.class);
        return value == null ? false : true;
    }

    public String getSecretKey() {
        return cacheManager.getCache(SignUpConstants.KEYSTORE).get(CryptoHelper.CACHE_KEY, String.class);
    }

    public String getSecretKey(String alias) {
        return cacheManager.getCache(SignUpConstants.KEY_ALIAS).get(alias, String.class);
    }
}
