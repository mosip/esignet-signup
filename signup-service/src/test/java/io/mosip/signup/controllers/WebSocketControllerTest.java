package io.mosip.signup.controllers;

import io.mosip.signup.api.dto.IdentityVerificationResult;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.services.CacheUtilService;
import io.mosip.signup.services.WebSocketHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static io.mosip.signup.util.ErrorConstants.INVALID_SLOT_ID;
import static io.mosip.signup.util.ErrorConstants.INVALID_STEP_CODE;
import static io.mosip.signup.util.SignUpConstants.VALUE_SEPARATOR;


@RunWith(SpringRunner.class)
@WebMvcTest(value = WebSocketController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class WebSocketControllerTest {

    @InjectMocks
    private WebSocketController webSocketController;

    @MockBean
    private CacheUtilService cacheUtilService;

    @MockBean
    private WebSocketHandler webSocketHandler;

    @MockBean
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(webSocketController, "webSocketHandler", webSocketHandler);
        ReflectionTestUtils.setField(webSocketController, "cacheUtilService", cacheUtilService);
    }

    @Test
    public void processFrames_withInvalidSlotId_thenFail() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        try {
            webSocketController.processFrames(identityVerificationRequest);
            Assert.fail();
        } catch (IdentityVerifierException e) {
            Assert.assertEquals(INVALID_SLOT_ID, e.getErrorCode());
        }
    }

    @Test
    public void processFrames_withInvalidStepCode_thenFail() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        identityVerificationRequest.setSlotId("slot-id");
        try {
            webSocketController.processFrames(identityVerificationRequest);
            Assert.fail();
        } catch (IdentityVerifierException e) {
            Assert.assertEquals(INVALID_STEP_CODE, e.getErrorCode());
        }
    }

    @Test
    public void processFrames_withValidInput_thenPass() {
        IdentityVerificationRequest identityVerificationRequest = new IdentityVerificationRequest();
        identityVerificationRequest.setStepCode("START");
        identityVerificationRequest.setSlotId("slot-id");
        webSocketController.processFrames(identityVerificationRequest);
    }

    @Test
    public void consumeStepResult_test() {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        webSocketController.consumeStepResult(identityVerificationResult);
    }

    @Test
    public void onConnected_test() {
        SessionConnectedEvent sessionConnectedEvent = Mockito.mock(SessionConnectedEvent.class);
        Mockito.when(sessionConnectedEvent.getUser()).thenReturn(new  java.security.Principal() {
            @Override
            public String getName() {
                return "";
            }
        });
        webSocketController.onConnected(sessionConnectedEvent);
        Mockito.verify(webSocketHandler, Mockito.times(1)).updateProcessDuration("");
    }

    @Test
    public void onDisconnected_test() {
        SessionDisconnectEvent sessionDisconnectEvent =  Mockito.mock(SessionDisconnectEvent.class);
        Mockito.when(sessionDisconnectEvent.getUser()).thenReturn(new  java.security.Principal() {
            @Override
            public String getName() {
                return "TID"+VALUE_SEPARATOR+"SID";
            }
        });
        webSocketController.onDisconnected(sessionDisconnectEvent);
        Mockito.verify(cacheUtilService, Mockito.times(1)).removeFromSlotConnected(Mockito.anyString());
        Mockito.verify(cacheUtilService, Mockito.times(1)).evictSlotAllottedTransaction(Mockito.anyString(),Mockito.anyString());
    }

    @Test
    public void onDisconnectedAbnormalClosedState_test() {
        SessionDisconnectEvent sessionDisconnectEvent =  Mockito.mock(SessionDisconnectEvent.class);
        Mockito.when(sessionDisconnectEvent.getUser()).thenReturn(new  java.security.Principal() {
            @Override
            public String getName() {
                return "TID"+VALUE_SEPARATOR+"SID";
            }
        });
        Mockito.when(sessionDisconnectEvent.getCloseStatus()).thenReturn(CloseStatus.SERVER_ERROR);

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setStatus(VerificationStatus.FAILED);
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(transaction);
        webSocketController.onDisconnected(sessionDisconnectEvent);

        Mockito.verify(cacheUtilService, Mockito.times(1)).updateVerifiedSlotTransaction(Mockito.anyString(), Mockito.any());
        Mockito.verify(cacheUtilService, Mockito.times(1)).removeFromSlotConnected(Mockito.anyString());
        Mockito.verify(cacheUtilService, Mockito.times(1)).evictSlotAllottedTransaction(Mockito.anyString(),Mockito.anyString());
    }
}
