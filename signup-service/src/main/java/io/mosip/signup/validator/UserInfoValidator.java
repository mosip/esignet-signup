package io.mosip.signup.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.esignet.api.util.ErrorConstants;
import io.mosip.signup.api.dto.ProfileDto;
import io.mosip.signup.api.exception.InvalidProfileException;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
@Component
public class UserInfoValidator implements ConstraintValidator<UserInfo, JsonNode> {

    @Autowired
    private ProfileRegistryPlugin profileRegistryPlugin;

    private String actionName;

    @Override
    public void initialize(UserInfo constraintAnnotation) {
        this.actionName = constraintAnnotation.actionName();
    }

    @Override
    public boolean isValid(JsonNode value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null || value.isNull() || value.isArray() || value.isEmpty())
            return false;

        ProfileDto profileDto = new ProfileDto();
        profileDto.setActive(true);
        profileDto.setConsent("AGREE");
        profileDto.setIdentity(value);
        profileDto.setIndividualId("individualId");
        try {
            profileRegistryPlugin.validate(actionName, profileDto);
            return true;
        } catch (InvalidProfileException ex) {
            log.error("Failed to validate the provided profile data", ex);
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(ex.getErrorCode()).addConstraintViolation();
        }
        return false;
    }
}
