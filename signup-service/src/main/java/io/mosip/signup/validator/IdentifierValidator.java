package io.mosip.signup.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class IdentifierValidator implements ConstraintValidator<Identifier, String> {

    @Value("${mosip.signup.identifier.regex}")
    private String identifierRegex;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isBlank())
            return false;
        return value.matches(identifierRegex);
    }
}
