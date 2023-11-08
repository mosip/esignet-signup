package io.mosip.signup.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SettingsResponse {

    private Map<String, Object> configs;
}
