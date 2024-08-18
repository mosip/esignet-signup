package io.mosip.signup.api.dto;


import lombok.Data;

import java.util.List;

@Data
public class IdentityVerificationDto {

    private String stepCode;
    private String disabilityType;
    private List<FrameDetail> frames;
}
