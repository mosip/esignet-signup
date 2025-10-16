package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class RegistrationFiles implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<String, String> uploadedFiles = new HashMap<>();
}
