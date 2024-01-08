package io.mosip.signup.validator;

import io.mosip.signup.dto.LanguageTaggedValue;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LanguageValueValidator implements ConstraintValidator<LanguageValue, LanguageTaggedValue> {

    private String valuePattern;
    private String language;

    @Autowired
    private Environment environment;

    @Override
    public void initialize(LanguageValue value) {
        this.valuePattern = environment.getProperty(value.valuePatternKey());
        this.language = value.language();
    }

    @Override
    public boolean isValid(LanguageTaggedValue value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null)
            return false;
        return value.getLanguage().equals(this.language) &&
                (StringUtils.hasText(value.getValue()) && value.getValue().matches(valuePattern));
    }
}
