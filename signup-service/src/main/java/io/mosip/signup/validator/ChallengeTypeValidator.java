package io.mosip.signup.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ChallengeTypeValidator implements ConstraintValidator<ChallengeType, String>{

    @Value("#{${mosip.signup.supported.challenge-types}}")
    private List<String> supportedFormatTypes;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return supportedFormatTypes.contains(value);
    }
}
