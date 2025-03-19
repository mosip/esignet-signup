package io.mosip.testrig.apirig.signup.utils;

public class SignupConstants {
	public static final String ACTIVE_PROFILES = "activeProfiles";

	public static final String ESIGNET_ACTUATOR_ENDPOINT_KEYWORD = "actuatorEsignetEndpoint";

	public static final String SIGNUP_ACTUATOR_ENDPOINT_KEYWORD = "actuatorSignupEndpoint";

	public static final String SIGNUP_BASE_URL = SignupConfigManager.getSignupBaseUrl();

	public static final String ESIGNET_BASE_URL = SignupConfigManager.getEsignetBaseUrl();

	public static final String ESIGNET_ACTUATOR_URL = ESIGNET_BASE_URL
			+ SignupConfigManager.getproperty(ESIGNET_ACTUATOR_ENDPOINT_KEYWORD);

	public static final String SIGNUP_ACTUATOR_URL = SIGNUP_BASE_URL
			+ SignupConfigManager.getproperty(SIGNUP_ACTUATOR_ENDPOINT_KEYWORD);

	public static final String SYSTEM_ENV_SECTION = "systemEnvironment";
	
	public static final String CLASS_PATH_APPLICATION_PROPERTIES = "classpath:/application.properties";
	
	public static final String CLASS_PATH_APPLICATION_DEFAULT_PROPERTIES = "classpath:/application-default.properties";

	public static final String DEFAULT_STRING = "default";
	
	public static final String MOSIP_CONFIG_APPLICATION_HYPHEN_STRING = "mosip-config/application-";
	
	public static final String DOT_PROPERTIES_STRING = ".properties";
	
	public static final String MOSIP_SIGNUP_STATUS_REQUEST_LIMIT_STRING = "mosip.signup.status.request.limit";
	
	public static final String MOSIP_SIGNUP_STATUS_REQUEST_DELAY_STRING = "mosip.signup.status.request.delay";
	
	public static final String STATUS_STRING = "status";
	
	public static final String UNKNOWN_EROOR_STRING = "unknown_error";
	
	public static final String PROPERTIES_STRING = "properties";
	
	public static final String IDENTITY_STRING = "identity";
	
	public static final String VALIDATORS_STRING = "validators";
	
	public static final String VALIDATOR_STRING = "validator";
	
	public static final String FULL_NAME_REGEX_PATTERN_STRING = "fullNameRegexPattern";
	
	public static final String PHONE_NUMBER_REGEX_PATTERN_STRING = "phoneNumberRegexPattern";
	
	public static final String PASSWORD_REGEX_PATTERN_STRING = "passwordRegexPattern";
	
	public static final String PHONE_STRING = "phone";
	
	public static final String FULL_NAME_STRING = "fullName";
	
	public static final String PASSWORD_STRING = "password";
	
	public static final String SCHEMA_JSON_STRING = "schemaJson";
	
	public static final String MOSIP_SIGNUP_FULLNAME_PATTERN_STRING = "mosip.signup.fullname.pattern";
	
	public static final String MOSIP_SIGNUP_IDENTIFIER_REGEX_STRING = "mosip.signup.identifier.regex";
	
	public static final String MOSIP_SIGNUP_PASSWORD_PATTERN_STRING =  "mosip.signup.password.pattern";
	
	public static final String MOSIP_SIGNUP_IDREPO_MANDATORY_LANGUAGE = "mosip.signup.idrepo.mandatory-language";
	
	public static final String JSON_PROPERTY_STRING = "json-property";

}
