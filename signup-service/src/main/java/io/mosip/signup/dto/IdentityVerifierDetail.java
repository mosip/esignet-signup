package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class IdentityVerifierDetail implements Serializable {

    private String id;
    private Map<String, String> displayName;
    private String processType;
    private boolean active;
    private String logoUrl;
    private boolean retryOnFailure;

}
