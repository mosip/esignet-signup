/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.ws;

import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.services.CacheUtilService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
public class WebSocketChannelInterceptorTest {

    @InjectMocks
    private WebSocketChannelInterceptor channelInterceptor;

    @Mock
    private CacheUtilService cacheUtilService;

    final static private String SLOT_SUBSCRIPTION_DESTINATION_PREFIX = "/topic/";

    @Test
    public void websocketSubscribe_withValidSlotId_thenPass() {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination(SLOT_SUBSCRIPTION_DESTINATION_PREFIX + "slotid");
        Message<?> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(new IdentityVerificationTransaction());

        Assertions.assertDoesNotThrow(() -> channelInterceptor.preSend(message, new MessageChannel() {
            @Override
            public boolean send(Message<?> message, long l) {
                return false;
            }
        }));
    }

    @Test
    public void websocketSubscribe_withInvalidSlotId_throwException() {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination(SLOT_SUBSCRIPTION_DESTINATION_PREFIX + "slotid");
        Message<?> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(null);

        Assertions.assertThrows(MessageDeliveryException.class, () -> channelInterceptor.preSend(message, new MessageChannel() {
            @Override
            public boolean send(Message<?> message, long l) {
                return false;
            }
        }));
    }
}
