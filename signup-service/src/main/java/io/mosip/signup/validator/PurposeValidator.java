package io.mosip.signup.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import io.mosip.signup.util.Purpose;

public class PurposeValidator implements ConstraintValidator<io.mosip.signup.validator.Purpose, Purpose> {
    @Override
    public boolean isValid(Purpose value, ConstraintValidatorContext context) {
        if(value == null)
            return false;
        return value.equals(Purpose.REGISTRATION) || value.equals(Purpose.RESET_PASSWORD);
    }
}
