/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.ws;


import io.mosip.signup.api.dto.*;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.api.util.ProcessFeedbackType;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.CacheUtilService;
import io.mosip.signup.services.IdentityVerifierFactory;
import io.mosip.signup.util.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;
import static io.mosip.signup.util.SignUpConstants.VALUE_SEPARATOR;

@Slf4j
@Service
public class WebSocketHandler {

    @Value("${mosip.signup.slot.expire-in-seconds}")
    private Integer slotExpireInSeconds;

    @Autowired
    CacheUtilService cacheUtilService;

    @Autowired
    private IdentityVerifierFactory identityVerifierFactory;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    public void processFrames(IdentityVerificationRequest identityVerificationRequest) {
        String errorCode = null;
        try {
            validate(identityVerificationRequest);
            IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId());
            if(transaction == null) {
                log.error("Ignoring identity verification request received for unknown/expired transaction!");
                throw new InvalidTransactionException();
            }

            IdentityVerifierPlugin plugin = identityVerifierFactory.getIdentityVerifier(transaction.getVerifierId());
            if(plugin == null) {
                log.error("Ignoring identity verification request received for unknown {} IDV plugin!", identityVerificationRequest.getSlotId());
                throw new SignUpException(PLUGIN_NOT_FOUND);
            }

            if(plugin.isStartStep(identityVerificationRequest.getStepCode())) {
                IdentityVerificationInitDto identityVerificationInitDto = new IdentityVerificationInitDto();
                identityVerificationInitDto.setIndividualId(transaction.getIndividualId());
                identityVerificationInitDto.setDisabilityType(transaction.getDisabilityType());
                plugin.initialize(identityVerificationRequest.getSlotId(), identityVerificationInitDto);
            }

            IdentityVerificationDto dto = new IdentityVerificationDto();
            dto.setStepCode(identityVerificationRequest.getStepCode());
            dto.setFrames(identityVerificationRequest.getFrames());
            plugin.verify(identityVerificationRequest.getSlotId(), dto);
        } catch (SignUpException e) {
            errorCode = e.getErrorCode();
            log.error("An error occurred while processing frames", e);
        } finally {
            if (errorCode != null) {
                sendErrorFeedback(identityVerificationRequest.getSlotId(), errorCode);
            }
        }
    }

    public void processVerificationResult(IdentityVerificationResult identityVerificationResult) {
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

        log.info("Analysis result published to /topic/{}", identityVerificationResult.getId());
        simpMessagingTemplate.convertAndSend("/topic/"+identityVerificationResult.getId(), identityVerificationResult);

        //END step marks verification process completion
        if(identityVerificationResult.getStep() != null && plugin.isEndStep(identityVerificationResult.getStep().getCode())) {
            log.info("Reached the end step for slot {} with current status: {}", identityVerificationResult.getId(),
                    transaction.getStatus());
            transaction.setStatus(VerificationStatus.RESULTS_READY);
            cacheUtilService.updateVerifiedSlotTransaction(identityVerificationResult.getId(), transaction);
        }
    }

    public void updateProcessDuration(String username) {
        String[] parts = username.split(VALUE_SEPARATOR);

        if(parts.length <= 1) {
            log.error("WebSocket Connected request received with invalid transaction details >>>>>> {}", username);
            return;
        }

        IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(parts[1]);
        if(transaction == null) {
            log.error("WebSocket Connected request received with invalid transaction details >>>>>> {}", username);
            return;
        }

        IdentityVerifierDetail[] verifierDetails = cacheUtilService.getIdentityVerifierDetails();
        Optional<IdentityVerifierDetail> result = Arrays.stream(verifierDetails)
                .filter(idv -> idv.isActive() && idv.getId().equals(transaction.getVerifierId()))
                .findFirst();

        result.ifPresent(identityVerifierDetail -> cacheUtilService.updateSlotExpireTime(username, getVerificationProcessExpireTimeInMillis(identityVerifierDetail)));
    }

    private long getVerificationProcessExpireTimeInMillis(IdentityVerifierDetail identityVerifierDetail) {
        int processDurationInSeconds = identityVerifierDetail.getProcessDuration() <= 0 ? slotExpireInSeconds : identityVerifierDetail.getProcessDuration();
        return System.currentTimeMillis() + ( processDurationInSeconds * 1000L );
    }

    private void sendErrorFeedback(String slotId, String errorCode) {
        log.error("Publishing error feedback : {}, with error : {}", slotId, errorCode);
        IDVProcessFeedback idvProcessFeedback = new IDVProcessFeedback();
        idvProcessFeedback.setType(ProcessFeedbackType.ERROR);
        idvProcessFeedback.setCode(errorCode);

        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setFeedback(idvProcessFeedback);

        simpMessagingTemplate.convertAndSend("/topic/" + slotId, identityVerificationResult);
    }

    private void validate(IdentityVerificationRequest request) {
        if(request == null || StringUtils.isEmpty(request.getSlotId()))
            throw new SignUpException(ErrorConstants.INVALID_SLOT_ID);
        if (request.getStepCode() == null || request.getStepCode().isBlank()) {
            throw new SignUpException(ErrorConstants.INVALID_STEP_CODE);
        }
        List<FrameDetail> frames = request.getFrames();
        if (frames != null && !frames.isEmpty()) {
            for (FrameDetail frame : frames) {
                if (frame.getFrame() == null || frame.getFrame().isBlank()) {
                    throw new SignUpException(ErrorConstants.INVALID_FRAME);
                }
                if (frame.getOrder() < 0) {
                    throw new SignUpException(ErrorConstants.INVALID_ORDER);
                }
            }
        }
    }
}
