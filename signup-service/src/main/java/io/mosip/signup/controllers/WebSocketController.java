/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

import io.mosip.signup.api.dto.*;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.services.CacheUtilService;
import io.mosip.signup.services.WebSocketHandler;
import io.mosip.signup.util.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.validation.Valid;
import java.util.Objects;

import static io.mosip.signup.util.SignUpConstants.VALUE_SEPARATOR;

@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private CacheUtilService cacheUtilService;


    @MessageMapping("/process-frame")
    public void processFrames(final @Payload IdentityVerificationRequest identityVerificationRequest) {
        log.debug("Process frame invoked with payload : {}", identityVerificationRequest);
        if(identityVerificationRequest == null || StringUtils.isEmpty(identityVerificationRequest.getSlotId()))
            throw new IdentityVerifierException(ErrorConstants.INVALID_SLOT_ID);

        if(StringUtils.isEmpty(identityVerificationRequest.getStepCode()))
            throw new IdentityVerifierException(ErrorConstants.INVALID_STEP_CODE);

        webSocketHandler.processFrames(identityVerificationRequest);
    }

    @KafkaListener(id = "step-status-consumer", autoStartup = "true",
            topics = IdentityVerifierPlugin.RESULT_TOPIC)
    public void consumeStepResult(final IdentityVerificationResult identityVerificationResult) {
        webSocketHandler.processVerificationResult(identityVerificationResult);
    }


    @EventListener
    public void onConnected(SessionConnectedEvent connectedEvent) {
        final String username = Objects.requireNonNull(connectedEvent.getUser()).getName();
        log.info("WebSocket Connected >>>>>> {}", username);
        webSocketHandler.updateProcessDuration(username);
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent disconnectEvent) {
        String username = Objects.requireNonNull(disconnectEvent.getUser()).getName();
        String transactionId = username.split(VALUE_SEPARATOR)[0];
        String slotId = username.split(VALUE_SEPARATOR)[1];

        log.info("WebSocket Disconnected >>>>>> {}", username);
        log.info("WebSocket Disconnected Status>>>>>> {}", disconnectEvent.getCloseStatus());
        if(!CloseStatus.NORMAL.equals(disconnectEvent.getCloseStatus())){
            IdentityVerificationTransaction transaction =
                    cacheUtilService.getVerifiedSlotTransaction(slotId);
            transaction.setStatus(VerificationStatus.FAILED);
            cacheUtilService.updateVerifiedSlotTransaction(slotId, transaction);
        }
        cacheUtilService.removeFromSlotConnected(username);
        cacheUtilService.evictSlotAllottedTransaction(transactionId,slotId);
    }
}
