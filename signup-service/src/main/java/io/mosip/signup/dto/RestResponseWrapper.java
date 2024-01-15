package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class RestResponseWrapper<T> implements Serializable {
    
    private String id;
    private String version;
    private String responsetime;
    private String metadata;
    private T response;
    private ArrayList<RestError> errors;
}
