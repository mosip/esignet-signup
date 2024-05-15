package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CaptchaRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String captchaToken;
    private String moduleName;
}
