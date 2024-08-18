package io.mosip.signup.services;

import io.mosip.signup.dto.IdentityVerificationTransaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;

import java.security.Principal;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class IdentityVerificationHandshakeHandlerTest {

    @Mock
    private CacheUtilService cacheUtilService;

    @InjectMocks
    private IdentityVerificationHandshakeHandler handler;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private WebSocketHandler wsHandler;

    @Test
    public void determineUser_withValidDetails_thenPass() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "IDV_SLOT_ALLOTTED=12345");
        headers.add("SlotId", "slot123");

        Mockito.when(request.getHeaders()).thenReturn(headers);

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setSlotId("slot123");
        Mockito.when(cacheUtilService.getSlotAllottedTransaction("12345")).thenReturn(transaction);

        Principal principal = handler.determineUser(request, wsHandler, new HashMap<>());

        Assert.assertNotNull(principal);
        Assert.assertEquals("12345##slot123", principal.getName());
    }

    @Test
    public void determineUser_withNoTransactionCookie_thenFail() {
        HttpHeaders headers = new HttpHeaders();

        Mockito.when(request.getHeaders()).thenReturn(headers);

        Assert.assertThrows(HandshakeFailureException.class, () ->
                handler.determineUser(request, wsHandler, new HashMap<>())
        );
    }

    @Test
    public void determineUser_withInvalidTransactionId_thenFail() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "IDV_SLOT_ALLOTTED=invalid");

        Mockito.when(request.getHeaders()).thenReturn(headers);
        Mockito.when(cacheUtilService.getSlotAllottedTransaction("invalid")).thenReturn(null);

        Assert.assertThrows(HandshakeFailureException.class, () ->
                handler.determineUser(request, wsHandler, new HashMap<>())
        );
    }

    @Test
    public void determineUser_withInvalidSlotId_ThenFail() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "IDV_SLOT_ALLOTTED=12345");
        headers.add("SlotId", "wrongSlot");

        Mockito.when(request.getHeaders()).thenReturn(headers);

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setSlotId("correctSlot");
        Mockito.when(cacheUtilService.getSlotAllottedTransaction("12345")).thenReturn(transaction);

        Assert.assertThrows(HandshakeFailureException.class, () ->
                handler.determineUser(request, wsHandler, new HashMap<>())
        );
    }


    @Test
    public void determineUser_withMissingSlotIdHeader_thenFail() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "IDV_SLOT_ALLOTTED=12345");

        Mockito.when(request.getHeaders()).thenReturn(headers);

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setSlotId("slot123");
        Mockito.when(cacheUtilService.getSlotAllottedTransaction("12345")).thenReturn(transaction);

        Assert.assertThrows(HandshakeFailureException.class, () ->
                handler.determineUser(request, wsHandler, new HashMap<>())
        );
    }

}
