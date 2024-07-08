package io.mosip.signup.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ProfileDto {

    String individualId;
    String consent;
    JsonNode identity;
    boolean active;
}
