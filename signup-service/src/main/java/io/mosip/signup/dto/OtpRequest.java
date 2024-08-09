package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OtpRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String key;
}
