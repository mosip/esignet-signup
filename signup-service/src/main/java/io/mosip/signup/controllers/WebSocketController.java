/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.mosip.signup.api.util.ErrorConstants.IDENTITY_VERIFICATION_FAILED;
import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;
import static io.mosip.signup.util.ErrorConstants.VERIFIED_CLAIMS_FIELD_ID;
import static io.mosip.signup.util.SignUpConstants.VALUE_SEPARATOR;

@Slf4j
@Controller()
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    CacheUtilService cacheUtilService;

    @Autowired
    private IdentityVerifierFactory identityVerifierFactory;

    @Autowired
    private ProfileRegistryPlugin profileRegistryPlugin;

    @Autowired
    private ObjectMapper objectMapper;


    @MessageMapping("/process-frame")
    public void processFrames(final @Valid @Payload IdentityVerificationRequest identityVerificationRequest) {
        log.debug("Process frame invoked with payload : {}", identityVerificationRequest);
        IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId());
        if(transaction == null)
            throw new InvalidTransactionException();

        IdentityVerifierPlugin plugin = identityVerifierFactory.getIdentityVerifier(transaction.getVerifierId());
        if(plugin == null)
            throw new SignUpException(PLUGIN_NOT_FOUND);

        IdentityVerificationDto dto = new IdentityVerificationDto();
        dto.setStepCode(identityVerificationRequest.getStepCode());
        dto.setFrames(identityVerificationRequest.getFrames());
        dto.setDisabilityType(transaction.getDisabilityType());
        plugin.verify(identityVerificationRequest.getSlotId(), dto);
    }

    @KafkaListener(id = "step-status-consumer", autoStartup = "true",
            topics = IdentityVerifierPlugin.RESULT_TOPIC)
    public void consumeStepStatus(final IdentityVerificationResult identityVerificationResult) {
        IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(identityVerificationResult.getId());
        if(transaction == null) {
            log.error("Ignoring identity verification result received for unknown/expired transaction!");
            return;
        }

        IdentityVerifierPlugin plugin = identityVerifierFactory.getIdentityVerifier(identityVerificationResult.getVerifierId());
        if(plugin == null) {
            log.error("Ignoring identity verification result received for unknown {} IDV plugin!", identityVerificationResult.getVerifierId());
            return;
        }

        simpMessagingTemplate.convertAndSend("/topic/"+identityVerificationResult.getId(), identityVerificationResult);

        //END step marks verification process completion
        if(identityVerificationResult.getStep() != null && plugin.isEndStep(identityVerificationResult.getStep().getCode())) {
            log.info("Reached the end step for {}", identityVerificationResult.getId());
            handleVerifiedResult(plugin, identityVerificationResult, transaction);
        }
    }

    private void handleVerifiedResult(IdentityVerifierPlugin plugin, IdentityVerificationResult identityVerificationResult,
                                      IdentityVerificationTransaction transaction) {
        try {
            VerifiedResult verifiedResult = plugin.getVerifiedResult(identityVerificationResult.getId());
            log.debug("VerifiedResult >> {}", verifiedResult);

            switch (verifiedResult.getStatus()) {
                case COMPLETED: //Proceed to update the profile
                    ProfileDto profileDto = new ProfileDto();
                    profileDto.setIndividualId(transaction.getIndividualId());
                    profileDto.setActive(true);
                    Map<String, Map<String, VerificationDetail>> verifiedData = new HashMap<>();
                    verifiedData.put(VERIFIED_CLAIMS_FIELD_ID, verifiedResult.getVerifiedClaims());
                    profileDto.setIdentity(objectMapper.valueToTree(verifiedData));

                    try {
                        profileRegistryPlugin.updateProfile(transaction.getApplicationId(), profileDto);
                        transaction.setStatus(VerificationStatus.UPDATE_PENDING);
                    } catch (ProfileException ex) {
                        log.error("Failed to updated verified claims in the registry", ex);
                       //TODO  transaction.setStatus(VerificationStatus.FAILED);
                       // transaction.setErrorCode(ex.getErrorCode());
                        transaction.setStatus(VerificationStatus.COMPLETED); //TEMP Fix
                    }
                    break;
                case FAILED:
                    transaction.setStatus(VerificationStatus.FAILED);
                    transaction.setErrorCode(verifiedResult.getErrorCode());
                    break;
                default:
                    transaction.setStatus(VerificationStatus.FAILED);
                    transaction.setErrorCode(IDENTITY_VERIFICATION_FAILED);
                    break;
            }

        } catch (IdentityVerifierException e) {
            log.error("Failed to fetch verified result from the plugin", e);
            transaction.setStatus(VerificationStatus.FAILED);
            transaction.setErrorCode(IDENTITY_VERIFICATION_FAILED);
        }
        cacheUtilService.updateVerifiedSlotTransaction(identityVerificationResult.getId(), transaction);
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
        cacheUtilService.evictSlotAllottedTransaction(username.split(VALUE_SEPARATOR)[0],
                username.split(VALUE_SEPARATOR)[1]);
    }
}
