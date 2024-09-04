/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.api.spi;

import io.mosip.signup.api.dto.IdentityVerificationDto;
import io.mosip.signup.api.dto.IdentityVerificationResult;
import io.mosip.signup.api.dto.VerifiedResult;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.util.ProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
public abstract class IdentityVerifierPlugin {

    private static final String START_STEP = "START";
    private static final String END_STEP = "END";
    public static final String RESULT_TOPIC = "ANALYZE_FRAMES_RESULT";

    @Autowired
    public KafkaTemplate<String, IdentityVerificationResult> kafkaTemplate;

    protected void publishAnalysisResult(IdentityVerificationResult identityVerificationResult) {
        kafkaTemplate.send(RESULT_TOPIC, identityVerificationResult);
    }

    public final boolean isStartStep(String stepCode) {
        return START_STEP.equalsIgnoreCase(stepCode);
    }

    public final boolean isEndStep(String stepCode) {
        return END_STEP.equalsIgnoreCase(stepCode);
    }

    /**
     * Get unique identity verifier ID
     * @return
     */
    public abstract String getVerifierId();

    /**
     * Get the list of supported process types
     * @return
     */
    public abstract List<ProcessType> getSupportedProcessTypes();

    /**
     * Verify the input frames based on the provided step code. TransactionId should be used to maintain the state of the
     * verification process.
     * It is expected to send the first step detail when the stepCode is passed as "START".
     * Once all the steps are completed in the process, this method to return stepCode as "END" to mark the completion of
     * identity verification process.
     * @param transactionId
     * @param identityVerificationDto
     * @throws IdentityVerifierException
     */
    public abstract void verify(String transactionId, IdentityVerificationDto identityVerificationDto) throws IdentityVerifierException;

    /**
     * Once the "END" stepCode is reached in the verify method, getVerifiedResult method is invoked to fetch the final
     * result if the identity verification process.
     * @param transactionId
     * @return
     * @throws IdentityVerifierException
     */
    public abstract VerifiedResult getVerifiedResult(String transactionId) throws IdentityVerifierException;
}
