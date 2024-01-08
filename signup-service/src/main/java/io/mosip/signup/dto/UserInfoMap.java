package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.LanguageValue;
import io.mosip.signup.validator.PhoneNumber;
import io.mosip.signup.validator.Language;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class UserInfoMap {

    @PhoneNumber
    private String phone;

    @NotEmpty(message = ErrorConstants.INVALID_FULLNAME)
    private List<@Valid @LanguageValue(valuePatternKey = "mosip.signup.fullname.pattern")
            LanguageTaggedValue> fullName;

    @Language
    private String preferredLang;
}
