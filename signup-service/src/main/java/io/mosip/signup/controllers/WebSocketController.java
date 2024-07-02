package io.mosip.signup.controllers;

import io.mosip.signup.api.dto.IDVProcessFeedback;
import io.mosip.signup.api.dto.IdentityVerificationDto;
import io.mosip.signup.api.dto.IdentityVerificationResult;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.CacheUtilService;
import io.mosip.signup.services.IdentityVerifierFactory;
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

import javax.validation.Valid;
import java.util.Objects;

import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;
import static io.mosip.signup.util.SignUpConstants.SOCKET_USERNAME_SEPARATOR;

@Slf4j
@Controller()
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    CacheUtilService cacheUtilService;

    @Autowired
    private IdentityVerifierFactory identityVerifierFactory;


    @MessageMapping("/process-frame")
    public void processFrames(final @Valid @Payload IdentityVerificationRequest identityVerificationRequest) {
        log.info("Process frame invoked with payload : {}", identityVerificationRequest);
        IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId());
        if(transaction == null)
            throw new InvalidTransactionException();

        IdentityVerifierPlugin plugin = identityVerifierFactory.getIdentityVerifier(transaction.getVerifierId());
        if(plugin == null)
            throw new SignUpException(PLUGIN_NOT_FOUND);

        IdentityVerificationDto dto = new IdentityVerificationDto();
        dto.setStepCode(identityVerificationRequest.getStepCode());
        dto.setFrames(identityVerificationRequest.getFrames());
        plugin.verify(identityVerificationRequest.getSlotId(), dto);
    }

    @KafkaListener(id = "step-status-consumer", autoStartup = "true",
            topics = IdentityVerifierPlugin.RESULT_TOPIC)
    public void consumeStepStatus(final IdentityVerificationResult verificationResult) {
        simpMessagingTemplate.convertAndSend("/topic/"+verificationResult.getId(), verificationResult);
    }

    @EventListener
    public void onConnected(SessionConnectedEvent connectedEvent) {
        final String username = Objects.requireNonNull(connectedEvent.getUser()).getName();
        log.info("WebSocket Connected >>>>>> {}", Objects.requireNonNull(connectedEvent.getUser()).getName());
        cacheUtilService.addToVerifiedSlot(username);
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent disconnectEvent) {
        String username = Objects.requireNonNull(disconnectEvent.getUser()).getName();
        log.info("WebSocket Disconnected >>>>>> {}", username);
        cacheUtilService.removeFromVerifiedSlot(username);
        cacheUtilService.evictSlotAllottedTransaction(username.split(SOCKET_USERNAME_SEPARATOR)[0],
                username.split(SOCKET_USERNAME_SEPARATOR)[1]);
    }
}
