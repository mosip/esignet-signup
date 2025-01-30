package io.mosip.signup.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.signup.api.dto.*;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.exception.ProfileException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.util.AuditEvent;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.ErrorConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static io.mosip.signup.api.util.ErrorConstants.IDENTITY_VERIFICATION_FAILED;
import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@RunWith(SpringRunner.class)
public class WebsocketHandlerTest {

    @InjectMocks
    WebSocketHandler webSocketHandler;

    @Mock
    private IdentityVerifierFactory identityVerifierFactory;

    @Mock
    private ProfileRegistryPlugin profileRegistryPlugin;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private CacheUtilService cacheUtilService;

    @Mock
    private AuditHelper auditHelper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(webSocketHandler, "objectMapper", objectMapper);
    }

    @Test
    public void processFrames_validStartStep_thenPass() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        identityVerificationRequest.setFrames(null);
        identityVerificationRequest.setSlotId("test");
        identityVerificationRequest.setStepCode("START");

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setVerifierId("verifier-id");
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId())).thenReturn(identityVerificationTransaction);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        webSocketHandler.processFrames(identityVerificationRequest);
        Mockito.verify(identityVerifierPlugin, Mockito.times(1)).initialize(Mockito.anyString(), Mockito.any());
        Mockito.verify(identityVerifierPlugin, Mockito.times(1)).verify(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void processFrames_validInput_thenPass() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        identityVerificationRequest.setFrames(Arrays.asList());
        identityVerificationRequest.setSlotId("test");
        identityVerificationRequest.setStepCode("Test-step");

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setVerifierId("verifier-id");
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId())).thenReturn(identityVerificationTransaction);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        webSocketHandler.processFrames(identityVerificationRequest);
        Mockito.verify(identityVerifierPlugin, Mockito.times(0)).initialize(Mockito.anyString(), Mockito.any());
        Mockito.verify(identityVerifierPlugin, Mockito.times(1)).verify(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void processFrames_invalidTransaction_thenFail() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        identityVerificationRequest.setSlotId("test");
        identityVerificationRequest.setStepCode("stepCode");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId())).thenReturn(null);
        webSocketHandler.processFrames(identityVerificationRequest);
        Mockito.verify(cacheUtilService, Mockito.times(1)).getVerifiedSlotTransaction(identityVerificationRequest.getSlotId());
        Mockito.verify(simpMessagingTemplate, Mockito.times(1))
                .convertAndSend(Mockito.eq("/topic/" + identityVerificationRequest.getSlotId()), Mockito.any(IdentityVerificationResult.class));
    }

    @Test
    public void processFrames_invalidVerifierId_thenFail() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        identityVerificationRequest.setSlotId("test");
        identityVerificationRequest.setStepCode("stepCode");
        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setVerifierId("verifier-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId())).thenReturn(identityVerificationTransaction);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(null);
        webSocketHandler.processFrames(identityVerificationRequest);
        Mockito.verify(cacheUtilService, Mockito.times(1)).getVerifiedSlotTransaction(identityVerificationRequest.getSlotId());
        Mockito.verify(identityVerifierFactory, Mockito.times(1)).getIdentityVerifier("verifier-id");
        Mockito.verify(simpMessagingTemplate, Mockito.times(1))
                .convertAndSend(Mockito.eq("/topic/" + identityVerificationRequest.getSlotId()), Mockito.any(IdentityVerificationResult.class));
    }

    @Test
    public void processVerificationResult_withValidInput_thenPass() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");

        IdentityVerificationTransaction identityVerificationTransaction = new IdentityVerificationTransaction();
        identityVerificationTransaction.setVerifierId("verifier-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(identityVerificationTransaction);
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        webSocketHandler.processVerificationResult(identityVerificationResult);
    }

    @Test
    public void processVerificationResult_OnEndStepWithNoVerifiedClaims_thenPass() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setVerifierId("verifier-id");
        transaction.setApplicationId("application-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(transaction);
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        VerificationResult verificationResult = new VerificationResult();
        verificationResult.setStatus(VerificationStatus.COMPLETED);
        Mockito.when(identityVerifierPlugin.getVerificationResult(identityVerificationResult.getId())).thenReturn(verificationResult);

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(0)).updateProfile(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(VerificationStatus.COMPLETED, transaction.getStatus());
    }

    @Test
    public void processVerificationResult_OnEndStepWithVerifiedClaims_thenPass() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setVerifierId("verifier-id");
        transaction.setApplicationId("application-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(transaction);
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        VerificationResult verificationResult = new VerificationResult();
        verificationResult.setStatus(VerificationStatus.COMPLETED);
        verificationResult.setVerifiedClaims(new HashMap<>());
        verificationResult.getVerifiedClaims().put("name", objectMapper.createObjectNode());
        Mockito.when(identityVerifierPlugin.getVerificationResult(identityVerificationResult.getId())).thenReturn(verificationResult);

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(1)).updateProfile(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(VerificationStatus.UPDATE_PENDING, transaction.getStatus());
    }

    @Test
    public void processVerificationResult_OnEndStepWithVerifiedClaimsAndFailedProfileUpdate_thenFail() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setVerifierId("verifier-id");
        transaction.setApplicationId("application-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(transaction);
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        VerificationResult verificationResult = new VerificationResult();
        verificationResult.setStatus(VerificationStatus.COMPLETED);
        verificationResult.setVerifiedClaims(new HashMap<>());
        verificationResult.getVerifiedClaims().put("name", objectMapper.createObjectNode());
        Mockito.when(identityVerifierPlugin.getVerificationResult(identityVerificationResult.getId())).thenReturn(verificationResult);

        Mockito.when(profileRegistryPlugin.updateProfile(Mockito.anyString(), Mockito.any())).thenThrow(new ProfileException("update_failed"));

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(1)).updateProfile(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(VerificationStatus.FAILED, transaction.getStatus());
        Assert.assertEquals("update_failed", transaction.getErrorCode());
    }

    @Test
    public void processVerificationResult_OnEndStepAndFailedVerification_thenFail() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setVerifierId("verifier-id");
        transaction.setApplicationId("application-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(transaction);
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);

        VerificationResult verificationResult = new VerificationResult();
        verificationResult.setStatus(VerificationStatus.FAILED);
        verificationResult.setErrorCode("verification_failed");
        Mockito.when(identityVerifierPlugin.getVerificationResult(identityVerificationResult.getId())).thenReturn(verificationResult);

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(0)).updateProfile(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(VerificationStatus.FAILED, transaction.getStatus());
        Assert.assertEquals("verification_failed", transaction.getErrorCode());

        //Set to invalid end status
        verificationResult.setStatus(VerificationStatus.STARTED);
        verificationResult.setErrorCode(null);
        Mockito.when(identityVerifierPlugin.getVerificationResult(identityVerificationResult.getId())).thenReturn(verificationResult);

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(0)).updateProfile(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(VerificationStatus.FAILED, transaction.getStatus());
        Assert.assertEquals(IDENTITY_VERIFICATION_FAILED, transaction.getErrorCode());
    }

    @Test
    public void processVerificationResult_OnExceptionFromGetVerificationResult_thenFail() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setVerifierId("verifier-id");
        transaction.setApplicationId("application-id");
        transaction.setSlotId("slotId");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(transaction);
        IdentityVerifierPlugin identityVerifierPlugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(identityVerifierPlugin);
        Mockito.when(identityVerifierPlugin.getVerificationResult(identityVerificationResult.getId())).thenThrow(new IdentityVerifierException("verification_failed"));

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(0)).updateProfile(Mockito.anyString(), Mockito.any());
        Mockito.verify(auditHelper, Mockito.times(1))
                .sendAuditTransaction(AuditEvent.PROCESS_FRAMES, AuditEventType.ERROR, "slotId", null);
        Assert.assertEquals(VerificationStatus.FAILED, transaction.getStatus());
        Assert.assertEquals("verification_failed", transaction.getErrorCode());
    }

    @Test
    public void processVerificationResult_withInvalidTransaction_thenDoNothing() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(null);

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(0)).updateProfile(Mockito.anyString(), Mockito.any());
        Mockito.verify(identityVerifierFactory, Mockito.times(0)).getIdentityVerifier(Mockito.anyString());
    }

    @Test
    public void processVerificationResult_withInvalidVerifierId_thenDoNothing() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId("test");
        identityVerificationResult.setVerifierId("verifier-id");
        identityVerificationResult.setStep(new IDVProcessStepDetail());
        identityVerificationResult.getStep().setCode("END");

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setVerifierId("verifier-id");
        transaction.setApplicationId("application-id");
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId())).thenReturn(transaction);
        Mockito.when(identityVerifierFactory.getIdentityVerifier("verifier-id")).thenReturn(null);

        webSocketHandler.processVerificationResult(identityVerificationResult);
        Mockito.verify(profileRegistryPlugin, Mockito.times(0)).updateProfile(Mockito.anyString(), Mockito.any());
        Mockito.verify(identityVerifierFactory, Mockito.times(1)).getIdentityVerifier(Mockito.anyString());
    }

    @Test
    public void testValidate_withValidRequest_thenPass() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("validStepCode");
        request.setFrames(new ArrayList<>());
        assertDoesNotThrow(() -> webSocketHandler.processFrames(request));
    }

    @Test
    public void testValidate_withEmptySlotId_thenFail() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("");
        request.setStepCode("START");
        try{
            webSocketHandler.processFrames(request);
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_SLOT_ID);
        }
    }

    @Test
    public void testValidate_withNullSlotId_thenFail() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId(null);
        request.setStepCode("START");
        try{
            webSocketHandler.processFrames(request);
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_SLOT_ID);
        }
    }

    @Test
    public void testValidate_withNullStepCode_thenFail() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode(null);

        try{
            webSocketHandler.processFrames(request);
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(), ErrorConstants.INVALID_STEP_CODE);
        }
    }

    @Test
    public void testValidate_withBlankStepCode_thenFail() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("  ");
        try{
            webSocketHandler.processFrames(request);
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_STEP_CODE);
        }
    }

    @Test
    public void testValidate_WithInvalidFrameContent_thenFail() {
        FrameDetail frameDetail = new FrameDetail();
        frameDetail.setFrame("  ");
        frameDetail.setOrder(1);

        List<FrameDetail> frames = new ArrayList<>();
        frames.add(frameDetail);

        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("validStepCode");
        request.setFrames(frames);
        try{
            webSocketHandler.processFrames(request);
        }catch (SignUpException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_FRAME);
        }
    }

    @Test
    public void testValidate_WithInvalidOrder_thenFail() {
        FrameDetail frameDetail = new FrameDetail();
        frameDetail.setFrame("turn left");
        frameDetail.setOrder(-1);

        List<FrameDetail> frames = new ArrayList<>();
        frames.add(frameDetail);

        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("validStepCode");
        request.setFrames(frames);
        try {
            webSocketHandler.processFrames(request);
        } catch (SignUpException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorConstants.INVALID_ORDER);
        }
    }

}
