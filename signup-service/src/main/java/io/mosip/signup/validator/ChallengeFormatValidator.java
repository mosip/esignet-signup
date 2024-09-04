/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
