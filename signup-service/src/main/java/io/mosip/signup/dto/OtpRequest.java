package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OtpRequest implements Serializable {

    private static final long serialVersionUID = 7890123456L;
    private String key;
}
