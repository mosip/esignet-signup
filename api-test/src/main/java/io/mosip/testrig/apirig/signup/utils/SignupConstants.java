package io.mosip.testrig.apirig.signup.utils;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;

public class SignupConstants {
	public static final String ACTIVE_PROFILES = "activeProfiles";
	public static final String SIGNUP_ACTUATOR_ENDPOINT_KEYWORD = "actuatorSignupEndpoint";
	public static final String IDREPO_ACTUATOR_ENDPOINT_KEYWORD = "actuatorIdRepoEndpoint";
	public static final String SIGNUP_REGISTRATION_UI_SPEC = "registrationUiSpec";
	public static final String SIGNUP_BASE_URL = SignupConfigManager.getSignupBaseUrl();
	public static final String SIGNUP_ACTUATOR_URL = SIGNUP_BASE_URL
			+ SignupConfigManager.getproperty(SIGNUP_ACTUATOR_ENDPOINT_KEYWORD);
	public static final String IDREPO_ACTUATOR_URL = BaseTestCase.ApplnURI
			+ SignupConfigManager.getproperty(IDREPO_ACTUATOR_ENDPOINT_KEYWORD);

	public static final String IDREPO_ACTUATOR_PROPERTY_SECTION = "idRepoActuatorPropertySection";
	public static final String SYSTEM_ENV_SECTION = "systemEnvironment";
	public static final String CLASS_PATH_APPLICATION_PROPERTIES = "classpath:/application.properties";
	public static final String IDREPO_DEFAULT_PROPERTIES = "id-repository-default.properties";
	public static final String APPLICATION_DEFAULT_PROPERTIES = "application-default.properties";
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
	public static final String SCHEMA = "schema";
	public static final String ALLOWED_VALUES = "allowedValues";
	public static final String USER_INFO = "userInfo";

	public static final String MOSIP_SIGNUP_FULLNAME_PATTERN_STRING = "mosip.signup.fullname.pattern";
	public static final String MOSIP_SIGNUP_IDENTIFIER_REGEX_STRING = "mosip.signup.identifier.regex";
	public static final String MOSIP_SIGNUP_PASSWORD_PATTERN_STRING = "mosip.signup.password.pattern";
	public static final String MOSIP_SIGNUP_IDREPO_MANDATORY_LANGUAGE = "mosip.signup.idrepo.mandatory-language";
	public static final String JSON_PROPERTY_STRING = "json-property";

	public static final String USE_PRE_CONFIGURED_OTP_STRING = "usePreConfiguredOtp";
	public static final String PRE_CONFIGURED_OTP_STRING = "preconfiguredOtp";
	public static final String TRUE_STRING = "true";
	public static final String ALL_ONE_OTP_STRING = "111111";

	public static final String AUTOMATION_USER = "AutomationUser";
	public static final String AUTOMATION_USER_KHM = "អ្នកប្រើប្រាស់ស្វ័យប្រវត្តិ";
	public static final String TEST_AUTOMATION = "testAutomation";
	public static final String TEST_AUTOMATION_EMAIL = "testAutomation@mosip.com";

	public static final String REQUEST_TIME = "requestTime";
	public static final String ID = "id";
	public static final String CONTROL_TYPE = "controlType";
	public static final String TYPE = "type";
	public static final String SUB_TYPE = "subType";
	public static final String DISABLED = "disabled";
	public static final String REGEX = "regex";
	public static final String DROPDOWN = "dropdown";
	public static final String RADIO = "radio";
	public static final String FORMAT = "format";
	public static final String TEXTAREA = "textarea";
	public static final String TEXTBOX = "textbox";
	public static final String DATE = "date";
	public static final String CHECKBOX = "checkbox";
	public static final String FILEUPLOAD = "fileupload";
	public static final String PHOTO = "photo";
	public static final String CONSENT = "consent";
	public static final String LOCALE = "locale";

	public static final String KHM = "khm";
	public static final String ENG = "eng";

	public static final String AT_SYMBOL = "@";
	public static final String YEAR_MONTH_DAY = "yyyy-MM-dd";

	public static final String REQUEST_TIME_PLACEHOLDER = "{{requestTime}}";
	public static final String VERIFIED_TRANSACTION_ID_PLACEHOLDER = "{{verifiedTransactionID}}";
	public static final String PHONE_PLACEHOLDER = "{{phone}}";
	public static final String USERNAME_PLACEHOLDER = "{{username}}";
	public static final String CONSENT_PLACEHOLDER = "{{consent}}";

}
