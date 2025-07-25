/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.util.Purpose;
import io.mosip.signup.util.SignUpConstants;
import io.mosip.signup.helper.CryptoHelper;
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

import java.nio.charset.StandardCharsets;

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

    @Test
    public void test_RegistrationTransaction_cache() {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");

        Mockito.when(cache.get("mock", RegistrationTransaction.class)).thenReturn(registrationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

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
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        Assert.assertEquals(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction), registrationTransaction);
        Assert.assertNotNull(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction));
    }

    @Test
    public void getActiveKeyAlias_withValidCacheKey_thenPass() {
        String expectedAlias = "activeKeyAlias";
        Mockito.when(cacheManager.getCache(SignUpConstants.KEY_ALIAS)).thenReturn(cache);
        Mockito.when(cache.get(Mockito.eq(CryptoHelper.ALIAS_CACHE_KEY), Mockito.eq(String.class))).thenReturn(expectedAlias);
        String actualAlias = cacheUtilService.getActiveKeyAlias(CryptoHelper.ALIAS_CACHE_KEY);
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
        Mockito.when(scriptingCommands.scriptLoad(Mockito.any(byte[].class))).thenReturn(scriptHash);
        Mockito.when(scriptingCommands.evalSha(
                Mockito.eq(scriptHash),
                Mockito.eq(ReturnType.INTEGER),
                Mockito.eq(1),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class)
        )).thenReturn(expectedResult);
        Long result = cacheUtilService.getSetSlotCount("testField", 1000L, 10);
        Assert.assertEquals(expectedResult, result);
        Mockito.verify(scriptingCommands).scriptLoad(Mockito.any(byte[].class));
        Mockito.verify(scriptingCommands).evalSha(
                Mockito.eq(scriptHash),
                Mockito.eq(ReturnType.INTEGER),
                Mockito.eq(1),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class)
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
        Mockito.when(scriptingCommands.scriptLoad(Mockito.any(byte[].class))).thenReturn(scriptHash);
        Mockito.when(redisConnection.scriptingCommands().evalSha(
                Mockito.eq(scriptHash),
                Mockito.eq(ReturnType.INTEGER),
                Mockito.eq(1),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class),
                Mockito.any(byte[].class)
        )).thenReturn(1L);
        cacheUtilService.updateSlotExpireTime(field, expireTimeInMillis);
        Mockito.verify(scriptingCommands).scriptLoad(Mockito.any(byte[].class));
        Mockito.verify(scriptingCommands).evalSha(
                Mockito.eq(scriptHash),
                Mockito.eq(ReturnType.INTEGER),
                Mockito.eq(1),
                Mockito.any(byte[].class),
                Mockito.eq(field.getBytes(StandardCharsets.UTF_8)),
                Mockito.eq(String.valueOf(expireTimeInMillis).getBytes(StandardCharsets.UTF_8))
        );
    }
}
