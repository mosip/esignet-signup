package io.mosip.signup.api.impl;

import io.mosip.signup.api.dto.IdentityVerificationDto;
import io.mosip.signup.api.dto.IdentityVerificationInitDto;
import io.mosip.signup.api.dto.VerificationResult;
import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import io.mosip.signup.api.util.ProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NoOpIdentityVerifierPluginImpl extends IdentityVerifierPlugin {

    @Override
    public String getVerifierId() {
        return "NoOpVerifier";
    }

    @Override
    public List<ProcessType> getSupportedProcessTypes() {
        return List.of();
    }

    @Override
    public void initialize(String transactionId, IdentityVerificationInitDto identityVerificationInitDto) {

    }

    @Override
    public void verify(String transactionId, IdentityVerificationDto identityVerificationDto) throws IdentityVerifierException {

    }

    @Override
    public VerificationResult getVerificationResult(String transactionId) throws IdentityVerifierException {
        return null;
    }
}
