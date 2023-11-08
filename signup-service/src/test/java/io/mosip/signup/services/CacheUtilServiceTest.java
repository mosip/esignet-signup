package io.mosip.signup.services;

import io.mosip.signup.dto.RegistrationTransaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@RunWith(MockitoJUnitRunner.class)
public class CacheUtilServiceTest {
    @InjectMocks
    private CacheUtilService cacheUtilService;

    @Mock
    private Cache cache;

    @Mock
    private CacheManager cacheManager;

    @Test
    public void test_RegistrationTransaction_cache() {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction();
        registrationTransaction.setOtp("123456");
        registrationTransaction.setIdentifier("85512123123");

        Mockito.when(cache.get("mock", RegistrationTransaction.class)).thenReturn(registrationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertEquals(cacheUtilService.setChallengeGeneratedTransaction("mock",
                registrationTransaction), registrationTransaction);
        Assert.assertEquals(cacheUtilService.setChallengeVerifiedTransaction("mock",
                registrationTransaction), registrationTransaction);
        Assert.assertEquals(cacheUtilService.setRegisteredTransaction("mock",
                registrationTransaction), registrationTransaction);

        Assert.assertNotNull(cacheUtilService.getChallengeGeneratedTransaction("mock"));
        Assert.assertNotNull(cacheUtilService.getChallengeVerifiedTransaction("mock"));
        Assert.assertNotNull(cacheUtilService.getRegisteredTransaction("mock"));
    }
}
