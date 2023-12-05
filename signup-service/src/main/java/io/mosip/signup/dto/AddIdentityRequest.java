package io.mosip.signup.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class AddIdentityRequest implements Serializable {

    private String registrationId;
    private Identity identity;
}
