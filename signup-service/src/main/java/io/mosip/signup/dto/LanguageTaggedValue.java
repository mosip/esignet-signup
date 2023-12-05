package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Language;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class LanguageTaggedValue {

    @Language
    private String language;

    @NotBlank(message = ErrorConstants.INVALID_VALUE)
    private String value;
}
