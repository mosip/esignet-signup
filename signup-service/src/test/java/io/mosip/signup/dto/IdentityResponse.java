package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class IdentityResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String status;
    private Identity identity;
    private List<String> documents;
    private List<String> verifiedAttributes;
}
