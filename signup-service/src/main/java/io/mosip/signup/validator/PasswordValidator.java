/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Value("${mosip.signup.password.max-length}")
    private int maxLength;

    @Value("${mosip.signup.password.min-length}")
    private int minLength;

    @Value("${mosip.signup.password.pattern}")
    private String pattern;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null || value.isBlank())
            return false;

        if(value.length() < minLength || value.length() > maxLength)
            return false;

        return value.matches(pattern);
    }
}
