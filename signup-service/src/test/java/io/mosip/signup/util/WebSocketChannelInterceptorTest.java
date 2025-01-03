package io.mosip.signup.util;

import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.exception.SignUpException;
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
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
public class WebSocketChannelInterceptorTest {

    @InjectMocks
    private WebSocketChannelInterceptor channelInterceptor;

    @Mock
    private CacheUtilService cacheUtilService;

    @Test
    public void websocketSubscribe_withValidSlotId_thenPass() {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination("/topic/slotid");
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
    public void websocketSubscribe_withInvalidSlotId_thenThrow() {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination("/topic/slotid");
        Message<?> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        Mockito.when(cacheUtilService.getVerifiedSlotTransaction(Mockito.anyString())).thenReturn(null);

        Assertions.assertThrows(SignUpException.class, () -> channelInterceptor.preSend(message, new MessageChannel() {
            @Override
            public boolean send(Message<?> message, long l) {
                return false;
            }
        }));
    }
}
