package io.mosip.signup.api.dto;


import lombok.Data;

@Data
public class IdentityVerificationInitDto {

    private String individualId;
    private String disabilityType;
}
