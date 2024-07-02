package io.mosip.signup.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    }

    @Test
    public void setChallengeTransaction_thenPass() {
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        Assert.assertEquals(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction), registrationTransaction);
        Assert.assertNotNull(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction));
    }


    @Test
    public void test_IdentityVerificationTransaction_cache() {
        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("123456");
        identityVerificationTransaction.setIndividualId("1234567890");
        identityVerificationTransaction.setVerifierId("1234567890");

        Mockito.when(cache.get("mock", IdentityVerificationTransaction.class)).thenReturn(identityVerificationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertEquals(cacheUtilService.setSlotAllottedTransaction("mock",identityVerificationTransaction), identityVerificationTransaction);
        Assert.assertEquals(cacheUtilService.setIdentityVerificationTransaction("mock",
                identityVerificationTransaction), identityVerificationTransaction);

        Assert.assertNotNull(cacheUtilService.getIdentityVerificationTransaction("mock"));
        Assert.assertNotNull(cacheUtilService.getSlotAllottedTransaction("mock"));
    }

    @Test
    public void test_JsonNode_cache() throws JsonProcessingException {
        String identityVerifierId = "testVerifierId";
        String jsonContent = "{\"key\":\"value\"}";

        // Convert JSON string to JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedJsonNode = objectMapper.readTree(jsonContent);

        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(JsonNode.class))).thenReturn(expectedJsonNode);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertEquals(cacheUtilService.setIdentityVerifierMetadata(identityVerifierId,expectedJsonNode), expectedJsonNode);
        Assert.assertNotNull(cacheUtilService.getIdentityVerifierMetadata(identityVerifierId));
    }

    @Test
    public void test_IdentityVerifierDetail_cache() throws JsonProcessingException {

        IdentityVerifierDetail[] identityVerifierDetails = new IdentityVerifierDetail[1];
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setActive(true);
        identityVerifierDetail.setId("123456");
        identityVerifierDetails[0]= identityVerifierDetail;


        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(IdentityVerifierDetail[].class))).thenReturn(identityVerifierDetails);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertEquals(cacheUtilService.setIdentityVerifierDetails("KEY",identityVerifierDetails), identityVerifierDetails);
        Assert.assertNotNull(cacheUtilService.getIdentityVerifierDetails());
    }


    @Test
    public void test_isIdentifierBlocked(){

        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(null);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertFalse(cacheUtilService.isIdentifierBlocked("identifier"));
        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(String.class))).thenReturn("value");
        Assert.assertTrue(cacheUtilService.isIdentifierBlocked("identifier"));
    }


    @Test
    public void test_String_cache() {

        Assert.assertEquals(cacheUtilService.blockIdentifier("transId", "key", "mock"), "mock");
        Assert.assertEquals(cacheUtilService.setSecretKey("transId", "mock"), "mock");
        Assert.assertEquals(cacheUtilService.setActiveKeyAlias("transId", "mock"), "mock");
    }

    @Test
    public void test_getCurrentSlotCount(){

        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(Long.class))).thenReturn(null);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertEquals(cacheUtilService.getCurrentSlotCount(),0L);
        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(Long.class))).thenReturn(5L);
        Assert.assertEquals(cacheUtilService.getCurrentSlotCount(),5L);
    }


}
