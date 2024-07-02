package io.mosip.signup.api.spi;

import io.mosip.signup.api.dto.IdentityVerificationDto;
import io.mosip.signup.api.dto.IdentityVerificationResult;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.util.ProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
public abstract class IdentityVerifierPlugin {

    public static final String RESULT_TOPIC = "ANALYZE_FRAMES_RESULT";

    @Autowired
    public KafkaTemplate<String, IdentityVerificationResult> kafkaTemplate;

    protected void publishAnalysisResult(IdentityVerificationResult identityVerificationResult) {
        kafkaTemplate.send(RESULT_TOPIC, identityVerificationResult);
    }

    /**
     *
     * @return
     */
    public abstract String getVerifierId();

    /**
     *
     * @return
     */
    public abstract List<ProcessType> getSupportedProcessTypes();

    /**
     *
     * @param transactionId
     */
    public abstract void verify(String transactionId, IdentityVerificationDto identityVerificationDto) throws IdentityVerifierException;

    /**
     *
     * @param transactionId
     */
    public abstract void getVerifiedResult(String transactionId) throws IdentityVerifierException;
}
