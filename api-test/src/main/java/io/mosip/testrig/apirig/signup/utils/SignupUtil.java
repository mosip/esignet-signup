package io.mosip.testrig.apirig.signup.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.signup.testrunner.MosipTestRunner;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.OTPListener;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.CertsUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.JWKKeyUtil;
import io.mosip.testrig.apirig.utils.KeycloakUserManager;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;
import io.restassured.response.Response;

public class SignupUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(SignupUtil.class);
	
	public static String getIdentityPluginNameFromEsignetActuator() {
		// Possible values = IdaAuthenticatorImpl, MockAuthenticationService

		String plugin = getValueFromEsignetActuator("classpath:/application.properties",
				"mosip.esignet.integration.authenticator");

		if (plugin == null || plugin.isBlank() == true) {
			plugin = getValueFromEsignetActuator("classpath:/application-default.properties",
					"mosip.esignet.integration.authenticator");
		}
		
		if (plugin == null || plugin.isBlank() == true) {
			plugin = getValueFromEsignetActuator("mosip-config/esignet",
					"mosip.esignet.integration.authenticator");
		}

		return plugin;
	}
	
	private static final Map<String, String> actuatorValueCache = new HashMap<>();
	
	public static JSONArray esignetActuatorResponseArray = null;

	public static String getValueFromEsignetActuator(String section, String key) {
		String url = SignupConfigManager.getEsignetBaseUrl() + SignupConfigManager.getproperty("actuatorEsignetEndpoint");
		String actuatorCacheKey = url + section + key;
		String value = actuatorValueCache.get(actuatorCacheKey);
		if (value != null && !value.isEmpty())
			return value;

		try {
			if (esignetActuatorResponseArray == null) {
				Response response = null;
				JSONObject responseJson = null;
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				responseJson = new JSONObject(response.getBody().asString());
				esignetActuatorResponseArray = responseJson.getJSONArray("propertySources");
			}

			for (int i = 0, size = esignetActuatorResponseArray.length(); i < size; i++) {
				JSONObject eachJson = esignetActuatorResponseArray.getJSONObject(i);
				if (eachJson.get("name").toString().contains(section)) {
					logger.info(eachJson.getJSONObject(GlobalConstants.PROPERTIES));
					value = eachJson.getJSONObject(GlobalConstants.PROPERTIES).getJSONObject(key)
							.get(GlobalConstants.VALUE).toString();
					if (SignupConfigManager.IsDebugEnabled())
						logger.info("Actuator: " + url + " key: " + key + " value: " + value);
					break;
				}
			}
			actuatorValueCache.put(actuatorCacheKey, value);

			return value;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			return value;
		}

	}
	
	public static TestCaseDTO isTestCaseValidForTheExecution(TestCaseDTO testCaseDTO) {
		String testCaseName = testCaseDTO.getTestCaseName();
		String inputJson = testCaseDTO.getInput();
		
		
		if (MosipTestRunner.skipAll == true) {
			throw new SkipException(GlobalConstants.PRE_REQUISITE_FAILED_MESSAGE);
		}
		
		
		if (getIdentityPluginNameFromEsignetActuator().toLowerCase().contains("mockauthenticationservice")) {
			
			// TO DO - need to conform whether esignet distinguishes between UIN and VID. BAsed on that need to remove VID test case from YAML.
			BaseTestCase.setSupportedIdTypes(Arrays.asList("UIN"));
			
			// Let run test cases eSignet & mock (for identity)   -- only UIN  test cases
			
			String endpoint = testCaseDTO.getEndPoint();
			if (endpoint.contains("/esignet/vci/") == true) {
				throw new SkipException(GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE);
			}
			if (endpoint.contains("/esignet/vci/") == false && endpoint.contains("/esignet/") == false
					&& endpoint.contains("/v1/signup/") == false && endpoint.contains("/mock-identity-system/") == false
					&& endpoint.contains("$GETENDPOINTFROMWELLKNOWN$") == false) {
				throw new SkipException(GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE);
			}
			if ((testCaseName.contains("_KycBioAuth_") || testCaseName.contains("_BioAuth_")
					|| testCaseName.contains("_SendBindingOtp_uin_Email_Valid_Smoke")
					|| testCaseName.contains("ESignet_AuthenticateUserIDP_NonAuth_uin_Otp_Valid_Smoke"))) {
				throw new SkipException(GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE);
			}

		} else if (getIdentityPluginNameFromEsignetActuator().toLowerCase().contains("idaauthenticatorimpl")) {
			// Let run test cases eSignet & MOSIP API calls --- both UIN and VID

			BaseTestCase.setSupportedIdTypes(Arrays.asList("UIN", "VID"));

			String endpoint = testCaseDTO.getEndPoint();
			if (endpoint.contains("/mock-identity-system/") == true
					|| ((testCaseName.equals("ESignet_CreateOIDCClient_all_Valid_Smoke_sid")
							|| testCaseName.equals("Signup_ESignet_CreateOIDCClient_all_Valid_Smoke_sid")
							|| testCaseName.equals("ESignet_CreateOIDCClient_Misp_Valid_Smoke_sid")
							|| testCaseName.equals("ESignet_CreateOIDCClient_NonAuth_all_Valid_Smoke_sid"))
							&& endpoint.contains("/v1/esignet/client-mgmt/oauth-client"))) {
				throw new SkipException(GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE);
			}

			JSONArray individualBiometricsArray = new JSONArray(
					getValueFromAuthActuator("json-property", "individualBiometrics"));
			String individualBiometrics = individualBiometricsArray.getString(0);

			if ((testCaseName.contains("_KycBioAuth_") || testCaseName.contains("_BioAuth_")
					|| testCaseName.contains("_SendBindingOtp_uin_Email_Valid_Smoke"))
					&& (!isElementPresent(globalRequiredFields, individualBiometrics))) {
				throw new SkipException(GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE);
			}

		} else if (getIdentityPluginNameFromEsignetActuator().toLowerCase().contains("sunbird")) {
			// Let run test cases eSignet & Sunbird (for identity)   -- only KBI 
			
		}
		
		if (testCaseDTO.isValidityCheckRequired()) {
			if (testCaseName.contains("uin") || testCaseName.contains("UIN") || testCaseName.contains("Uin")) {
				if (BaseTestCase.getSupportedIdTypesValue().contains("UIN")
						&& BaseTestCase.getSupportedIdTypesValue().contains("uin")) {
					throw new SkipException("Idtype UIN not supported skipping the testcase");
				}
			} else if (testCaseName.contains("vid") || testCaseName.contains("VID") || testCaseName.contains("Vid")) {
				if (BaseTestCase.getSupportedIdTypesValue().contains("VID")
						&& BaseTestCase.getSupportedIdTypesValue().contains("vid")) {
					throw new SkipException("Idtype VID not supported skipping the testcase");
				}
			}
		}

		if (SkipTestCaseHandler.isTestCaseInSkippedList(testCaseName)) {
			throw new SkipException(GlobalConstants.KNOWN_ISSUES);
		}
		
		if ((testCaseName.contains("ESignet_AuthenticateUserPassword") && inputJson.contains("_PHONE$")) || testCaseName.contains("AuthenticateUserPasswordNegTC_UnRegistered_IndividualId_Neg")) {
			String suffix = getValueFromEsignetActuator("classpath:/application.properties",
					"mosip.esignet.ui.config.username.postfix");

			if (suffix == null || suffix.isBlank() == true) {
				suffix = getValueFromEsignetActuator("classpath:/application-default.properties",
						"mosip.esignet.ui.config.username.postfix");
			}
			
			if (suffix == null || suffix.isBlank() == true) {
				suffix = getValueFromEsignetActuator("mosip-config/esignet",
						"mosip.esignet.ui.config.username.postfix");
			}
			
			if (suffix != null && suffix.isBlank() == false) {
				testCaseDTO.setInput(testCaseDTO.getInput().replace("_PHONE$", "_PHONE$" + suffix));
				
				if (testCaseName.contains("_UnRegistered_IndividualId_Neg")) {
					testCaseDTO.setInput(testCaseDTO.getInput().replace("$PHONENUMBERFROMREGEXFORSIGNUP$", "$PHONENUMBERFROMREGEXFORSIGNUP$" + suffix));
				}
			}
		}

		return testCaseDTO;
	}
	
	public static String inputstringKeyWordHandeler(String jsonString, String testCaseName) {
		if (jsonString.contains("$ID:")) {
			String autoGenIdFileName = signupAutoGeneratedIdPropFileName;
			jsonString = replaceIdWithAutogeneratedId(jsonString, "$ID:", autoGenIdFileName);
		}
		
		if (jsonString.contains(GlobalConstants.TIMESTAMP)) {
			jsonString = replaceKeywordValue(jsonString, GlobalConstants.TIMESTAMP, generateCurrentUTCTimeStamp());
		}
		
		if (testCaseName.contains("ESignet_GenerateApiKey_")) {
			KeycloakUserManager.createKeyCloakUsers(genPartnerName, genPartnerEmail, "AUTH_PARTNER");
		}
		
		if (testCaseName.contains("ESignet_GenerateApiKeyKyc_")) {
			KeycloakUserManager.createKeyCloakUsers(genPartnerName + "2n", "12d" + genPartnerEmail, "AUTH_PARTNER");
		}
		
		if (jsonString.contains("_$REGISTEREDUSERFULLNAME$")) {
			JSONObject inputReqJson = new JSONObject(jsonString);
			JSONObject fullNameJson = new JSONObject();
			String keyName = "";
			String stringArray = "";
			if (inputReqJson.has("request") && inputReqJson.getJSONObject("request").has("challengeInfo")
					&& inputReqJson.getJSONObject("request").getJSONArray("challengeInfo").length() > 1 && inputReqJson
							.getJSONObject("request").getJSONArray("challengeInfo").getJSONObject(1).has("challenge")) {
				keyName = inputReqJson.getJSONObject("request").getJSONArray("challengeInfo").getJSONObject(1)
						.getString("challenge");
				if (!keyName.isBlank() && keyName != null) {
					stringArray = CertsUtil.getCertificate(keyName);
					if (!stringArray.isBlank() && stringArray != null) {
						JSONArray fullNameArray = new JSONArray(stringArray);
						fullNameJson.put("fullName", fullNameArray);
						byte[] byteBioData = fullNameJson.toString().getBytes();

						String challengeValue = Base64.getUrlEncoder().encodeToString(byteBioData);
						logger.info(challengeValue);

						jsonString = replaceKeywordValue(jsonString, keyName, challengeValue);
					}
				}
			}
		}
		
		if (jsonString.contains("$PASSWORDFORAUTHENTICATION$")) {
			jsonString = replaceKeywordValue(jsonString, "$PASSWORDFORAUTHENTICATION$",
					PASSWORD_FOR_ADDIDENTITY_AND_REGISTRATION);
		}
		
		if (jsonString.contains("$RESETPASSWORDFORAUTHENTICATION$")) {
			jsonString = replaceKeywordValue(jsonString, "$RESETPASSWORDFORAUTHENTICATION$", PASSWORD_TO_RESET);
		}
		
		if (jsonString.contains("$RANDOMIDFOROIDCCLIENT$")) {
			jsonString = replaceKeywordValue(jsonString, "$RANDOMIDFOROIDCCLIENT$",
					"mosip" + generateRandomNumberString(2) + Calendar.getInstance().getTimeInMillis());
		}
		
		if (jsonString.contains("$IDPREDIRECTURI$")) {
			jsonString = replaceKeywordValue(jsonString, "$IDPREDIRECTURI$",
					ApplnURI.replace(GlobalConstants.API_INTERNAL, "healthservices") + "/userprofile");
		}
		
		if (jsonString.contains("$SIGNUPREDIRECTURI$")) {
			jsonString = replaceKeywordValue(jsonString, "$SIGNUPREDIRECTURI$",
					SignupConfigManager.getSignupBaseUrl() + "/identity-verification");
		}
		
		if (jsonString.contains("$OIDCJWKKEY$")) {
			String jwkKey = "";
			if (gettriggerESignetKeyGen1()) {
				jwkKey = JWKKeyUtil.generateAndCacheJWKKey(OIDCJWK1);
				settriggerESignetKeyGen1(false);
			} else {
				jwkKey = JWKKeyUtil.getJWKKey(OIDCJWK1);
			}
			jsonString = replaceKeywordValue(jsonString, "$OIDCJWKKEY$", jwkKey);
		}
		
		if (jsonString.contains("$OIDCJWKKEY2$")) {
			String jwkKey = "";
			if (gettriggerESignetKeyGen2()) {
				jwkKey = JWKKeyUtil.generateAndCacheJWKKey(OIDCJWK2);
				settriggerESignetKeyGen2(false);
			} else {
				jwkKey = JWKKeyUtil.getJWKKey(OIDCJWK2);
			}
			jsonString = replaceKeywordValue(jsonString, "$OIDCJWKKEY2$", jwkKey);
		}
		
		if (jsonString.contains("$OIDCJWKKEY3$")) {
			String jwkKey = "";
			if (gettriggerESignetKeyGen12()) {
				jwkKey = JWKKeyUtil.generateAndCacheJWKKey(OIDCJWK3);
				settriggerESignetKeyGen12(false);
			} else {
				jwkKey = JWKKeyUtil.getJWKKey(OIDCJWK3);
			}
			jsonString = replaceKeywordValue(jsonString, "$OIDCJWKKEY3$", jwkKey);
		}
		
		if (jsonString.contains("$CLIENT_ASSERTION_JWK$")) {
			String oidcJWKKeyString = JWKKeyUtil.getJWKKey(OIDCJWK1);
			logger.info("oidcJWKKeyString =" + oidcJWKKeyString);
			try {
				oidcJWKKey1 = RSAKey.parse(oidcJWKKeyString);
				logger.info("oidcJWKKey1 =" + oidcJWKKey1);
			} catch (java.text.ParseException e) {
				logger.error(e.getMessage());
			}
			JSONObject request = new JSONObject(jsonString);
			String clientId = null;
			if (request.has("client_id")) {
				clientId = request.get("client_id").toString();
			}
			
			String tempUrl = getValueFromEsignetWellKnownEndPoint("token_endpoint", SignupConfigManager.getEsignetBaseUrl());
			
			jsonString = replaceKeywordValue(jsonString, "$CLIENT_ASSERTION_JWK$",
					signJWKKey(clientId, oidcJWKKey1, tempUrl));
		}
		
		return jsonString;
		
	}
	
	public static String replaceKeywordValue(String jsonString, String keyword, String value) {
		if (value != null && !value.isEmpty())
			return jsonString.replace(keyword, value);
		else
			throw new SkipException("Marking testcase as skipped as required fields are empty " + keyword);
	}
	
	public static String signJWKKey(String clientId, RSAKey jwkKey, String tempUrl) {
		int idTokenExpirySecs = Integer
				.parseInt(getValueFromEsignetActuator(SignupConfigManager.getEsignetActuatorPropertySection(),
						GlobalConstants.MOSIP_ESIGNET_ID_TOKEN_EXPIRE_SECONDS));
		JWSSigner signer;

		try {
			signer = new RSASSASigner(jwkKey);

			Date currentTime = new Date();

			// Create a Calendar instance to manipulate time
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentTime);

			// Add one hour to the current time
			calendar.add(Calendar.HOUR_OF_DAY, (idTokenExpirySecs / 3600)); // Adding one hour

			// Get the updated expiration time
			Date expirationTime = calendar.getTime();

			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(clientId).audience(tempUrl).issuer(clientId)
					.issueTime(currentTime).expirationTime(expirationTime).build();

			logger.info("JWT current and expiry time " + currentTime + " & " + expirationTime);

			SignedJWT signedJWT = new SignedJWT(
					new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwkKey.getKeyID()).build(), claimsSet);

			signedJWT.sign(signer);
			clientAssertionToken = signedJWT.serialize();
		} catch (Exception e) {
			logger.error("Exception while signing oidcJWKKey for client assertion: " + e.getMessage());
		}
		return clientAssertionToken;
	}
	
	public static String getValueFromEsignetWellKnownEndPoint(String key, String baseURL) {
		String url = baseURL + SignupConfigManager.getproperty("esignetWellKnownEndPoint");
		Response response = null;
		JSONObject responseJson = null;
		if (responseJson == null) {
			try {
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				responseJson = new org.json.JSONObject(response.getBody().asString());
				return responseJson.getString(key);
			} catch (Exception e) {
				logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			}
		}
		return responseJson.getString(key);
	}
	
	public static String clientAssertionToken;
	
	protected static final String OIDCJWK1 = "oidcJWK1";
	protected static final String OIDCJWK2 = "oidcJWK2";
	protected static final String OIDCJWK3 = "oidcJWK3";
	
	protected static RSAKey oidcJWKKey1 = null;
	
	protected static boolean triggerESignetKeyGen1 = true;
	protected static boolean triggerESignetKeyGen2 = true;
	protected static boolean triggerESignetKeyGen12 = true;
	
	private static boolean gettriggerESignetKeyGen1() {
		return triggerESignetKeyGen1;
	}
	
	private static void settriggerESignetKeyGen1(boolean value) {
		triggerESignetKeyGen1 = value;
	}
	
	private static void settriggerESignetKeyGen2(boolean value) {
		triggerESignetKeyGen2 = value;
	}

	private static boolean gettriggerESignetKeyGen2() {
		return triggerESignetKeyGen2;
	}
	
	private static void settriggerESignetKeyGen12(boolean value) {
		triggerESignetKeyGen12 = value;
	}

	private static boolean gettriggerESignetKeyGen12() {
		return triggerESignetKeyGen12;
	}
	
	public static String otpHandler(String inputJson, String testCaseName) {
		JSONObject request = new JSONObject(inputJson);
		String emailId = null;
		String otp = null;
		if (request.has(GlobalConstants.REQUEST)) {
			if (request.getJSONObject(GlobalConstants.REQUEST).has("otp")) {
				if (request.getJSONObject(GlobalConstants.REQUEST).getString("otp").endsWith(GlobalConstants.MOSIP_NET)
						|| request.getJSONObject(GlobalConstants.REQUEST).getString("otp")
								.endsWith(GlobalConstants.OTP_AS_PHONE)) {
					emailId = request.getJSONObject(GlobalConstants.REQUEST).get("otp").toString();
					if (emailId.endsWith(GlobalConstants.OTP_AS_PHONE))
						emailId = emailId.replace(GlobalConstants.OTP_AS_PHONE, "");
					logger.info(emailId);
					otp = OTPListener.getOtp(emailId);
					request.getJSONObject(GlobalConstants.REQUEST).put("otp", otp);
					inputJson = request.toString();
					return inputJson;
				}
			} else if (request.has(GlobalConstants.REQUEST)) {
				if (request.getJSONObject(GlobalConstants.REQUEST).has(GlobalConstants.CHALLENGELIST)) {
					if (request.getJSONObject(GlobalConstants.REQUEST).getJSONArray(GlobalConstants.CHALLENGELIST)
							.length() > 0) {
						if (request.getJSONObject(GlobalConstants.REQUEST).getJSONArray(GlobalConstants.CHALLENGELIST)
								.getJSONObject(0).has(GlobalConstants.CHALLENGE)) {
							if (request.getJSONObject(GlobalConstants.REQUEST)
									.getJSONArray(GlobalConstants.CHALLENGELIST).getJSONObject(0)
									.getString(GlobalConstants.CHALLENGE).endsWith(GlobalConstants.MOSIP_NET)
									|| request.getJSONObject(GlobalConstants.REQUEST)
											.getJSONArray(GlobalConstants.CHALLENGELIST).getJSONObject(0)
											.getString(GlobalConstants.CHALLENGE)
											.endsWith(GlobalConstants.OTP_AS_PHONE)) {
								emailId = request.getJSONObject(GlobalConstants.REQUEST)
										.getJSONArray(GlobalConstants.CHALLENGELIST).getJSONObject(0)
										.getString(GlobalConstants.CHALLENGE);
								if (emailId.endsWith(GlobalConstants.OTP_AS_PHONE))
									emailId = emailId.replace(GlobalConstants.OTP_AS_PHONE, "");
								logger.info(emailId);
								otp = OTPListener.getOtp(emailId);
								request.getJSONObject(GlobalConstants.REQUEST)
										.getJSONArray(GlobalConstants.CHALLENGELIST).getJSONObject(0)
										.put(GlobalConstants.CHALLENGE, otp);
								inputJson = request.toString();
							}
						}
					}
				}
				return inputJson;
			}
		}

		return inputJson;
	}
	
	public static JSONArray signupActuatorResponseArray = null;

	public static String getValueFromSignupActuator(String section, String key) {
		String url = SignupConfigManager.getSignupBaseUrl() + SignupConfigManager.getproperty("actuatorSignupEndpoint");
		String actuatorCacheKey = url + section + key;
		String value = actuatorValueCache.get(actuatorCacheKey);
		if (value != null && !value.isEmpty())
			return value;

		try {
			if (signupActuatorResponseArray == null) {
				Response response = null;
				JSONObject responseJson = null;
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				responseJson = new JSONObject(response.getBody().asString());
				signupActuatorResponseArray = responseJson.getJSONArray("propertySources");
			}

			for (int i = 0, size = signupActuatorResponseArray.length(); i < size; i++) {
				JSONObject eachJson = signupActuatorResponseArray.getJSONObject(i);
				if (eachJson.get("name").toString().contains(section)) {
					logger.info(eachJson.getJSONObject(GlobalConstants.PROPERTIES));
					value = eachJson.getJSONObject(GlobalConstants.PROPERTIES).getJSONObject(key)
							.get(GlobalConstants.VALUE).toString();
					if (SignupConfigManager.IsDebugEnabled())
						logger.info("Actuator: " + url + " key: " + key + " value: " + value);
					break;
				}
			}
			actuatorValueCache.put(actuatorCacheKey, value);

			return value;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			return value;
		}

	}
	
	public static void getSupportedLanguage() {
		String supportedLanguages = getValueFromSignupActuator("systemEnvironment",
				"MOSIP_SIGNUP_SUPPORTED_LANGUAGES");
		
		if (supportedLanguages == null || supportedLanguages.isBlank() == true) {
			supportedLanguages = getValueFromSignupActuator("classpath:/application-default.properties",
					"mosip.signup.supported-languages");
		}
		
		if (supportedLanguages == null || supportedLanguages.isBlank() == true) {
			supportedLanguages = getValueFromSignupActuator("mosip/mosip-config/signup",
					"mosip.signup.supported-languages");
		}

		if (supportedLanguages != null && supportedLanguages.isBlank() == false) {
			supportedLanguages = supportedLanguages.replace("{", "").replace("}", "").replace("'", "");

			// Split the string by commas
			String[] languages = supportedLanguages.split(",");

			// Use a TreeSet to sort the languages
			Set<String> sortedLanguages = new TreeSet<>();
			for (String language : languages) {
				sortedLanguages.add(language.trim()); // Trim to remove any extra spaces
			}

			// Add sorted languages to the languageList
			BaseTestCase.languageList.addAll(sortedLanguages);

			logger.info("languageList " + BaseTestCase.languageList);
		} else {
			logger.error("Language not found");
		}
	}
	
	public static String getTypeValueFromWebSocketMessage(String message) {
		try {
			JSONObject rootObject = new JSONObject(message);

			if (rootObject.has("step") && !rootObject.get("step").equals(JSONObject.NULL)) {
				JSONObject stepObject = rootObject.getJSONObject("step");
				if (stepObject.has("code")) {
					return stepObject.getString("code");
				}
			}

			if (rootObject.has("feedback") && !rootObject.get("feedback").equals(JSONObject.NULL)) {
				JSONObject feedbackObject = rootObject.getJSONObject("feedback");
				if (feedbackObject.has("code")) {
					return feedbackObject.getString("code");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("Type is not available in the response.");
		return null;
	}
	
	public static List<String> signupSupportedLanguage = new ArrayList<>();
	public static void getSignupSupportedLanguage() {
		signupSupportedLanguage = new ArrayList<>();
		
//		List<String> signupSupportedLanguage = new ArrayList<>();
		String supportedLanguages = getValueFromSignupActuator("systemEnvironment",
				"MOSIP_SIGNUP_SUPPORTED_LANGUAGES");
		
		if (supportedLanguages == null || supportedLanguages.isBlank() == true) {
			supportedLanguages = getValueFromSignupActuator("mosip/mosip-config/signup",
					"mosip.signup.supported-languages");
		}
		
		if (supportedLanguages == null || supportedLanguages.isBlank() == true) {
			supportedLanguages = getValueFromSignupActuator("classpath:/application-default.properties",
					"mosip.signup.supported-languages");
		}

		if (supportedLanguages != null && supportedLanguages.isBlank() == false) {
			supportedLanguages = supportedLanguages.replace("{", "").replace("}", "").replace("'", "");

			// Split the string by commas
			String[] languages = supportedLanguages.split(",");

			// Use a TreeSet to sort the languages
			Set<String> sortedLanguages = new TreeSet<>();
			for (String language : languages) {
				sortedLanguages.add(language.trim()); // Trim to remove any extra spaces
			}

			// Add sorted languages to the languageList
			signupSupportedLanguage.addAll(sortedLanguages);

			logger.info("signupSupportedLanguage " + signupSupportedLanguage);
		} else {
			logger.error("Language not found");
		}
	}
	
	
	public static String generateFullNameToRegisterUsers(String inputJson, String testCaseName) {
		JSONArray fullNameArray = new JSONArray();
		String fullNamePattern = getValueFromSignUpSetting("fullname.pattern").toString();
		List<String> fullnames = Arrays.asList(" ឮᨪដ", "សុភិបាល", "វណ្ណៈ", "៻៥᧿", "គុសល", "ស្រីមុជ", "ចន្ថ័រន", "  ឃ  ំ ដ     ៹ម");
		String randomFullName = getRandomElement(fullnames);
		getSignupSupportedLanguage();
		List<String> languageList =  new ArrayList<>(signupSupportedLanguage);
//		languageList = BaseTestCase.getLanguageList();

		// For current sprint eng is removed.
		if (languageList.contains("eng"))
			languageList.remove("eng");
		if (testCaseName.contains("_Only_1st_Lang_On_Name_Field_Neg") && languageList.size() > 1)
			languageList.remove(1);

		for (int i = 0; i < languageList.size(); i++) {
			if (languageList.get(i) != null && !languageList.get(i).isEmpty()) {
				JSONObject eachValueJson = new JSONObject();
				if (testCaseName.contains("_Invalid_Value_On_Language_Field_Neg")) {
					eachValueJson.put(GlobalConstants.LANGUAGE, "sdbfkfj");
				} else if (testCaseName.contains("_Empty_Value_On_Language_Field_Neg")) {
					eachValueJson.put(GlobalConstants.LANGUAGE, "");
				} else
					eachValueJson.put(GlobalConstants.LANGUAGE, languageList.get(i));
				String generatedString = "";

				try {
					if (!fullNamePattern.isEmpty()) {
//						while (generatedString.isBlank()) {
//							generatedString = genStringAsperRegex(fullNamePattern);
//						}
//						eachValueJson.put(GlobalConstants.VALUE, generatedString);

						eachValueJson.put(GlobalConstants.VALUE, randomFullName);

						if (testCaseName.contains("_Only_Language_On_Name_Field_Neg"))
							eachValueJson.remove(GlobalConstants.VALUE);
						else if (testCaseName.contains("_Only_Value_On_Name_Field_Neg"))
							eachValueJson.remove(GlobalConstants.LANGUAGE);
						else if (testCaseName.contains("_Empty_Value_On_Name_Field_Neg"))
							eachValueJson.put(GlobalConstants.VALUE, "");
						else if (testCaseName.contains("_Space_Value_On_Name_Field_Neg"))
							eachValueJson.put(GlobalConstants.VALUE, " ");
						else if (testCaseName.contains("_Only_SpecialChar_On_Name_Field_Neg"))
							eachValueJson.put(GlobalConstants.VALUE, "%^&*&** ^&&&");
						else if (testCaseName.contains("_Only_Num_Value_On_Name_Field_Neg"))
							eachValueJson.put(GlobalConstants.VALUE, "564846841");
						else if (testCaseName.contains("_AlphaNum_Value_On_Name_Field_Neg"))
							eachValueJson.put(GlobalConstants.VALUE, "អានុសា765651");
						else if (testCaseName.contains("_Exceeding_Limit_Value_On_Name_Field_Neg"))
							eachValueJson.put(GlobalConstants.VALUE, generateRandomAlphaNumericString(50));

					} else {
						logger.error("REGEX pattern not availble in the setting API");
						return "";
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					return "";
				}
				fullNameArray.put(eachValueJson);
			}

		}
		if (testCaseName.contains("_SName_Valid")) {
			CertsUtil.addCertificateToCache(testCaseName + "_$REGISTEREDUSERFULLNAME$", fullNameArray.toString());
		}
		inputJson = replaceKeywordValue(inputJson, "$FULLNAMETOREGISTERUSER$", fullNameArray.toString());

		return inputJson;
	}
	
	public static JSONObject signUpSettingsResponseJson = null;

	public static String getValueFromSignUpSetting(String key) {
		String url = SignupConfigManager.getSignupBaseUrl() + SignupConfigManager.getproperty("signupSettingsEndPoint");
		String actuatorCacheKey = url + key;
		String value = actuatorValueCache.get(actuatorCacheKey);
		if (value != null && !value.isEmpty())
			return value;

		try {
			if (signUpSettingsResponseJson == null) {
				Response response = null;
				JSONObject responseJson = null;
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

				responseJson = new JSONObject(response.getBody().asString());
				signUpSettingsResponseJson = responseJson.getJSONObject("response").getJSONObject("configs");
			}

			if (signUpSettingsResponseJson.has(key)) {
				value = signUpSettingsResponseJson.getString(key);
				actuatorValueCache.put(actuatorCacheKey, value);
			}

			if (SignupConfigManager.IsDebugEnabled())
				logger.info("Actuator: " + url + " key: " + key + " value: " + value);
			return value;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			return "";
		}

	}
	
	public static String getPhoneNumberFromRegex() {
		String phoneNumber = "";
		// TODO Regex needs to be taken from Schema
		String phoneNumberRegex = getValueFromSignUpSetting("identifier.pattern");
		if (!phoneNumberRegex.isEmpty())
			try {
				phoneNumber = genStringAsperRegex(phoneNumberRegex);
				return phoneNumber;
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		return phoneNumber;
	}

	public static String getPasswordPatternRegex() {
		String password = "";
		// TODO Regex needs to be taken from Schema
		String passwordRegex = getValueFromSignUpSetting("password.pattern");
		if (!passwordRegex.isEmpty())
			try {
				password = genStringAsperRegex(passwordRegex);
				return password;
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		return password;
	}
	
}