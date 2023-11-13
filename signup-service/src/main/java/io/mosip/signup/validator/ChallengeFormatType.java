package io.mosip.signup.validator;

import io.mosip.esignet.core.constants.ErrorConstants;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = ChallengeFormatTypeValidator.class)
@Documented
public @interface ChallengeFormatType {
    String message() default ErrorConstants.INVALID_CHALLENGE_FORMAT;
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
