package io.mosip.signup.util;

import io.mosip.signup.services.CacheUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private CacheUtilService cacheUtilService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if(isSlotSubscriptionMessage(accessor)) {
            String destination = accessor.getDestination();
            String slotId = destination.replace("/topic/", "");
            if(cacheUtilService.getVerifiedSlotTransaction(slotId) == null) {
                throw new MessageDeliveryException(ErrorConstants.INVALID_SLOT_ID);
            }
        }
        return message;
    }

    private boolean isSlotSubscriptionMessage(StompHeaderAccessor accessor) {
        return accessor != null
                && StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                && accessor.getDestination() != null
                && accessor.getDestination().startsWith("/topic/");
    }
}