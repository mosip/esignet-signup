package io.mosip.signup.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.mosip.esignet.core.exception.InvalidRequestException;
import io.mosip.kernel.core.util.StringUtils;

import java.io.IOException;

public class BooleanValidatorDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText();

        if (StringUtils.isBlank(value)) {
            throw new InvalidRequestException(ErrorConstants.INVALID_REQUEST);
        }

        switch (value.toLowerCase()) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new InvalidRequestException(ErrorConstants.INVALID_REQUEST);
        }
    }
}
