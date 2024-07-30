package io.mosip.signup.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Identity implements Serializable {

    private static final long serialVersionUID = 5678901234L;

    @JsonProperty("UIN")
    private String UIN;

    @JsonProperty("IDSchemaVersion")
    private float IDSchemaVersion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LanguageTaggedValue> fullName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phone;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String preferredLang;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Password password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String registrationType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> selectedHandles;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean phoneVerified;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long updatedAt;
}
