/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.signup.api.dto.ProfileDto;
import io.mosip.signup.api.spi.ProfileRegistryPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class IdentifierValidator implements ConstraintValidator<Identifier, String> {

    @Value("${mosip.signup.identifier.name}")
    private String identifierName;

    @Autowired
    private ProfileRegistryPlugin profileRegistryPlugin;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isBlank())
            return false;

        try {
            ProfileDto profileDto = new ProfileDto();
            Map<String, String> identityMap = new HashMap<>();
            identityMap.put(identifierName, value);
            profileDto.setIdentity(objectMapper.valueToTree(identityMap));
            profileRegistryPlugin.validate("UPDATE", profileDto);
        } catch (Exception e) {
            log.error("Error while validating identifier", e);
            return false;
        }
        return true;
    }
}
