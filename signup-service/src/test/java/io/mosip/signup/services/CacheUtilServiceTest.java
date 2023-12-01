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
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", "");
        registrationTransaction.setChallengeHash("123456-HASH");

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

    @Test
    public void setChallengeTransaction_thenPass() {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", "");
        Assert.assertEquals(cacheUtilService.setChallengeGeneratedTransaction("mock-transaction", registrationTransaction), registrationTransaction);
        Assert.assertNotNull(cacheUtilService.setChallengeGeneratedTransaction("mock-transaction", registrationTransaction));
    }
}
