package io.mosip.signup.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    @Value("${mosip.signup.identifier.regex}")
    private String phoneNumberRegex;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null || value.isBlank())
            return false;
        return value.matches(phoneNumberRegex);
    }
}
