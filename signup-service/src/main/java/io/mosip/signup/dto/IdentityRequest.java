package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class IdentityRequest implements Serializable {

    private static final long serialVersionUID = 7654321098L;
    private String registrationId;
    private Identity identity;
}
