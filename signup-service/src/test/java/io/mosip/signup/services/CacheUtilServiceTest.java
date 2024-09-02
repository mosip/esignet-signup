package io.mosip.signup.services;

import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.RegistrationTransaction;
import io.mosip.signup.util.Purpose;
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

}
