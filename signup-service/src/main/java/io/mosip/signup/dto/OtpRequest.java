package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OtpRequest implements Serializable {
    private String key;
}
