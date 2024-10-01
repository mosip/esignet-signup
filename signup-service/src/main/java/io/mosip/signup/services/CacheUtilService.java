/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.Longs;
import io.mosip.esignet.core.constants.Constants;
import io.mosip.esignet.core.dto.OIDCTransaction;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Locale;

import static io.mosip.signup.util.SignUpConstants.*;

@Slf4j
@Service
public class CacheUtilService {

    public static final String COMMON_KEY = "KEY";

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${mosip.signup.slot.expire-in-seconds}")
    private Integer slotExpireInSeconds;

    private static final String CLEANUP_SCRIPT = "local hash_name = ARGV[1]\n" +
            "local max_age = ARGV[2]\n" +
            "\n" +
            "for field, value in pairs(redis.call('hgetall', hash_name)) do\n" +
            "\tlocal epoch = tonumber(value)\n" +
            "\tif epoch and os.time() - epoch > max_age then\n" +
            "\t\tredis.call('hdel', hash_name, field)\n" +
            "\tend\n" +
            "end";
    private String scriptHash = null;


    private static final String ADD_SLOT_SCRIPT = "local count = redis.call('HLEN', KEYS[1]); " +
            "if count < tonumber(ARGV[1]) then" +
            " redis.call('HSET', KEYS[1], ARGV[2], ARGV[3]);" +
            " return count;" +
            "else return -1; " +
            "end";
    private String addSlotScriptHash = null;



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

    public RegistrationTransaction createUpdateChallengeGeneratedTransaction(String transactionId,
                                                                             RegistrationTransaction registrationTransaction) {
        cacheManager.getCache(SignUpConstants.CHALLENGE_GENERATED).put(transactionId, registrationTransaction); //NOSONAR getCache() will not be returning null here.
        return registrationTransaction;
    }

    public void updateStatusCheckTransaction(String transactionId,
                                             RegistrationTransaction registrationTransaction) {
        cacheManager.getCache(SignUpConstants.STATUS_CHECK).put(transactionId, registrationTransaction);    //NOSONAR getCache() will not be returning null here.
    }

    //Identity verification process related caches

    @Cacheable(value = SignUpConstants.IDENTITY_VERIFIERS, key = "#key")
    public IdentityVerifierDetail[] setIdentityVerifierDetails(String key, IdentityVerifierDetail[] identityVerifierDetails) {
        return identityVerifierDetails;
    }

    @Cacheable(value = SignUpConstants.IDENTITY_VERIFIER_METADATA, key = "#identityVerifierId")
    public JsonNode setIdentityVerifierMetadata(String identityVerifierId, JsonNode jsonNode) {
        return jsonNode;
    }

    @Cacheable(value = SignUpConstants.IDENTITY_VERIFICATION, key = "#transactionId")
    public IdentityVerificationTransaction setIdentityVerificationTransaction(String transactionId,
                                                                       IdentityVerificationTransaction identityVerificationTransaction) {
        return identityVerificationTransaction;
    }

    @CacheEvict(value = SignUpConstants.IDENTITY_VERIFICATION, key = "#transactionId")
    @Cacheable(value = SignUpConstants.SLOT_ALLOTTED, key = "#transactionId")
    public IdentityVerificationTransaction setSlotAllottedTransaction(String transactionId,
                                                                      IdentityVerificationTransaction identityVerificationTransaction) {
        return identityVerificationTransaction;
    }

    @CacheEvict(value = SignUpConstants.SLOT_ALLOTTED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.VERIFIED_SLOT, key = "#slotId")
    public IdentityVerificationTransaction setVerifiedSlotTransaction(String transactionId, String slotId,
                                                                      IdentityVerificationTransaction identityVerificationTransaction) {
        return identityVerificationTransaction;
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

    public IdentityVerificationTransaction getSlotAllottedTransaction(String transactionId) {
        return cacheManager.getCache(SignUpConstants.SLOT_ALLOTTED).get(transactionId, IdentityVerificationTransaction.class); //NOSONAR getCache() will not be returning null here.
    }

    public IdentityVerificationTransaction getVerifiedSlotTransaction(String slotId) {
        return cacheManager.getCache(SignUpConstants.VERIFIED_SLOT).get(slotId, IdentityVerificationTransaction.class); //NOSONAR getCache() will not be returning null here.
    }

    public JsonNode getIdentityVerifierMetadata(String identityVerifierId) {
        return cacheManager.getCache(SignUpConstants.IDENTITY_VERIFIER_METADATA).get(identityVerifierId, JsonNode.class); //NOSONAR getCache() will not be returning null here.
    }

    public void updateVerifiedSlotTransaction(String slotId, IdentityVerificationTransaction transaction) {
        if(cacheManager.getCache(SignUpConstants.VERIFIED_SLOT) != null) {
            cacheManager.getCache(SignUpConstants.VERIFIED_SLOT).put(slotId, transaction);
        }
    }

    public void updateVerificationStatus(String haltedTransactionId, String status, String errorCode) {
        if(cacheManager.getCache(Constants.HALTED_CACHE) != null) {
            OIDCTransaction oidcTransaction = cacheManager.getCache(Constants.HALTED_CACHE).get(haltedTransactionId, OIDCTransaction.class);
            if(oidcTransaction != null) {
                oidcTransaction.setVerificationStatus(status);
                oidcTransaction.setVerificationErrorCode(errorCode);
                cacheManager.getCache(Constants.HALTED_CACHE).put(haltedTransactionId, oidcTransaction);
            }
        }
    }

    public void addToSlotConnected(String value) {
        redisConnectionFactory.getConnection().hSet(SLOTS_CONNECTED.getBytes(), value.getBytes(),
                Longs.toByteArray(System.currentTimeMillis()));
    }

    public void removeFromSlotConnected(String value) {
        redisConnectionFactory.getConnection().hDel(SLOTS_CONNECTED.getBytes(), value.getBytes());
    }

    public long getCurrentSlotCount() {
        Long count = redisConnectionFactory.getConnection().hLen(SLOTS_CONNECTED.getBytes());
        log.info("Current allotted slot count : {}", count);
        return count == null ? 0 : count;
    }

    //Cleanup assigned slot details on WS connection disconnect
    @Caching(evict = {
            @CacheEvict(value = SignUpConstants.SLOT_ALLOTTED, key = "#transactionId")
    })
    public void evictSlotAllottedTransaction(String transactionId, String slotId) {
    }

    /**
     * SchedulerLock - When dealing with multiple instances, tasks will run more than once. We are using
     * a scheduler lock, ensuring that scheduled task runs only once.
     */
    @Scheduled(cron = "${mosip.signup.slot.cleanup-cron}")
    @SchedulerLock(name = "clearExpiredSlots", lockAtMostFor = "PT120S", lockAtLeastFor = "PT120S")
    public void clearExpiredSlots() {
        log.info("Scheduled Task - clearExpiredSlots triggered");
        if(redisConnectionFactory.getConnection() != null) {
            if(scriptHash == null) {
                scriptHash = redisConnectionFactory.getConnection().scriptingCommands().scriptLoad(CLEANUP_SCRIPT.getBytes());
            }
            LockAssert.assertLocked();
            log.info("Running scheduled cleanup task - task to clear expired slots with script hash: {} {} {}", scriptHash,
                    SLOTS_CONNECTED, slotExpireInSeconds);
            redisConnectionFactory.getConnection().scriptingCommands().evalSha(scriptHash, ReturnType.INTEGER, 1,
                    SLOTS_CONNECTED.getBytes(), new byte[]{slotExpireInSeconds.byteValue()});
        }
    }

    public Long getSetSlotCount(String field, Integer maxCount) {
        if(redisConnectionFactory.getConnection() != null) {
            if(addSlotScriptHash == null) {
                addSlotScriptHash = redisConnectionFactory.getConnection().scriptingCommands().scriptLoad(ADD_SLOT_SCRIPT.getBytes());
            }
            log.info("Running ADD_SLOT_SCRIPT script: {} {} {}", addSlotScriptHash, SLOTS_CONNECTED, maxCount);
            return redisConnectionFactory.getConnection().scriptingCommands().evalSha(addSlotScriptHash, ReturnType.INTEGER, 1,
                    SLOTS_CONNECTED.getBytes(), new byte[]{maxCount.byteValue()}, field.getBytes(), Longs.toByteArray(System.currentTimeMillis()));
        }
        return -1L;
    }
}
