package io.mosip.signup.dto;

import lombok.Data;

import java.util.List;

@Data
public class InitiateIdentityVerificationResponse {

    IdentityVerifierDetail[] identityVerifiers;
}
