package io.mosip.signup.api.dto;


import lombok.Data;

import java.util.List;

@Data
public class IdentityVerificationDto {

    private String stepCode;
    private List<FrameDetail> frames;
}
