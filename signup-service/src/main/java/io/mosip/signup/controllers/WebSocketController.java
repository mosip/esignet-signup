package io.mosip.signup.controllers;

import io.mosip.signup.dto.IDVProcessFeedback;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationResponse;
import io.mosip.signup.services.CacheUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Slf4j
@Controller()
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    CacheUtilService cacheUtilService;

    @KafkaListener(id = "step-status-consumer", autoStartup = "true",
            topics = "ANALYZE_FRAMES_RESULT")
    public void consumeStepStatus(final IdentityVerificationResponse response) {
        simpMessagingTemplate.convertAndSend("/topic/"+response.getSlotId(), response);
    }

    @MessageMapping("/process-frame")
    public void processFrames(final @Payload IdentityVerificationRequest identityVerificationRequest) {
        String slot = identityVerificationRequest.getSlotId();
        log.info("Message received from Client >>>>> {}", slot);

        IdentityVerificationResponse identityVerificationResponse = new IdentityVerificationResponse();
        identityVerificationResponse.setSlotId(slot);

        IDVProcessFeedback idvProcessFeedback = new IDVProcessFeedback();
        idvProcessFeedback.setCode("turn_left");
        idvProcessFeedback.setType("MESSAGE");
        identityVerificationResponse.setFeedback(idvProcessFeedback);
        simpMessagingTemplate.convertAndSend("/topic/"+slot, identityVerificationResponse);
    }

    @EventListener
    public void onConnected(SessionConnectedEvent connectedEvent) {
        log.info("WebSocket Connected >>>>>> {}", Objects.requireNonNull(connectedEvent.getUser()).getName());
        //TODO ??
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent disconnectEvent) {
        String sessionId = Objects.requireNonNull(disconnectEvent.getUser()).getName();
        log.info("WebSocket Disconnected >>>>>> {}", sessionId);
        cacheUtilService.decrementCurrentSlotCount();
        cacheUtilService.evictSlotAllottedTransaction(sessionId.split("##")[1]);
    }
}
