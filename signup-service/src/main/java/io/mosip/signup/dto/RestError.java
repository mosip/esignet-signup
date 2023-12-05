package io.mosip.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestError {

    private String errorCode;
    private String message;

}
