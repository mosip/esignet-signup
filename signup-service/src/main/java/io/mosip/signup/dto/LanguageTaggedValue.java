package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class LanguageTaggedValue implements Serializable {

    public LanguageTaggedValue(String language, String value){
        this.language =  language;
        this.value = value;
    }

    @Language
    private String language;

    @NotBlank(message = ErrorConstants.INVALID_VALUE)
    private String value;
}
