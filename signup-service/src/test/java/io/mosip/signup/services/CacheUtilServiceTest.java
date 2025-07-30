/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import io.mosip.esignet.core.constants.Constants;
import io.mosip.esignet.core.dto.OIDCTransaction;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.helper.CryptoHelper;
import io.mosip.signup.util.Purpose;
import io.mosip.signup.util.SignUpConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import static org.mockito.ArgumentMatchers.*;


@RunWith(MockitoJUnitRunner.class)
public class CacheUtilServiceTest {
    @InjectMocks
    private CacheUtilService cacheUtilService;

    @Mock
    private Cache cache;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Test
    public void test_RegistrationTransaction_cache() {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");

        Mockito.when(cache.get("mock", RegistrationTransaction.class)).thenReturn(registrationTransaction);
        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);

        Assert.assertEquals(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock",
                registrationTransaction), registrationTransaction);
        Assert.assertEquals(cacheUtilService.setChallengeVerifiedTransaction("mock", "vmock",
                registrationTransaction), registrationTransaction);
        Assert.assertEquals(cacheUtilService.setStatusCheckTransaction("mock",
                registrationTransaction), registrationTransaction);

        Assert.assertNotNull(cacheUtilService.getChallengeGeneratedTransaction("mock"));
        Assert.assertNotNull(cacheUtilService.getChallengeVerifiedTransaction("mock"));
        Assert.assertNotNull(cacheUtilService.getStatusCheckTransaction("mock"));
        Assert.assertEquals(cacheUtilService.blockIdentifier("mock-transaction", "key","value"), "value");
        Assert.assertEquals(cacheUtilService.setSecretKey("key","value"), "value");
        Assert.assertEquals(cacheUtilService.setActiveKeyAlias("key","value"), "value");
        IdentityVerifierDetail[] identityVerifierDetails = new IdentityVerifierDetail[]{};
        IdentityVerificationTransaction identityVerificationTransaction=new IdentityVerificationTransaction();
        Assert.assertArrayEquals(cacheUtilService.setIdentityVerifierDetails("key",identityVerifierDetails), identityVerifierDetails);
        Assert.assertEquals(cacheUtilService.setIdentityVerificationTransaction("transactionId",identityVerificationTransaction),identityVerificationTransaction);
    }

    @Test
    public void setChallengeTransaction_thenPass() {
        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        Assert.assertEquals(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction), registrationTransaction);
        Assert.assertNotNull(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction));
    }

    @Test
    public void getActiveKeyAlias_withValidCacheKey_thenPass() {
        String expectedAlias = "activeKeyAlias";
        Mockito.when(cacheManager.getCache(SignUpConstants.KEY_ALIAS)).thenReturn(cache);
        Mockito.when(cache.get(eq(CryptoHelper.ALIAS_CACHE_KEY), eq(String.class))).thenReturn(expectedAlias);
        String actualAlias = cacheUtilService.getActiveKeyAlias();
        Assert.assertEquals(expectedAlias, actualAlias);
    }

    @Test
    public void getSetSlotCount_withNullRedisConnection_thenFail() {
        Mockito.when(redisConnectionFactory.getConnection()).thenReturn(null);
        Long result = cacheUtilService.getSetSlotCount("testField", 1000L, 10);
        Assert.assertEquals(Long.valueOf(-1L), result);
    }

    @Test
    public void getSetSlotCount_withValidRedisConnection_thenPass() {
        RedisConnection redisConnection = Mockito.mock(RedisConnection.class);
        RedisScriptingCommands scriptingCommands = Mockito.mock(RedisScriptingCommands.class);
        String scriptHash = "mockScriptHash";
        Long expectedResult = 1L;
        Mockito.when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        Mockito.when(redisConnection.scriptingCommands()).thenReturn(scriptingCommands);
        Mockito.when(scriptingCommands.scriptLoad(any(byte[].class))).thenReturn(scriptHash);
        Mockito.when(scriptingCommands.evalSha(
                eq(scriptHash),
                eq(ReturnType.INTEGER),
                eq(1),
                any(byte[].class),
                any(byte[].class),
                any(byte[].class),
                any(byte[].class)
        )).thenReturn(expectedResult);
        Long result = cacheUtilService.getSetSlotCount("testField", 1000L, 10);
        Assert.assertEquals(expectedResult, result);
        Mockito.verify(scriptingCommands).scriptLoad(any(byte[].class));
        Mockito.verify(scriptingCommands).evalSha(
                eq(scriptHash),
                eq(ReturnType.INTEGER),
                eq(1),
                any(byte[].class),
                any(byte[].class),
                any(byte[].class),
                any(byte[].class)
        );
    }

    @Test
    public void updateSlotExpireTime_withValidRedisConnection_thenPass() {
        String field = "testField";
        long expireTimeInMillis = 1000L;
        RedisConnection redisConnection = Mockito.mock(RedisConnection.class);
        RedisScriptingCommands scriptingCommands = Mockito.mock(RedisScriptingCommands.class);
        String scriptHash = "mockScriptHash";
        Mockito.when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        Mockito.when(redisConnection.scriptingCommands()).thenReturn(scriptingCommands);
        Mockito.when(scriptingCommands.scriptLoad(any(byte[].class))).thenReturn(scriptHash);
        Mockito.when(redisConnection.scriptingCommands().evalSha(
                eq(scriptHash),
                eq(ReturnType.INTEGER),
                eq(1),
                any(byte[].class),
                any(byte[].class),
                any(byte[].class)
        )).thenReturn(1L);
        cacheUtilService.updateSlotExpireTime(field, expireTimeInMillis);
        Mockito.verify(scriptingCommands).scriptLoad(any(byte[].class));
        Mockito.verify(scriptingCommands).evalSha(
                eq(scriptHash),
                eq(ReturnType.INTEGER),
                eq(1),
                any(byte[].class),
                eq(field.getBytes(StandardCharsets.UTF_8)),
                eq(String.valueOf(expireTimeInMillis).getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    public void updateVerifiedSlotTransaction_whenCacheIsNull_thenFail() {
        String slotId = "slot123";
        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        Mockito.when(cacheManager.getCache(SignUpConstants.VERIFIED_SLOT)).thenReturn(null);
        cacheUtilService.updateVerifiedSlotTransaction(slotId, transaction);
        Mockito.verifyNoInteractions(cache);
    }

    @Test
    public void updateVerifiedSlotTransaction_whenCacheExists_thenPass() {
        String slotId = "slot123";
        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setStatus(VerificationStatus.COMPLETED);
        transaction.setErrorCode("NO_ERROR");
        Mockito.when(cacheManager.getCache(SignUpConstants.VERIFIED_SLOT)).thenReturn(cache);
        cacheUtilService.updateVerifiedSlotTransaction(slotId, transaction);
        Mockito.verify(cache).put(slotId, transaction);
    }

    @Test
    public void updateVerificationStatus_whenTransactionIsNull_thenFail() {
        String haltedTransactionId = "txn123";
        String cacheKey = Constants.HALTED_CACHE + "::" + haltedTransactionId;

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(cacheKey)).thenReturn(null);
        cacheUtilService.updateVerificationStatus(haltedTransactionId, "FAILED", "ERROR");
        Mockito.verify(valueOperations, Mockito.never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    public void updateVerificationStatus_whenTransactionExists_thenPass() {
        String haltedTransactionId = "txn123";
        String status = "COMPLETED";
        String errorCode = "ERROR";
        String cacheKey = Constants.HALTED_CACHE + "::" + haltedTransactionId;

        OIDCTransaction transaction = new OIDCTransaction();
        transaction.setVerificationStatus(status);
        transaction.setVerificationErrorCode(errorCode);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(cacheKey)).thenReturn(transaction);
        Mockito.when(redisTemplate.getExpire(cacheKey)).thenReturn(300L);
        cacheUtilService.updateVerificationStatus(haltedTransactionId, status, errorCode);
        Assert.assertEquals(status, transaction.getVerificationStatus());
        Assert.assertEquals(errorCode, transaction.getVerificationErrorCode());
        Mockito.verify(valueOperations).set(cacheKey, transaction, 300L, TimeUnit.SECONDS);
    }

}
