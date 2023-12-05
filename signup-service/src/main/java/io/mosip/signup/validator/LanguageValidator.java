package io.mosip.signup.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class LanguageValidator implements ConstraintValidator<Language, String> {


    @Value("#{${mosip.signup.supported-languages}}")
    private List<String> supportedLanguages;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null || value.isBlank())
            return false;
        return supportedLanguages.contains(value);
    }
}
