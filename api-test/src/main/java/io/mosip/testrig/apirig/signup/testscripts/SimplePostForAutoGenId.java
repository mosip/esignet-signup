package io.mosip.testrig.apirig.signup.testscripts;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.signup.utils.SignupConfigManager;
import io.mosip.testrig.apirig.signup.utils.SignupUtil;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.restassured.response.Response;

public class SimplePostForAutoGenId extends SignupUtil implements ITest {
	private static final Logger logger = Logger.getLogger(SimplePostForAutoGenId.class);
	protected String testCaseName = "";
	public String idKeyName = null;
	public Response response = null;
	public boolean sendEsignetToken = false;
	public boolean auditLogCheck = false;

	@BeforeClass
	public static void setLogLevel() {
		if (SignupConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	/**
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		sendEsignetToken = context.getCurrentXmlTest().getLocalParameters().containsKey("sendEsignetToken");
		idKeyName = context.getCurrentXmlTest().getLocalParameters().get("idKeyName");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}

	/**
	 * Test method for OTP Generation execution
	 * 
	 * @param objTestParameters
	 * @param testScenario
	 * @param testcaseName
	 * @throws AuthenticationTestException
	 * @throws AdminTestException
	 * @throws NoSuchAlgorithmException
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO)
			throws AuthenticationTestException, AdminTestException, NoSuchAlgorithmException {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseDTO = SignupUtil.isTestCaseValidForTheExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}
		if (testCaseDTO.getTestCaseName().contains("VID") || testCaseDTO.getTestCaseName().contains("Vid")) {
			if (!BaseTestCase.getSupportedIdTypesValue().contains("VID")
					&& !BaseTestCase.getSupportedIdTypesValue().contains("vid")) {
				throw new SkipException(GlobalConstants.VID_FEATURE_NOT_SUPPORTED);
			}
		}
		String[] templateFields = testCaseDTO.getTemplateFields();
		String inputJson = "";

		if (BaseTestCase.currentModule.equals(GlobalConstants.MASTERDATA)
				&& testCaseName.startsWith("Esignet_CreateOIDCClient")) {
			inputJson = testCaseDTO.getInput();
		} else {
			inputJson = getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate());
		}

		String outputJson = getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate());

		if (testCaseDTO.getTemplateFields() != null && templateFields.length > 0) {
			ArrayList<JSONObject> inputtestCases = AdminTestUtil.getInputTestCase(testCaseDTO);
			ArrayList<JSONObject> outputtestcase = AdminTestUtil.getOutputTestCase(testCaseDTO);
			for (int i = 0; i < languageList.size(); i++) {
				response = postWithBodyAndCookieForAutoGeneratedId(ApplnURI + testCaseDTO.getEndPoint(),
						getJsonFromTemplate(inputtestCases.get(i).toString(), testCaseDTO.getInputTemplate()),
						COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), idKeyName);

				Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(
						response.asString(),
						getJsonFromTemplate(outputtestcase.get(i).toString(), testCaseDTO.getOutputTemplate()),
						testCaseDTO, response.getStatusCode());
				if (testCaseDTO.getTestCaseName().toLowerCase().contains("dynamic")) {
					JSONObject json = new JSONObject(response.asString());
					idField = json.getJSONObject("response").get("id").toString();
				}
				Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));

				if (!OutputValidationUtil.publishOutputResult(ouputValid))
					throw new AdminTestException("Failed at output validation");
			}
		} else {
			inputJson = SignupUtil.inputstringKeyWordHandeler(inputJson, testCaseName);
			if (testCaseName.contains("ESignet_")) {
				if (SignupConfigManager.isInServiceNotDeployedList(GlobalConstants.ESIGNET)) {
					throw new SkipException("esignet is not deployed hence skipping the testcase");
				}

				String tempUrl = null;
				if (testCaseDTO.getEndPoint().contains("/signup/")) {
					tempUrl = SignupConfigManager.getSignupBaseUrl();
				} else {
					tempUrl = SignupConfigManager.getEsignetBaseUrl();
				}

				if ((testCaseName.contains("_AuthorizationCode_")) || (testCaseName.contains("_AuthToken_Xsrf_"))
						|| (testCaseName.contains("_OAuthDetailsRequest"))) {
					response = postRequestWithCookieAuthHeadersAndXsrfTokenForAutoGenId(
							tempUrl + testCaseDTO.getEndPoint(), inputJson, COOKIENAME, testCaseDTO.getTestCaseName(),
							idKeyName);
				} else {
					if (SignupUtil.getIdentityPluginNameFromEsignetActuator().toLowerCase()
							.contains("mockauthenticationservice") == true) {
						inputJson = inputJsonKeyWordHandeler(inputJson, testCaseName);
						response = SignupUtil.postWithBodyAndBearerToken(tempUrl + testCaseDTO.getEndPoint(), inputJson,
								COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), idKeyName);
						if (testCaseName.toLowerCase().contains("_sid")) {
							writeAutoGeneratedId(testCaseName, idKeyName, new JSONObject(response.getBody().asString())
									.getJSONObject(GlobalConstants.RESPONSE).getString(idKeyName).toString());
						}
					} else {
						response = postWithBodyAndBearerTokenForAutoGeneratedId(tempUrl + testCaseDTO.getEndPoint(),
								inputJson, COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), idKeyName);
					}

				}
			} else {
				if (testCaseDTO.getTestCaseName().equals("Signup_Esignet_CreateOIDCClient_all_Valid_Smoke_sid")
						&& SignupUtil.getIdentityPluginNameFromEsignetActuator().toLowerCase()
								.contains("idaauthenticatorimpl")) {
					testCaseDTO.setTestCaseName("Signup_ESignet_CreateOIDCClient_all_Valid_Smoke_sid");
				}
				response = postWithBodyAndCookieForAutoGeneratedId(ApplnURI + testCaseDTO.getEndPoint(), inputJson,
						auditLogCheck, COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), idKeyName,
						sendEsignetToken);
			}

			Map<String, List<OutputValidationDto>> ouputValid = null;
			if (testCaseName.contains("_StatusCode")) {

				OutputValidationDto customResponse = customStatusCodeResponse(String.valueOf(response.getStatusCode()),
						testCaseDTO.getOutput());

				ouputValid = new HashMap<>();
				ouputValid.put(GlobalConstants.EXPECTED_VS_ACTUAL, List.of(customResponse));
			} else {
				ouputValid = OutputValidationUtil.doJsonOutputValidation(response.asString(),
						getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()), testCaseDTO,
						response.getStatusCode());
			}
			Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));
			if (!OutputValidationUtil.publishOutputResult(ouputValid))
				throw new AdminTestException("Failed at output validation");
		}

	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}
}
