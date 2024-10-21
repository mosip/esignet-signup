/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.signup.api.dto.*;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.exception.ProfileException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import io.mosip.signup.api.util.VerificationStatus;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.exception.InvalidTransactionException;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.util.AuditEvent;
import io.mosip.signup.util.AuditEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.mosip.signup.api.util.ErrorConstants.IDENTITY_VERIFICATION_FAILED;
import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;
import static io.mosip.signup.util.ErrorConstants.VERIFIED_CLAIMS_FIELD_ID;
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
    private ProfileRegistryPlugin profileRegistryPlugin;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    AuditHelper auditHelper;


    public void processFrames(IdentityVerificationRequest identityVerificationRequest) {
        IdentityVerificationTransaction transaction = cacheUtilService.getVerifiedSlotTransaction(identityVerificationRequest.getSlotId());
        if(transaction == null)
            throw new InvalidTransactionException();

        IdentityVerifierPlugin plugin = identityVerifierFactory.getIdentityVerifier(transaction.getVerifierId());
        if(plugin == null)
            throw new SignUpException(PLUGIN_NOT_FOUND);

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

        simpMessagingTemplate.convertAndSend("/topic/"+identityVerificationResult.getId(), identityVerificationResult);

        //END step marks verification process completion
        if(identityVerificationResult.getStep() != null && plugin.isEndStep(identityVerificationResult.getStep().getCode())) {
            log.info("Reached the end step for {}", identityVerificationResult.getId());
            handleVerificationResult(plugin, identityVerificationResult, transaction);
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

        result.ifPresent(identityVerifierDetail -> cacheUtilService.addToSlotConnected(username, getVerificationProcessExpireTimeInMillis(identityVerifierDetail)));
    }

    private void handleVerificationResult(IdentityVerifierPlugin plugin, IdentityVerificationResult identityVerificationResult,
                                      IdentityVerificationTransaction transaction) {
        try {
            VerificationResult verificationResult = plugin.getVerificationResult(identityVerificationResult.getId());
            log.debug("Verification result >> {}", verificationResult);

            switch (verificationResult.getStatus()) {
                case COMPLETED: //Proceed to update the profile
                    if(CollectionUtils.isEmpty(verificationResult.getVerifiedClaims())) {
                        log.warn("**** Empty verified_claims was returned on successful verification process ****");
                        transaction.setStatus(VerificationStatus.COMPLETED);
                        break;
                    }

                    ProfileDto profileDto = new ProfileDto();
                    profileDto.setIndividualId(transaction.getIndividualId());
                    profileDto.setActive(true);
                    Map<String, Map<String, JsonNode>> verifiedData = new HashMap<>();
                    verifiedData.put(VERIFIED_CLAIMS_FIELD_ID, verificationResult.getVerifiedClaims());
                    profileDto.setIdentity(objectMapper.valueToTree(verifiedData));
                    profileRegistryPlugin.updateProfile(transaction.getApplicationId(), profileDto);
                    transaction.setStatus(VerificationStatus.UPDATE_PENDING);
                    break;
                default:
                    transaction.setStatus(VerificationStatus.FAILED);
                    transaction.setErrorCode(verificationResult.getErrorCode() == null ? IDENTITY_VERIFICATION_FAILED : verificationResult.getErrorCode());
                    break;
            }

        } catch (IdentityVerifierException | ProfileException e) {
            log.error("Failed to update profile", e);
            transaction.setStatus(VerificationStatus.FAILED);
            transaction.setErrorCode(e instanceof IdentityVerifierException ?
                    ((IdentityVerifierException) e).getErrorCode() :
                    ((ProfileException) e).getErrorCode());
            auditHelper.sendAuditTransaction(AuditEvent.PROCESS_FRAMES, AuditEventType.ERROR,transaction.getSlotId(), null);
        }
        cacheUtilService.updateVerifiedSlotTransaction(identityVerificationResult.getId(), transaction);
        cacheUtilService.updateVerificationStatus(transaction.getAccessTokenSubject(), transaction.getStatus().toString(),
                transaction.getErrorCode());
    }

    private long getVerificationProcessExpireTimeInMillis(IdentityVerifierDetail identityVerifierDetail) {
        int processDurationInSeconds = identityVerifierDetail.getProcessDuration() <= 0 ? slotExpireInSeconds : identityVerifierDetail.getProcessDuration();
        return System.currentTimeMillis() + ( processDurationInSeconds * 1000L );
    }
}
