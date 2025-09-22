package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.RestClient;
import io.restassured.response.Response;
import constants.UiConstants;

public class EsignetUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(EsignetUtil.class);
	public static String pluginName = null;
	public static JSONArray signupActiveProfiles = null;

	public static List<String> testCasesInRunScope = new ArrayList<>();

	public static void setLogLevel() {
		if (EsignetConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	public static JSONArray signupActuatorResponseArray = null;

	public static String getValueFromSignupActuator(String section, String key) {

		String value = null;
		// Normalize the key for environment variables
		String keyForEnvVariableSection = key.toUpperCase().replace("-", "_").replace(".", "_");

		// Try to fetch profiles if not already fetched
		if (signupActiveProfiles == null || signupActiveProfiles.length() == 0) {
			signupActiveProfiles = getActiveProfilesFromActuator(UiConstants.SIGNUP_ACTUATOR_URL,
					UiConstants.ACTIVE_PROFILES);
		}
		
		// First try to fetch the value from system environment
		value = getValueFromSignupActuatorWithUrl(UiConstants.SYSTEM_ENV_SECTION, keyForEnvVariableSection,
				UiConstants.SIGNUP_ACTUATOR_URL);

		// Fallback to other sections if value is not found
		if (value == null || value.isBlank()) {
			value = getValueFromSignupActuatorWithUrl(UiConstants.CLASS_PATH_APPLICATION_PROPERTIES, key,
					UiConstants.SIGNUP_ACTUATOR_URL);
		}

		if (value == null || value.isBlank()) {
			value = getValueFromSignupActuatorWithUrl(UiConstants.CLASS_PATH_APPLICATION_DEFAULT_PROPERTIES, key,
					UiConstants.SIGNUP_ACTUATOR_URL);
		}

		// Try fetching from active profiles if available
		if (value == null || value.isBlank()) {
			if (signupActiveProfiles != null && signupActiveProfiles.length() > 0) {
				for (int i = 0; i < signupActiveProfiles.length(); i++) {
					String propertySection = signupActiveProfiles.getString(i).equals(UiConstants.DEFAULT_STRING)
							? UiConstants.MOSIP_CONFIG_APPLICATION_HYPHEN_STRING + signupActiveProfiles.getString(i)
									+ UiConstants.DOT_PROPERTIES_STRING
							: signupActiveProfiles.getString(i) + UiConstants.DOT_PROPERTIES_STRING;

					value = getValueFromSignupActuatorWithUrl(propertySection, key, UiConstants.SIGNUP_ACTUATOR_URL);

					if (value != null && !value.isBlank()) {
						break;
					}
				}
			} else {
				logger.warn("No active profiles were retrieved.");
			}
		}

		// Fallback to a default section if no value found
		if (value == null || value.isBlank()) {
			value = getValueFromSignupActuatorWithUrl(EsignetConfigManager.getEsignetActuatorPropertySection(), key,
					UiConstants.SIGNUP_ACTUATOR_URL);
		}
		
		// Final fallback to the original section if no value was found
		if (value == null || value.isBlank()) {
			value = getValueFromSignupActuatorWithUrl(section, key, UiConstants.SIGNUP_ACTUATOR_URL);
		}

		// Log the final result or an error message if not found
		if (value == null || value.isBlank()) {
			logger.error("Value not found for section: " + section + ", key: " + key);
		}

		return value;
	}

	public static String getValueFromSignupActuatorWithUrl(String section, String key, String url) {
		// Generate cache key based on the url, section, and key
		String actuatorCacheKey = url + section + key;
		String value = actuatorValueCache.get(actuatorCacheKey);

		if (value != null && !value.isEmpty()) {
			return value; // Return cached value if available
		}

		try {
			
			// Fetch the actuator response array if not already populated
			if (signupActuatorResponseArray == null) {
				Response response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				JSONObject responseJson = new JSONObject(response.getBody().asString());
				signupActuatorResponseArray = responseJson.getJSONArray("propertySources");
			}

			// Search through the property sources for the section
			for (int i = 0, size = signupActuatorResponseArray.length(); i < size; i++) {
				JSONObject eachJson = signupActuatorResponseArray.getJSONObject(i);
				if (eachJson.get("name").toString().contains(section)) {
					logger.info("Found properties: " + eachJson.getJSONObject(GlobalConstants.PROPERTIES));
					value = eachJson.getJSONObject(GlobalConstants.PROPERTIES).getJSONObject(key)
							.get(GlobalConstants.VALUE).toString();
					if (EsignetConfigManager.IsDebugEnabled()) {
						logger.info("Actuator: " + url + " key: " + key + " value: " + value);
					}
					break;
				}
			}

			// Cache the retrieved value
			if (value != null && !value.isEmpty()) {
				actuatorValueCache.put(actuatorCacheKey, value);
			}

			return value;
		} catch (JSONException e) {
			logger.error("Error parsing JSON for section: " + section + ", key: " + key + " - " + e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error("Error fetching value for section: " + section + ", key: " + key + " - " + e.getMessage());
			return null;
		}
	}

	public static JSONArray getActiveProfilesFromActuator(String url, String key) {
		JSONArray activeProfiles = null;

		try {
			Response response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
			JSONObject responseJson = new JSONObject(response.getBody().asString());

			// If the key exists in the response, return the associated JSONArray
			if (responseJson.has(key)) {
				activeProfiles = responseJson.getJSONArray(key);
			} else {
				logger.warn("The key '" + key + "' was not found in the response.");
			}

		} catch (Exception e) {
			// Handle other errors like network issues, etc.
			logger.error("Error fetching active profiles from the actuator: " + e.getMessage());
		}

		return activeProfiles;
	}

	public static int getOtpResendDelayFromSignupActuator() {
		String value = getValueFromSignupActuator("classpath:/application-default.properties",
				"mosip.signup.challenge.resend-delay");
		if (value != null && !value.isBlank()) {
			return Integer.parseInt(value.trim());
		} else {
			logger.error("OTP resend delay value not found in actuator, using default 60s");
			return 60;
		}
	}

	public static String generateMobileNumberFromRegex() {
		String regex = getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.identifier.regex");
		String digitRange = regex.substring(regex.indexOf('{') + 1, regex.indexOf('}'));
		String[] parts = digitRange.split(",");

		int min = Integer.parseInt(parts[0]);
		int max = (parts.length > 1) ? Integer.parseInt(parts[1]) : min;
		int length = (min + new Random().nextInt(max - min + 1)) + 1;
		StringBuilder number = new StringBuilder();
		number.append(new Random().nextInt(9) + 1);
		for (int i = 1; i < length; i++) {
			number.append(new Random().nextInt(10));
		}

		return number.toString();
	}

	public static String getCountryCodeFromActuator() {
		return getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.identifier.prefix");
	}

	public static String getPasswordPattern() {
		return getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.password.pattern");
	}

	public static int getPasswordMinLength() {
		String value = getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.password.min-length");
		return Integer.parseInt(value);
	}

	public static int getPasswordMaxLength() {
		String value = getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.password.max-length");
		return Integer.parseInt(value);
	}

	public static String generateValidPasswordFromActuator() {
		int min = getPasswordMinLength();
		int max = getPasswordMaxLength();
		String regex = getPasswordPattern();

		Pattern pattern = Pattern.compile(regex);
		Random random = new Random();

		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lower = "abcdefghijklmnopqrstuvwxyz";
		String digits = "0123456789";
		String special = "_!@#$%^&*";
		String all = upper + lower + digits + special;

		while (true) {
			int length = min + random.nextInt(max - min + 1);
			StringBuilder pwd = new StringBuilder();

			pwd.append(upper.charAt(random.nextInt(upper.length())));
			pwd.append(lower.charAt(random.nextInt(lower.length())));
			pwd.append(digits.charAt(random.nextInt(digits.length())));
			pwd.append(special.charAt(random.nextInt(special.length())));

			for (int i = 4; i < length; i++) {
				pwd.append(all.charAt(random.nextInt(all.length())));
			}

			String candidate = pwd.toString();

			if (pattern.matcher(candidate).matches()) {
				return candidate;
			}
		}
	}

	public static String generateInvalidPassword(int length) {
		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lower = "abcdefghijklmnopqrstuvwxyz";
		String special = "!@#$%^&*()_+";
		String all = upper + lower + special;

		Random random = new Random();
		StringBuilder pwd = new StringBuilder();
		for (int i = 0; i < length; i++) {
			pwd.append(all.charAt(random.nextInt(all.length())));
		}
		return pwd.toString();
	}

	private static JSONObject signupUISpecResponse;

	private static JSONObject getSignupUISpecResponse() {
		if (signupUISpecResponse == null) {
			try {
				logger.info("Loading Signup UI Spec from " + UiConstants.SIGNUP_UI_SPEC_URL);
				Response response = RestClient.getRequest(UiConstants.SIGNUP_UI_SPEC_URL, MediaType.APPLICATION_JSON,
						MediaType.APPLICATION_JSON);
				signupUISpecResponse = new JSONObject(response.getBody().asString());
			} catch (Exception e) {
				logger.error("Failed to load Signup UI Spec from URL.", e);
				signupUISpecResponse = new JSONObject();
			}
		}
		return signupUISpecResponse;
	}

	public static String getFieldProperty(String fieldId, String property, String langCode) {
		try {
			JSONArray schema = getSignupUISpecResponse().optJSONObject("response").optJSONArray("schema");

			if (schema == null) {
				logger.warn("Schema missing in UI Spec");
				return null;
			}

			for (int i = 0; i < schema.length(); i++) {
				JSONObject field = schema.getJSONObject(i);
				if (fieldId.equals(field.optString("id"))) {

					if (field.has(property) && field.opt(property) instanceof JSONObject) {
						JSONObject obj = field.optJSONObject(property);
						if (obj != null) {
							String value = obj.optString(langCode, null);
							logger.info(property + " for " + fieldId + " in " + langCode + ": " + value);
							return value;
						}
					}

					if ("validators".equals(property)) {
						JSONArray validators = field.optJSONArray("validators");
						if (validators == null)
							continue;

						for (int j = 0; j < validators.length(); j++) {
							JSONObject validator = validators.getJSONObject(j);
							if (langCode.equals(validator.optString("langCode"))) {
								String regex = validator.optString("regex", null);
								logger.info("Regex for " + fieldId + " in " + langCode + ": " + regex);
								return regex;
							}
						}
					}
				}
			}

			logger.warn("No " + property + " for " + fieldId + " in " + langCode);
		} catch (Exception e) {
			logger.error("Error getting " + property + " for " + fieldId + " - " + langCode, e);
		}
		return null;
	}

	public static String getRegexForField(String fieldId, String langCode) {
		return getFieldProperty(fieldId, "validators", langCode);
	}

	public static String getRegexForFullName(String langCode) {
		return getRegexForField("fullName", langCode);
	}

	public static String getPlaceholderForField(String fieldId, String langCode) {
		return getFieldProperty(fieldId, "placeholder", langCode);
	}

	public static String getPlaceholderForFullName(String langCode) {
		return getPlaceholderForField("fullName", langCode);
	}

	public static String getPlaceholderForPassword(String langCode) {
		return getPlaceholderForField("password", langCode);
	}

	public static String getInfoForField(String fieldId, String langCode) {
		return getFieldProperty(fieldId, "info", langCode);
	}

	public static String getInfoForFullName(String langCode) {
		return getInfoForField("fullName", langCode);
	}

	public static String getInfoForPassword(String langCode) {
		return getInfoForField("password", langCode);
	}

	public static class FullName {
		public String english;
		public String khmer;
	}

	public static FullName generateNamesFromUiSpec() {
		String enRegex = getRegexForFullName("en");
		String kmRegex = getRegexForFullName("km");

		int enMax = extractMaxLength(enRegex);
		int kmMax = extractMaxLength(kmRegex);

		FullName fullName = new FullName();
		fullName.english = generateEnglishName(enMax);
		fullName.khmer = generateKhmerName(kmMax);

		return fullName;
	}

	public static int extractMaxLength(String regex) {
		if (regex == null)
			return 10;
		int start = regex.indexOf('{');
		int end = regex.indexOf('}');
		if (start != -1 && end != -1) {
			String[] parts = regex.substring(start + 1, end).split(",");
			if (parts.length == 2)
				return Integer.parseInt(parts[1].trim());
			return Integer.parseInt(parts[0].trim());
		}
		return 10;
	}

	public static int extractMinLength(String regex) {
		if (regex == null)
			return 2;
		int start = regex.indexOf('{');
		int end = regex.indexOf('}');
		if (start != -1 && end != -1) {
			String[] parts = regex.substring(start + 1, end).split(",");
			return Integer.parseInt(parts[0].trim());
		}
		return 2;
	}

	public static String generateEnglishName(int maxLength) {
		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ";
		Random random = new Random();
		int length = 2 + random.nextInt(Math.max(1, maxLength - 1));
		StringBuilder name = new StringBuilder();
		name.append((char) ('A' + random.nextInt(26)));
		for (int i = 1; i < length; i++) {
			name.append(letters.charAt(random.nextInt(letters.length())));
		}
		return name.toString().trim();
	}

	public static String generateKhmerName(int maxLength) {
		Random random = new Random();
		int length = 2 + random.nextInt(Math.max(1, maxLength - 1));
		StringBuilder name = new StringBuilder();

		int[][] ranges = { { 0x1780, 0x17FF }, { 0x19E0, 0x19FF }, };

		for (int i = 0; i < length; i++) {
			int[] range = ranges[random.nextInt(ranges.length)];
			int codePoint = range[0] + random.nextInt(range[1] - range[0] + 1);

			name.append((char) codePoint);
		}
		return name.toString();
	}

	public static int getMinDigits() {
		String regex = getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.identifier.regex");
		String digitRange = regex.substring(regex.indexOf('{') + 1, regex.indexOf('}'));
		String[] parts = digitRange.split(",");
		return Integer.parseInt(parts[0]);
	}

	public static int getMaxDigits() {
		String regex = getValueFromSignupActuator("applicationConfig: [classpath:/application-default.properties]",
				"mosip.signup.identifier.regex");
		String digitRange = regex.substring(regex.indexOf('{') + 1, regex.indexOf('}'));
		String[] parts = digitRange.split(",");
		return (parts.length > 1) ? Integer.parseInt(parts[1]) : Integer.parseInt(parts[0]);
	}

	public class RegisteredDetails {

		private static String registeredMobileNumber;
		private static String registeredFullName;
		private static String registeredPassword;

		public static String getMobileNumber() {
			return registeredMobileNumber;
		}

		public static void setMobileNumber(String mobileNumber) {
			registeredMobileNumber = mobileNumber;
		}

		public static String getPassword() {
			return registeredPassword;
		}

		public static void setPassword(String password) {
			registeredPassword = password;
		}

		public static String getFullName() {
			return registeredFullName;
		}

		public static void setFullName(String reisteredFullName) {
			registeredFullName = reisteredFullName;
		}
	}

	public static String getNumberStartingWithZero(int length) {
		return "0" + generateMobileNumberFromRegex().substring(0, length - 1);
	}

	public static String getLessThanMinimumDigit() {
		int min = getMinDigits();
		return generateMobileNumberFromRegex().substring(0, min - 1);
	}

	public static String getAllZeros(int length) {
		return "0".repeat(length);
	}

	public static String getMoreThanMaxDigits() {
		return generateMobileNumberFromRegex() + "1";
	}

	public static String getAlphaNumeric() {
		return generateMobileNumberFromRegex().substring(0, 5) + "ABCD";
	}

	public static String getSpecialChar() {
		return "!@#$%^";
	}

	public static String getMoreThanMaxLengthFullName(String lang) {
		int maxLength = extractMaxLength(getRegexForFullName(lang));
		return generateKhmerName(maxLength + 10);
	}

	public static String getNumericFullName(String lang) {
		int length = extractMinLength(getRegexForFullName(lang));
		return "0".repeat(length);
	}

	public static String getAlphanumericFullName(String lang) {
		int minLength = extractMinLength(getRegexForFullName(lang));
		return generateKhmerName(minLength - 1) + "1";
	}

}
