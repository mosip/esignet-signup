package io.mosip.signup.services;

import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
}
