/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;


import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.util.ErrorConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RunWith(SpringRunner.class)
public class IdentityVerificationHandshakeHandlerTest {



    @InjectMocks
    private IdentityVerificationHandshakeHandler identityVerificationHandshakeHandler;

    @Mock
    private CacheUtilService cacheUtilService;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = Collections.emptyMap();
    }

    @Test
    public void determineUserWithValidTransaction_withValidDetails_thenPass() throws Exception {


        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setSlotId("123");

        Mockito.when(cacheUtilService.getSlotAllottedTransaction(Mockito.anyString())).thenReturn(transaction);

        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        HttpHeaders headers=new HttpHeaders();
        headers.set("Cookie","IDV_SLOT_ALLOTTED=Slot###123");
        Mockito.when(request.getHeaders()).thenReturn(headers);
        Mockito.when(request.getURI()).thenReturn(new URI("http://localhost?slotId=123"));
        Principal principal = identityVerificationHandshakeHandler.determineUser(request, Mockito.mock(WebSocketHandler.class), attributes);

        Assert.assertNotNull(principal);
        Assert.assertEquals("Slot###123", principal.getName());
    }

    @Test
    public void determineUserWithInvalidTransaction_thenFail() throws Exception {

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setSlotId("123");

        Mockito.when(cacheUtilService.getSlotAllottedTransaction(Mockito.anyString())).thenReturn(transaction);

        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        HttpHeaders headers=new HttpHeaders();
        headers.set("Cookie","IDV_SLOT_ALLOTTED");
        Mockito.when(request.getHeaders()).thenReturn(headers);
        Mockito.when(request.getURI()).thenReturn(new URI("http://localhost?slotId=123"));

        try{
            identityVerificationHandshakeHandler.determineUser(request, Mockito.mock(WebSocketHandler.class), attributes);
            Assert.fail("Expected HandshakeFailureException");
        }catch(HandshakeFailureException e) {
            Assert.assertEquals(ErrorConstants.INVALID_TRANSACTION, e.getMessage());
        }
    }

    @Test
    public void determineUserWithInValidSlotId_thenFail() throws Exception {

        IdentityVerificationTransaction transaction = new IdentityVerificationTransaction();
        transaction.setSlotId("123");
        Mockito.when(cacheUtilService.getSlotAllottedTransaction(Mockito.anyString())).thenReturn(transaction);

        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        HttpHeaders headers=new HttpHeaders();
        headers.set("Cookie","IDV_SLOT_ALLOTTED=Slot###1234");
        Mockito.when(request.getHeaders()).thenReturn(headers);
        Mockito.when(request.getURI()).thenReturn(new URI("http://localhost?slotId=123"));
        try{
            identityVerificationHandshakeHandler.determineUser(request, Mockito.mock(WebSocketHandler.class), attributes);
        }catch (HandshakeFailureException e){
            Assert.assertEquals(ErrorConstants.INVALID_TRANSACTION, e.getMessage());
        }
    }

}
