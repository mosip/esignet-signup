package io.mosip.signup.dto;

import lombok.Data;

@Data
public class AddIdentityResponse {

    private String status;
    private String identity;
    private String documents;
    private String verifiedAttributes;
}
