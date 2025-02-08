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

}
