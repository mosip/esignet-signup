package io.mosip.signup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Identity implements Serializable {

    @JsonProperty("UIN")
    private String UIN;

    @JsonProperty("IDSchemaVersion")
    private float IDSchemaVersion;

    private List<LanguageTaggedValue> fullName;
    private String phone;
    private String preferredLang;
    private Password password;
    private String registrationType;
}
