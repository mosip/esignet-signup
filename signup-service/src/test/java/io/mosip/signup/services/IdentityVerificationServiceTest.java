/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;


@RunWith(MockitoJUnitRunner.class)
public class IdentityVerificationServiceTest {

    @InjectMocks
    IdentityVerificationService identityVerificationService;

    @Mock
    CacheUtilService cacheUtilService;

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    ProfileRegistryPlugin profileRegistryPlugin;

    private MockWebServer mockWebServer;

    private String mockServerUri;
    private int port;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockWebServer = new MockWebServer();

        mockWebServer.start();
        port=mockWebServer.getPort();
        mockServerUri ="http://localhost:"+port;
        ReflectionTestUtils.setField(identityVerificationService, "privateKeyAlias", "signup");
        ReflectionTestUtils.setField(identityVerificationService, "p12FilePath", "keystore.p12");
        ReflectionTestUtils.setField(identityVerificationService, "audience", "http://localhost:"+port+"/signup.dev.mosip.net/v1/esignet/oauth/token");
        ReflectionTestUtils.setField(identityVerificationService, "p12FilePassword", "mosip");
        ReflectionTestUtils.setField(identityVerificationService, "oauthClientId", "clientId");
        ReflectionTestUtils.setField(identityVerificationService, "oauthRedirectUri", "https://signup.dev.mosip.net/identity-verification");
        ReflectionTestUtils.setField(identityVerificationService, "oauthTokenUri", mockServerUri);
        ReflectionTestUtils.setField(identityVerificationService, "objectMapper", objectMapper);
        KeyPair keyPair = generateRSAKeyPair();
        X509Certificate certificate = generateSelfSignedCertificate(keyPair);
        createAndStoreKeyInP12(keyPair, certificate);
    }

    @AfterEach
    public void shutDownMockSever() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    public void getStatus_withStatusAsCompleted_thenPass(){

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setStatus(VerificationStatus.COMPLETED);
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(identityVerificationTransaction);
        identityVerificationService.getStatus("testTransactionId###123");
        Mockito.verify(cacheUtilService, Mockito.times(1)).setSharedVerificationResult(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void getStatus_withUpdateStatusAsPending_thenPass(){

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setStatus(VerificationStatus.UPDATE_PENDING);
        identityVerificationTransaction.setApplicationId("testApplicationId");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(identityVerificationTransaction);
        Mockito.when(profileRegistryPlugin.getProfileCreateUpdateStatus("testApplicationId")).thenReturn(ProfileCreateUpdateStatus.PENDING);
        identityVerificationService.getStatus("testTransactionId###123");
        Mockito.verify(cacheUtilService, Mockito.times(1)).setSharedVerificationResult(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void getStatus_withUpdateStatusAsCompleted_thenPass(){

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setStatus(VerificationStatus.UPDATE_PENDING);
        identityVerificationTransaction.setApplicationId("testApplicationId");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(identityVerificationTransaction);
        Mockito.when(profileRegistryPlugin.getProfileCreateUpdateStatus("testApplicationId")).thenReturn(ProfileCreateUpdateStatus.COMPLETED);
        identityVerificationService.getStatus("testTransactionId###123");
        Mockito.verify(cacheUtilService, Mockito.times(1)).setSharedVerificationResult(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void getStatus_withUpdateStatusAsFailed_thenPass(){

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setStatus(VerificationStatus.UPDATE_PENDING);
        identityVerificationTransaction.setApplicationId("testApplicationId");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(identityVerificationTransaction);
        Mockito.when(profileRegistryPlugin.getProfileCreateUpdateStatus("testApplicationId")).thenReturn(ProfileCreateUpdateStatus.FAILED);
        identityVerificationService.getStatus("testTransactionId###123");
        Mockito.verify(cacheUtilService, Mockito.times(1)).setSharedVerificationResult(Mockito.anyString(), Mockito.anyString());
    }



    @Test
    public void initiateIdentityVerification_withValidDetails_thenPass() throws IOException {

        String oauthIssuerUri="http://localhost:"+port+"/esignet.dev.mosip.net/v1/esignet/oidc/userinfo";
        ClassPathResource resource = new ClassPathResource("keystore.p12");
        Path absolutePath = Paths.get(resource.getURI());
        ReflectionTestUtils.setField(identityVerificationService, "p12FilePath", absolutePath.toString());
        ReflectionTestUtils.setField(identityVerificationService, "oauthUserinfoUri", oauthIssuerUri);
        ReflectionTestUtils.setField(identityVerificationService, "oauthTokenUri", mockServerUri);
        InitiateIdentityVerificationRequest request = new InitiateIdentityVerificationRequest();
        request.setAuthorizationCode("authCode");
        request.setState("state");
        MockResponse response = new MockResponse()
                .setBody("{\n" +
                        "    \"id_token\": \"eyJraWQiOiJkdmt6enRuanJ3WS11azU1V3hGN0FCNWFTOHFPODFGd2o5T05UTERZLVI0IiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoid2VCMlhGUHE0eFpHNF9VUFhYckNpZyIsInN1YiI6Ijk3Q3NiYk01amY3MHBMbmpWaW9PaGNvclFiMXVWbE5leUNXM2ZRT2VuY00iLCJhdWQiOiIxMjM0NTY3ODkwIiwiYWNyIjoibW9zaXA6aWRwOmFjcjpnZW5lcmF0ZWQtY29kZSIsImF1dGhfdGltZSI6MTcxNzA2MjU5OCwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODhcL3YxXC9lc2lnbmV0IiwiZXhwIjoxNzE3MDY2MjAxLCJpYXQiOjE3MTcwNjI2MDEsIm5vbmNlIjoiOTczZWllbGp6bmcifQ.Yopt54_l9CBZUcYA3cWxXugnMfwgOkn0H8QEFtaQBTq_SkBoqabt0_5cGMoc7U_WhUnhRFfWNZKuUrrmCZyrnOnrmh_-qQ004lql_GfXc-vrWSCVYx9FK_G5E115a6ltT_XtH3Ur_9Fw7QOk08WIzBU6BOi-lz7Q46lGzAR2tGVQ6tXvYjpRtM1WNkyL33KJNGS2bBGqglK7CzPm3Pj7tyM9nXuusNOGVed_PgbG-Ur7ItpZWy1feDd8WmynXibM6kc95cCg2a4GHpJxjb437ls_OYEHjDm6F0eWyAdemp6mXLIsj7fZYu_razwaXYSW2ZKg6XQLyqZfHa2j5AWnXA\",\n" +
                        "    \"token_type\": \"Bearer\",\n" +
                        "    \"access_token\": \"eyJraWQiOiJkdmt6enRuanJ3WS11azU1V3hGN0FCNWFTOHFPODFGd2o5T05UTERZLVI0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI5N0NzYmJNNWpmNzBwTG5qVmlvT2hjb3JRYjF1VmxOZXlDVzNmUU9lbmNNIiwiYXVkIjoiMTIzNDU2Nzg5MCIsImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDg4XC92MVwvZXNpZ25ldCIsImV4cCI6MTcxNzA2NjIwMSwiaWF0IjoxNzE3MDYyNjAxLCJjbGllbnRfaWQiOiIxMjM0NTY3ODkwIn0.dVST9YDnTksOs764zJlYq4Qxam2foY6nbNhHadUMBWEubzXOV85g9oWOOoRtyJTHPpzIWGLRQ_XrO4GyKN0RvG2me9atwTIMAUrKAh2sPusNiZSLkaAtrOu1Oppfe0ob03ozJVmHtwaL8fPwEQ1icJktjEqSDpTMgZG333K9rZaA8wSqVExaF94PvONWSxrv9EmfIMJssJdIRrqWPqPbrh6Jx-WYMe3pdGyxZTh_V6EeCvbMbHTpfG0Kg0ZD8BcsAaDmw1Sk6fehEiW2RuJK_0Vp_4I9_IsOzuSYcFafxhNpkJjnCLNfnqvXVm5qXi-x2WB9l54RHnpTrmQ3yitWZg\",\n" +
                        "    \"expires_in\": 3600\n" +
                        "}")
                .addHeader("Content-Type", "application/json");
        mockWebServer.enqueue(response);
        mockWebServer.url(mockServerUri);
        MockResponse response1 = new MockResponse()
                .setBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\n")
                .addHeader("Content-Type", "application/jwt");
        mockWebServer.enqueue(response1);

        IdentityVerifierDetail [] identityVerifierDetails = new IdentityVerifierDetail[1];
        Mockito.when(cacheUtilService.getIdentityVerifierDetails()).thenReturn(identityVerifierDetails);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);

        InitiateIdentityVerificationResponse result = identityVerificationService.initiateIdentityVerification(request, httpServletResponse);
        Assert.assertNotNull(result);
    }


    @Test
    public void initiateIdentityVerification_withInValidUserRequestDetails_thenFail() throws IOException {

        String oauthIssuerUri="http://localhost:"+port+"/esignet.dev.mosip.net/v1/esignet/oidc/userinfo";
        ClassPathResource resource = new ClassPathResource("keystore.p12");
        Path absolutePath = Paths.get(resource.getURI());
        ReflectionTestUtils.setField(identityVerificationService, "p12FilePath", absolutePath.toString());
        ReflectionTestUtils.setField(identityVerificationService, "oauthUserinfoUri", oauthIssuerUri);
        ReflectionTestUtils.setField(identityVerificationService, "oauthTokenUri", mockServerUri);
        InitiateIdentityVerificationRequest request = new InitiateIdentityVerificationRequest();
        request.setAuthorizationCode("authCode");
        request.setState("state");
        MockResponse response = new MockResponse()
                .setBody("{\n" +
                        "    \"id_token\": \"eyJraWQiOiJkdmt6enRuanJ3WS11azU1V3hGN0FCNWFTOHFPODFGd2o5T05UTERZLVI0IiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoid2VCMlhGUHE0eFpHNF9VUFhYckNpZyIsInN1YiI6Ijk3Q3NiYk01amY3MHBMbmpWaW9PaGNvclFiMXVWbE5leUNXM2ZRT2VuY00iLCJhdWQiOiIxMjM0NTY3ODkwIiwiYWNyIjoibW9zaXA6aWRwOmFjcjpnZW5lcmF0ZWQtY29kZSIsImF1dGhfdGltZSI6MTcxNzA2MjU5OCwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODhcL3YxXC9lc2lnbmV0IiwiZXhwIjoxNzE3MDY2MjAxLCJpYXQiOjE3MTcwNjI2MDEsIm5vbmNlIjoiOTczZWllbGp6bmcifQ.Yopt54_l9CBZUcYA3cWxXugnMfwgOkn0H8QEFtaQBTq_SkBoqabt0_5cGMoc7U_WhUnhRFfWNZKuUrrmCZyrnOnrmh_-qQ004lql_GfXc-vrWSCVYx9FK_G5E115a6ltT_XtH3Ur_9Fw7QOk08WIzBU6BOi-lz7Q46lGzAR2tGVQ6tXvYjpRtM1WNkyL33KJNGS2bBGqglK7CzPm3Pj7tyM9nXuusNOGVed_PgbG-Ur7ItpZWy1feDd8WmynXibM6kc95cCg2a4GHpJxjb437ls_OYEHjDm6F0eWyAdemp6mXLIsj7fZYu_razwaXYSW2ZKg6XQLyqZfHa2j5AWnXA\",\n" +
                        "    \"token_type\": \"Bearer\",\n" +
                        "    \"access_token\": \"eyJraWQiOiJkdmt6enRuanJ3WS11azU1V3hGN0FCNWFTOHFPODFGd2o5T05UTERZLVI0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI5N0NzYmJNNWpmNzBwTG5qVmlvT2hjb3JRYjF1VmxOZXlDVzNmUU9lbmNNIiwiYXVkIjoiMTIzNDU2Nzg5MCIsImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDg4XC92MVwvZXNpZ25ldCIsImV4cCI6MTcxNzA2NjIwMSwiaWF0IjoxNzE3MDYyNjAxLCJjbGllbnRfaWQiOiIxMjM0NTY3ODkwIn0.dVST9YDnTksOs764zJlYq4Qxam2foY6nbNhHadUMBWEubzXOV85g9oWOOoRtyJTHPpzIWGLRQ_XrO4GyKN0RvG2me9atwTIMAUrKAh2sPusNiZSLkaAtrOu1Oppfe0ob03ozJVmHtwaL8fPwEQ1icJktjEqSDpTMgZG333K9rZaA8wSqVExaF94PvONWSxrv9EmfIMJssJdIRrqWPqPbrh6Jx-WYMe3pdGyxZTh_V6EeCvbMbHTpfG0Kg0ZD8BcsAaDmw1Sk6fehEiW2RuJK_0Vp_4I9_IsOzuSYcFafxhNpkJjnCLNfnqvXVm5qXi-x2WB9l54RHnpTrmQ3yitWZg\",\n" +
                        "    \"expires_in\": 3600\n" +
                        "}")
                .addHeader("Content-Type", "application/json");

        mockWebServer.enqueue(response);
        MockResponse response1 = new MockResponse().setHttp2ErrorCode(400).setStatus("Bad Request");
        mockWebServer.enqueue(response1);
        mockWebServer.url(mockServerUri);

        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        try{
            identityVerificationService.initiateIdentityVerification(request, httpServletResponse);
            Assert.fail();
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.USERINFO_FAILED);
        }
    }

    @Test
    public void initiateIdentityVerification_withInValidDetails_thenFail() {

        InitiateIdentityVerificationRequest request = new InitiateIdentityVerificationRequest();
        request.setAuthorizationCode("authCode");
        request.setState("state");

        MockResponse response = new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal Server Error\"}")
                .addHeader("Content-Type", "application/json");

        mockWebServer.enqueue(response);
        mockWebServer.url(mockServerUri);

        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);

        try{
            identityVerificationService.initiateIdentityVerification(request, httpServletResponse);
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.GRANT_EXCHANGE_FAILED);
        }
    }

    @Test
    public void initiateIdentityVerification_withInvalidPrivateKeyPassword_thenFail() {

        ReflectionTestUtils.setField(identityVerificationService, "p12FilePassword", "wrongpassword");
        InitiateIdentityVerificationRequest request = new InitiateIdentityVerificationRequest();
        request.setAuthorizationCode("authCode");
        request.setState("state");
        try {
            identityVerificationService.initiateIdentityVerification(request,null);
        }catch (Exception e) {
            Assert.assertEquals(ErrorConstants.GRANT_EXCHANGE_FAILED, e.getMessage());
        }
    }

    @Test
    public void getSlot_withValidDetails_thenPass()  {

        ReflectionTestUtils.setField(identityVerificationService, "slotMaxCount", 100);
        String transactionId = "testTransactionId";
        SlotRequest slotRequest = new SlotRequest();
        slotRequest.setVerifierId("testVerifierId");

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("testSlotId");
        Mockito.when(cacheUtilService.getIdentityVerificationTransaction(transactionId)).thenReturn(identityVerificationTransaction);

        IdentityVerifierDetail [] identityVerifierDetails = new IdentityVerifierDetail[1];
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setId("testVerifierId");
        identityVerifierDetail.setActive(true);
        identityVerifierDetails[0] = identityVerifierDetail;

        Mockito.when(cacheUtilService.getIdentityVerifierDetails()).thenReturn(identityVerifierDetails);
        Mockito.when(cacheUtilService.getCurrentSlotCount()).thenReturn(10L);
        Mockito.when(cacheUtilService.setSlotAllottedTransaction(Mockito.anyString(), Mockito.any())).thenReturn(
                identityVerificationTransaction); // Assuming maxSlotPoolSize > 10
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        // Execute
        SlotResponse result = identityVerificationService.getSlot(transactionId, slotRequest, httpServletResponse);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result);
        Assert.assertEquals(identityVerificationTransaction.getSlotId(), result.getSlotId());

    }

    @Test
    public void getSlot_withInValidTransaction_thenFail() {
        Mockito.when(cacheUtilService.getIdentityVerificationTransaction(Mockito.anyString())).thenReturn(null);
        try{
            identityVerificationService.getSlot("transactionId", null, null);
        }catch (SignUpException e){
            Assert.assertEquals(ErrorConstants.INVALID_TRANSACTION, e.getErrorCode());
        }
    }

    @Test
    public void getSlot_withInValidVerifierId_thenFail()  {
        SlotRequest slotRequest = new SlotRequest();
        slotRequest.setVerifierId("testVerifierId2");

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("testSlotId");
        Mockito.when(cacheUtilService.getIdentityVerificationTransaction(Mockito.anyString())).thenReturn(identityVerificationTransaction);

        IdentityVerifierDetail [] identityVerifierDetails = new IdentityVerifierDetail[1];
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setId("testVerifierId");
        identityVerifierDetail.setActive(true);
        identityVerifierDetails[0] = identityVerifierDetail;

        Mockito.when(cacheUtilService.getIdentityVerifierDetails()).thenReturn(identityVerifierDetails);
        try{
            identityVerificationService.getSlot("transactionId", slotRequest, null);
        }catch (SignUpException e){
            Assert.assertEquals(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID, e.getErrorCode());
        }
    }


    @Test
    public void getSlot_withFullSlot_thenFail()  {

        ReflectionTestUtils.setField(identityVerificationService, "slotMaxCount", 100);
        SlotRequest slotRequest = new SlotRequest();
        slotRequest.setVerifierId("testVerifierId");

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setSlotId("testSlotId");
        Mockito.when(cacheUtilService.getIdentityVerificationTransaction(Mockito.anyString())).thenReturn(identityVerificationTransaction);

        IdentityVerifierDetail [] identityVerifierDetails = new IdentityVerifierDetail[1];
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setId("testVerifierId");
        identityVerifierDetail.setActive(true);
        identityVerifierDetails[0] = identityVerifierDetail;

        Mockito.when(cacheUtilService.getIdentityVerifierDetails()).thenReturn(identityVerifierDetails);
        Mockito.when(cacheUtilService.getCurrentSlotCount()).thenReturn(100L);
        // Execute
        try{
            identityVerificationService.getSlot("transactionId", slotRequest, null);
        }catch (SignUpException e){
            Assert.assertEquals(ErrorConstants.SLOT_NOT_AVAILABLE, e.getErrorCode());
        }
    }

    @Test
    public void getIdentityVerifierDetails_withValidDetails_thenPass() throws IOException {

        String transactionId = "validTransactionId";
        String identityVerifierId = "validIdentityVerifierId";
        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction(); // Populate as needed
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setId(identityVerifierId);
        identityVerifierDetail.setActive(true);
        IdentityVerifierDetail[] verifierDetails = {identityVerifierDetail, new IdentityVerifierDetail()};
        Mockito.when(cacheUtilService.getIdentityVerificationTransaction(Mockito.anyString())).thenReturn(transaction);
        Mockito.when(cacheUtilService.setIdentityVerifierDetails(Mockito.anyString(),Mockito.any())).thenReturn(verifierDetails);

        InputStream verifierDetailsInputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(verifierDetails));
        Resource verifierDetailsMockResource = Mockito.mock(Resource.class);
        Mockito.when(verifierDetailsMockResource.getInputStream()).thenReturn(verifierDetailsInputStream);

        InputStream metadataInputStream = new ByteArrayInputStream("{\"id\":\"validIdentityVerifierId\",\"active\":true}".getBytes());
        Resource metadataMockResource = Mockito.mock(Resource.class);
        Mockito.when(metadataMockResource.getInputStream()).thenReturn(metadataInputStream);
        Mockito.when(resourceLoader.getResource(Mockito.anyString())).thenReturn(verifierDetailsMockResource, metadataMockResource);

        Mockito.when(cacheUtilService.setIdentityVerifierMetadata(Mockito.anyString(),Mockito.any())).thenReturn(objectMapper.createObjectNode());

        JsonNode result = identityVerificationService.getIdentityVerifierDetails(transactionId, identityVerifierId);
        Assert.assertNotNull(result);
    }


    @Test
    public void getIdentityVerifierDetails_withInValidDetails_thenFail() throws IOException {

        String transactionId = "validTransactionId";
        String identityVerifierId = "validIdentityVerifierId";
        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction(); // Populate as needed
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setId("invalidIdentityVerifierId");
        identityVerifierDetail.setActive(true);
        IdentityVerifierDetail[] verifierDetails = {identityVerifierDetail, new IdentityVerifierDetail()};
        Mockito.when(cacheUtilService.getIdentityVerificationTransaction(Mockito.anyString())).thenReturn(transaction);
        Mockito.when(cacheUtilService.setIdentityVerifierDetails(Mockito.anyString(),Mockito.any())).thenReturn(verifierDetails);

        InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(verifierDetails));
        Resource mockResource = Mockito.mock(Resource.class);
        Mockito.when(mockResource.getInputStream()).thenReturn(inputStream);
        Mockito.when(resourceLoader.getResource(Mockito.anyString())).thenReturn(mockResource);

        try{
            identityVerificationService.getIdentityVerifierDetails(transactionId, identityVerifierId);
        }catch (SignUpException e){
            Assert.assertEquals(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID, e.getErrorCode());
        }
    }


    private KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        X500Name dnName = new X500Name("CN=Test");
        BigInteger certSerialNumber = new BigInteger(Long.toString(now));
        Date endDate = new Date(now + 365 * 24 * 60 * 60 * 1000L);

        // Convert PublicKey to SubjectPublicKeyInfo
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, publicKeyInfo);
        X509CertificateHolder certHolder = certBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().getCertificate(certHolder);
    }

    private void createAndStoreKeyInP12(KeyPair keyPair, X509Certificate certificate) throws Exception {
        // Initialize a KeyStore of type PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("signup", keyPair.getPrivate(), "mosip".toCharArray(), new Certificate[]{certificate});
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resourceDirectory = resourceLoader.getResource("classpath:");
        File directory = resourceDirectory.getFile();
        // Create the keystore.p12 file in the resources directory
        File file = new File(directory, "keystore.p12");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            keyStore.store(fos, "mosip".toCharArray());
        }
    }
}
