package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class IdentityRequest implements Serializable {

    private String registrationId;
    private Identity identity;
}
