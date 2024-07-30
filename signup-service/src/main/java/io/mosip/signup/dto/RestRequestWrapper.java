package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RestRequestWrapper<T> implements Serializable {

    private static final long serialVersionUID = 8901234567L;
    private String id;
    private String version;
    private String requesttime;
    private T request;
}
