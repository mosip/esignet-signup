package io.mosip.signup.dto;

import lombok.Data;

@Data
public class SlotRequest {

    private String verifierId;
    private String consent;
    private String disabilityType;

}
