package io.mosip.signup.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConsentValidator implements ConstraintValidator<Consent, String> {

    public static final String AGREE = "AGREE";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null || value.isBlank())
            return false;

        return AGREE.equals(value);
    }
}
