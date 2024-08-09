package io.mosip.signup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UINResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonProperty("uin")
    private String UIN;
}
