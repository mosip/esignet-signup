package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CaptchaResponse implements Serializable {

    private boolean success;
    private String message;
}
