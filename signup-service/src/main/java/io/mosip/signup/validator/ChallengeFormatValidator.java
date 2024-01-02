package io.mosip.signup.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ChallengeFormatValidator implements ConstraintValidator<ChallengeFormat, String> {

    @Value("#{${mosip.signup.supported.challenge-format-types}}")
    private List<String> supportedFormatTypes;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isBlank())
            return false;
        return supportedFormatTypes.contains(value);
    }
}
