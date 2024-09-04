package io.mosip.signup.services;


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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

import static io.mosip.signup.api.util.ErrorConstants.IDENTITY_VERIFICATION_FAILED;
import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;
import static io.mosip.signup.util.ErrorConstants.VERIFIED_CLAIMS_FIELD_ID;

@Slf4j
@Service
public class WebSocketHandler {

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
        else {
            IdentityVerificationDto dto = new IdentityVerificationDto();
            dto.setStepCode(identityVerificationRequest.getStepCode());
            dto.setFrames(identityVerificationRequest.getFrames());
            plugin.verify(identityVerificationRequest.getSlotId(), dto);
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

        simpMessagingTemplate.convertAndSend("/topic/"+identityVerificationResult.getId(), identityVerificationResult);

        //END step marks verification process completion
        if(identityVerificationResult.getStep() != null && plugin.isEndStep(identityVerificationResult.getStep().getCode())) {
            log.info("Reached the end step for {}", identityVerificationResult.getId());
            handleVerificationResult(plugin, identityVerificationResult, transaction);
        }
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
                    Map<String, Map<String, VerificationDetail>> verifiedData = new HashMap<>();
                    verifiedData.put(VERIFIED_CLAIMS_FIELD_ID, verificationResult.getVerifiedClaims());
                    profileDto.setIdentity(objectMapper.valueToTree(verifiedData));
                    try {
                        profileRegistryPlugin.updateProfile(transaction.getApplicationId(), profileDto);
                        transaction.setStatus(VerificationStatus.UPDATE_PENDING);
                    } catch (ProfileException ex) {
                        log.error("Failed to updated verified claims in the registry", ex);
                        transaction.setStatus(VerificationStatus.FAILED);
                        transaction.setErrorCode(ex.getErrorCode());
                    }
                    break;
                case FAILED:
                    transaction.setStatus(VerificationStatus.FAILED);
                    transaction.setErrorCode(verificationResult.getErrorCode());
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
}
