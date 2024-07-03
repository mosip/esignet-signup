package io.mosip.signup.services;

import io.mosip.signup.api.dto.*;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.api.util.ProcessFeedbackType;
import io.mosip.signup.api.util.ProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.mosip.signup.api.util.ProcessType.VIDEO;

@Slf4j
@Component
public class MockIdentityVerifierPlugin extends IdentityVerifierPlugin {

    @Override
    public String getVerifierId() {
        return "mock-identity-verifier";
    }

    @Override
    public List<ProcessType> getSupportedProcessTypes() {
        return List.of(VIDEO);
    }

    @Override
    public void verify(String transactionId, IdentityVerificationDto identityVerificationDto) throws IdentityVerifierException {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();
        identityVerificationResult.setId(transactionId);

        if(isStartStep(identityVerificationDto.getStepCode())) {
            IDVProcessStepDetail stepDetail = new IDVProcessStepDetail();
            stepDetail.setCode("liveness_check");
            stepDetail.setFramesPerSecond(3);
            stepDetail.setDurationInSeconds(60);
            stepDetail.setStartupDelayInSeconds(3);
            stepDetail.setRetryOnTimeout(true);
            identityVerificationResult.setStep(stepDetail);
        }
        else {
            IDVProcessFeedback processFeedback = new IDVProcessFeedback();
            processFeedback.setCode("turn_left");
            processFeedback.setType(ProcessFeedbackType.MESSAGE);
        }
        publishAnalysisResult(identityVerificationResult);
    }

    @Override
    public VerificationResult getVerifiedResult(String transactionId) throws IdentityVerifierException {
        return null;
    }

}
