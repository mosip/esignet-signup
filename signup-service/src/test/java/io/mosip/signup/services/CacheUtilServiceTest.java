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
    public void getChallengeVerifiedTransaction_withValidDetails_thenPass()  {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");

        Mockito.when(cache.get("mock", RegistrationTransaction.class)).thenReturn(registrationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertNotNull(cacheUtilService.getChallengeVerifiedTransaction("mock"));

    }

    @Test
    public void getStatusCheckTransaction_withValidDetails_thenPass()  {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");

        Mockito.when(cache.get("mock", RegistrationTransaction.class)).thenReturn(registrationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertNotNull(cacheUtilService.getStatusCheckTransaction("mock"));

    }

    @Test
    public void getChallengeGeneratedTransaction_withValidDetails_thenPass()  {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");

        Mockito.when(cache.get("mock", RegistrationTransaction.class)).thenReturn(registrationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertNotNull(cacheUtilService.getChallengeGeneratedTransaction("mock"));

    }

    @Test
    public void setStatusCheckTransaction_withValidDetails_thenPass()  {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");
        Assert.assertEquals(cacheUtilService.setStatusCheckTransaction("mock",
                registrationTransaction), registrationTransaction);

    }

    @Test
    public void setChallengeVerifiedTransaction_withValidDetails_thenPass()  {
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        registrationTransaction.setChallengeHash("123456-HASH");

        Assert.assertEquals(cacheUtilService.setChallengeVerifiedTransaction("mock", "vmock",
                registrationTransaction), registrationTransaction);

    }

    @Test
    public void setChallengeTransaction_thenPass() {
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        Assert.assertEquals(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction), registrationTransaction);
        Assert.assertNotNull(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction));
    }

    @Test
    public void createUpdateChallengeGeneratedTransaction_withValidDetails_thenPass()  {
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        RegistrationTransaction registrationTransaction = new RegistrationTransaction("+85512123123", Purpose.REGISTRATION);
        Assert.assertEquals(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction), registrationTransaction);
        Assert.assertNotNull(cacheUtilService.createUpdateChallengeGeneratedTransaction("mock-transaction", registrationTransaction));
    }


    @Test
    public void setSlotAllottedTransaction_withValidDetails_thenPass()  {
        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("123456");
        identityVerificationTransaction.setIndividualId("1234567890");
        identityVerificationTransaction.setVerifierId("1234567890");

        Assert.assertEquals(cacheUtilService.setSlotAllottedTransaction("mock",identityVerificationTransaction), identityVerificationTransaction);
    }

    @Test
    public void setIdentityVerificationTransaction_withValidDetails_thenPass()  {
        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("123456");
        identityVerificationTransaction.setIndividualId("1234567890");
        identityVerificationTransaction.setVerifierId("1234567890");

        Assert.assertEquals(cacheUtilService.setIdentityVerificationTransaction("mock",
                identityVerificationTransaction), identityVerificationTransaction);
    }

    @Test
    public void getIdentityVerificationTransaction_withValidDetails_thenPass()  {
        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("123456");
        identityVerificationTransaction.setIndividualId("1234567890");
        identityVerificationTransaction.setVerifierId("1234567890");
        Mockito.when(cache.get("mock", IdentityVerificationTransaction.class)).thenReturn(identityVerificationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertNotNull(cacheUtilService.getIdentityVerificationTransaction("mock"));
    }

    @Test
    public void getSlotAllottedTransaction_withValidDetails_thenPass()  {
        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("123456");
        identityVerificationTransaction.setIndividualId("1234567890");
        identityVerificationTransaction.setVerifierId("1234567890");

        Mockito.when(cache.get("mock", IdentityVerificationTransaction.class)).thenReturn(identityVerificationTransaction);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertNotNull(cacheUtilService.getSlotAllottedTransaction("mock"));
    }


    @Test
    public void setIdentityVerifierMetadata_withValidDetails_thenPass() throws JsonProcessingException {
        String identityVerifierId = "testVerifierId";
        String jsonContent = "{\"key\":\"value\"}";

        // Convert JSON string to JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedJsonNode = objectMapper.readTree(jsonContent);

        Assert.assertEquals(cacheUtilService.setIdentityVerifierMetadata(identityVerifierId,expectedJsonNode), expectedJsonNode);
    }


    @Test
    public void getIdentityVerifierMetadata_withValidDetails_thenPass() throws JsonProcessingException {
        String identityVerifierId = "testVerifierId";
        String jsonContent = "{\"key\":\"value\"}";

        // Convert JSON string to JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedJsonNode = objectMapper.readTree(jsonContent);

        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(JsonNode.class))).thenReturn(expectedJsonNode);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertNotNull(cacheUtilService.getIdentityVerifierMetadata(identityVerifierId));
    }


    @Test
    public void setIdentityVerifierDetails_withValidDetails_thenPass(){
        IdentityVerifierDetail[] identityVerifierDetails = new IdentityVerifierDetail[1];
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setActive(true);
        identityVerifierDetail.setId("123456");
        identityVerifierDetails[0]= identityVerifierDetail;

        Assert.assertEquals(cacheUtilService.setIdentityVerifierDetails("KEY",identityVerifierDetails), identityVerifierDetails);
    }

    @Test
    public void getIdentityVerifierDetails_withValidDetails_thenPass(){
        IdentityVerifierDetail[] identityVerifierDetails = new IdentityVerifierDetail[1];
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setActive(true);
        identityVerifierDetail.setId("123456");
        identityVerifierDetails[0]= identityVerifierDetail;


        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(IdentityVerifierDetail[].class))).thenReturn(identityVerifierDetails);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Assert.assertNotNull(cacheUtilService.getIdentityVerifierDetails());
    }

    @Test
    public void isIdentifierBlocked_withUnblockedIdentifier_thenPass(){
        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(null);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertFalse(cacheUtilService.isIdentifierBlocked("identifier"));
    }

    @Test
    public void isIdentifierBlocked_withBlockedIdentifier_thenPass(){
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(String.class))).thenReturn("value");
        Assert.assertTrue(cacheUtilService.isIdentifierBlocked("identifier"));
    }


    @Test
    public void blockIdentifier_withValidDetails_thenPass(){
        Assert.assertEquals(cacheUtilService.blockIdentifier("transId", "key", "mock"), "mock");
    }

    @Test
    public void setSecretKey_withValidDetails_thenPass(){
        Assert.assertEquals(cacheUtilService.setSecretKey("transId", "mock"), "mock");
    }

    @Test
    public void setActiveKeyAlias_withValidDetails_thenPass(){
        Assert.assertEquals(cacheUtilService.setActiveKeyAlias("transId", "mock"), "mock");
    }



    @Test
    public void getCurrentSlotCount_withValidDetails_thenPass(){

        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(Long.class))).thenReturn(null);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Assert.assertEquals(cacheUtilService.getCurrentSlotCount(),0L);
        Mockito.when(cache.get(Mockito.anyString(), Mockito.eq(Long.class))).thenReturn(5L);
        Assert.assertEquals(cacheUtilService.getCurrentSlotCount(),5L);
    }

}
