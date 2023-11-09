package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegistrationTransaction implements Serializable {

    String otp; //TODO temporary field. It will be removed later after integrate with OTP Manager service.
    String identifier;
}