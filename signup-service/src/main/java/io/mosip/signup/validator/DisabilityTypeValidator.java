/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class DisabilityTypeValidator implements ConstraintValidator<DisabilityType, String> {

    private final List<String> allowedValues = Arrays.asList("VISION", "AUDITORY", "MOBILITY", "NEUROLOGICAL");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null) //null is accepted
            return true;

        return allowedValues.contains(value);
    }
}
