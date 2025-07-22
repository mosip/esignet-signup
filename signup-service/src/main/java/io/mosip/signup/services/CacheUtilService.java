/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.esignet.core.constants.Constants;
import io.mosip.esignet.core.dto.OIDCTransaction;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.ErrorConstants;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.mosip.signup.util.SignUpConstants.*;

@Slf4j
@Service
public class CacheUtilService {

    public static final String COMMON_KEY = "KEY";

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mosip.signup.iam.token.endpoint}")
    private String authTokenEndpoint;

    @Value("${mosip.signup.iam.client-id}")
    private String clientId;

    @Value("${mosip.signup.iam.client-secret}")
    private String clientSecret;

    private static final String CLEANUP_SCRIPT = "local function binary_to_long(binary_str)\n" +
            "    local result = 0\n" +
            "    for i = 1, #binary_str do\n" +
            "        result = result * 256 + binary_str:byte(i)\n" +
            "    end\n" +
            "    return result\n" +
            "end" +
            "\n" +
            "local hash_name = KEYS[1]\n" +
            "local time = redis.call(\"TIME\")\n" +
            "local current_time = ( tonumber(time[1]) * 1000) + math.floor( tonumber(time[2]) / 1000)\n" +
            "local verified_slot_cache_keys = {}\n" +
            "local fields_to_delete = {}\n" +
            "local delcount=0\n" +
            "local cursor = \"0\"\n" +
            "repeat\n" +
            "    local result = redis.call('hscan', hash_name, cursor)\n" +
            "    cursor = result[1]\n" +
            "    local hash_data = result[2]\n" +
            "    for i = 1, #hash_data, 2 do\n" +
            "        local field = hash_data[i]\n" +
            "        local value = tonumber(hash_data[i + 1])\n" +
            "        if value < current_time then\n" +
            "            local separator_index = string.find(field, \"###\")\n" +
            "            if separator_index then               \n" +
            "                local key_part = string.sub(field, 1, separator_index - 1)\n" +
            "                table.insert(verified_slot_cache_keys, \"verified_slot::\" .. key_part)\n" +
            "            end\n" +
            "            table.insert(fields_to_delete, field)\n" +
            "        end\n" +
            "    end\n" +
            "until cursor == \"0\"\n" +
            "if #verified_slot_cache_keys > 0 then\n" +
            "    redis.call('del', unpack(verified_slot_cache_keys))\n" +
            "end\n" +
            "if #fields_to_delete > 0 then\n" +
            "    delcount=redis.call('hdel', hash_name, unpack(fields_to_delete))\n" +
            "end\n" +
            "return delcount\n";
    private String cleanupScriptHash = null;


    private static final String ADD_SLOT_SCRIPT = "local function add_to_hset(key, field, value, max_count)\n" +
            "    local count = redis.call('HLEN', key) or 0\n" +
            "    max_count = tonumber(max_count)\n" +
            "    if count < max_count then\n" +
            "        redis.call('HSET', key, field, value)\n" +
            "        return count\n" +
            "    else\n" +
            "        return -1\n" +
            "    end\n" +
            "end\n" +
            "\n" +
            "return add_to_hset(KEYS[1], ARGV[1], ARGV[2], ARGV[3])\n";
    private String addSlotScriptHash = null;

    private static final String UPDATE_SLOT_EXPIRE_DT_SCRIPT = "local function update_slot_expire_dt(key, field, value)\n" +
            "redis.call('HSET', key, field, value)\n" +
            "end\n" +
            "\n" +
            "return update_slot_expire_dt(KEYS[1], ARGV[1], ARGV[2], ARGV[3])\n";
    private String updateSlotExpireDtScriptHash = null;



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

    @Cacheable(value = SignUpConstants.ACCESS_TOKEN, key = "'access_token'")
    public String fetchAccessTokenFromIAMServer() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "client_credentials");

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(authTokenEndpoint, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        log.error("Failed to retrieve token from IAM: {}", response.getBody());
        throw new SignUpException(ErrorConstants.TOKEN_REQUEST_FAILED);
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
        log.debug("IdentityVerificationTransaction initiated with status : {} and errorCode: {}",
                identityVerificationTransaction.getStatus(), identityVerificationTransaction.getErrorCode());
        return identityVerificationTransaction;
    }

    @CacheEvict(value = SignUpConstants.IDENTITY_VERIFICATION, key = "#transactionId")
    @Cacheable(value = SignUpConstants.SLOT_ALLOTTED, key = "#transactionId")
    public IdentityVerificationTransaction setSlotAllottedTransaction(String transactionId,
                                                                      IdentityVerificationTransaction identityVerificationTransaction) {
        log.debug("IdentityVerificationTransaction slot allotted with status : {} and errorCode: {}",
                identityVerificationTransaction.getStatus(), identityVerificationTransaction.getErrorCode());
        return identityVerificationTransaction;
    }

    @CacheEvict(value = SignUpConstants.SLOT_ALLOTTED, key = "#transactionId")
    @Cacheable(value = SignUpConstants.VERIFIED_SLOT, key = "#slotId")
    public IdentityVerificationTransaction setVerifiedSlotTransaction(String transactionId, String slotId,
                                                                      IdentityVerificationTransaction identityVerificationTransaction) {
        log.debug("IdentityVerificationTransaction inserted with status : {} and errorCode: {}",
                identityVerificationTransaction.getStatus(), identityVerificationTransaction.getErrorCode());
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

    public IdentityVerificationTransaction
    getVerifiedSlotTransaction(String slotId) {
        return cacheManager.getCache(SignUpConstants.VERIFIED_SLOT).get(slotId, IdentityVerificationTransaction.class); //NOSONAR getCache() will not be returning null here.
    }

    public JsonNode getIdentityVerifierMetadata(String identityVerifierId) {
        return cacheManager.getCache(SignUpConstants.IDENTITY_VERIFIER_METADATA).get(identityVerifierId, JsonNode.class); //NOSONAR getCache() will not be returning null here.
    }

    public void updateVerifiedSlotTransaction(String slotId, IdentityVerificationTransaction transaction) {
        if(cacheManager.getCache(SignUpConstants.VERIFIED_SLOT) != null) {
            log.debug("IdentityVerificationTransaction updated with status : {} and errorCode: {}",
                    transaction.getStatus(), transaction.getErrorCode());
            cacheManager.getCache(SignUpConstants.VERIFIED_SLOT).put(slotId, transaction);
        }
    }

    public void updateVerificationStatus(String haltedTransactionId, String status, String errorCode) {
        String cacheKey = Constants.HALTED_CACHE + "::" + haltedTransactionId;
        OIDCTransaction oidcTransaction = (OIDCTransaction) redisTemplate.opsForValue().get(cacheKey);
        if(oidcTransaction != null) {
            oidcTransaction.setVerificationStatus(status);
            oidcTransaction.setVerificationErrorCode(errorCode);
            long ttl = redisTemplate.getExpire(cacheKey);
            redisTemplate.opsForValue().set(cacheKey, oidcTransaction, ttl, TimeUnit.SECONDS);
        }
    }

    public void removeFromSlotConnected(String value) {
        redisConnectionFactory.getConnection().hDel(SLOTS_CONNECTED.getBytes(), value.getBytes());
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
        if (redisConnectionFactory.getConnection() != null) {
            if (isScriptNotLoaded(cleanupScriptHash)) {
                cleanupScriptHash = redisConnectionFactory.getConnection().scriptingCommands().scriptLoad(CLEANUP_SCRIPT.getBytes());
            }
            LockAssert.assertLocked();
            log.info("Running scheduled cleanup task - task to clear expired slots with script hash: {} {}", cleanupScriptHash,
                    SLOTS_CONNECTED);

            long keysDeleted = redisConnectionFactory.getConnection().scriptingCommands().evalSha(
                    cleanupScriptHash,
                    ReturnType.INTEGER,
                    1,  // Number of keys
                    SLOTS_CONNECTED.getBytes() // The Redis hash name (key)
            );
            log.info("Running scheduled cleanup task - Keys Deleted count: {}", keysDeleted);
        }
    }

    public Long getSetSlotCount(String field, long expireTimeInMillis, Integer maxCount) {
        if (redisConnectionFactory.getConnection() != null) {
            if (isScriptNotLoaded(addSlotScriptHash)) {
                addSlotScriptHash = redisConnectionFactory.getConnection().scriptingCommands().scriptLoad(ADD_SLOT_SCRIPT.getBytes());
            }
            log.info("Running ADD_SLOT_SCRIPT script: {} {} {}", addSlotScriptHash, SLOTS_CONNECTED, maxCount);

            // Convert field and maxCount to appropriate types
            return redisConnectionFactory.getConnection().scriptingCommands().evalSha(
                    addSlotScriptHash,
                    ReturnType.INTEGER,
                    1,  // Number of keys (SLOTS_CONNECTED is the key here)
                    SLOTS_CONNECTED.getBytes(StandardCharsets.UTF_8),  // key (first argument in Lua script)
                    field.getBytes(StandardCharsets.UTF_8),  // field (second argument in Lua script)
                    String.valueOf(expireTimeInMillis).getBytes(StandardCharsets.UTF_8),  // value
                    String.valueOf(maxCount).getBytes(StandardCharsets.UTF_8)  // maxCount
            );
        }
        return -1L;
    }

    public void updateSlotExpireTime(String field, long expireTimeInMillis) {
        if (redisConnectionFactory.getConnection() != null) {
            if (isScriptNotLoaded(updateSlotExpireDtScriptHash)) {
                updateSlotExpireDtScriptHash = redisConnectionFactory.getConnection().scriptingCommands().scriptLoad(UPDATE_SLOT_EXPIRE_DT_SCRIPT.getBytes());
            }
            log.info("Running UPDATE_SLOT_EXPIRE_DT_SCRIPT script: {} {} {}", updateSlotExpireDtScriptHash, SLOTS_CONNECTED, field);

            redisConnectionFactory.getConnection().scriptingCommands().evalSha(
                    updateSlotExpireDtScriptHash,
                    ReturnType.INTEGER,
                    1,  // Number of keys (SLOTS_CONNECTED is the key here)
                    SLOTS_CONNECTED.getBytes(StandardCharsets.UTF_8),  // key (first argument in Lua script)
                    field.getBytes(StandardCharsets.UTF_8),  // field (second argument in Lua script)
                    String.valueOf(expireTimeInMillis).getBytes(StandardCharsets.UTF_8) // value
            );
        }
    }

    private boolean isScriptNotLoaded(String scriptHash) {
        if(scriptHash == null) return true;
        List<Boolean> scriptExists = redisConnectionFactory.getConnection().scriptingCommands().scriptExists(scriptHash);
        return scriptExists == null || !scriptExists.get(0);
    }

}
