package io.mosip.testrig.apirig.signup.testscripts;

import java.lang.reflect.Field;
import java.util.Map;

import javax.websocket.Session;

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


import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.signup.utils.SignupConfigManager;
import io.mosip.testrig.apirig.signup.utils.SignupUtil;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.WebSocketClientUtil;
import io.restassured.response.Response;

public class WebScocketConnection extends AdminTestUtil implements ITest {
	//private static final Logger logger = Logger.getLogger(WebScocketConnection.class);
	private static final Logger logger = Logger.getLogger(WebScocketConnection.class);
	protected String testCaseName = "";
	public String idKeyName = null;
	public Response response = null;
	public boolean auditLogCheck = false;
	private boolean sendWebsocketMessage = true;
	private Session session;

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
	 * @throws InterruptedException
	 * @throws NumberFormatException
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO)
			throws AuthenticationTestException, AdminTestException, NumberFormatException, InterruptedException {

		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = SignupUtil.isTestCaseValidForExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}

		String inputJson = getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate());
		inputJson = inputJsonKeyWordHandeler(inputJson, testCaseName);

		auditLogCheck = testCaseDTO.isAuditLogCheck();

		JSONObject webSocketReqJson = new JSONObject(inputJson);
		String message1 = webSocketReqJson.get("message1").toString();
		String message2 = webSocketReqJson.get("message2").toString();
		webSocketReqJson.remove("message1");
		webSocketReqJson.remove("message2");

		String slotId = webSocketReqJson.getString("slotId");
		String idvSlotAllotted = webSocketReqJson.getString("idvSlotAllotted");
		String cookie = GlobalConstants.IDV_SLOT_ALLOTED_KEY + idvSlotAllotted;
		String sendDestination = webSocketReqJson.getString("sendDestination");
		String subscribeDestination = "/topic/" + slotId;

		String tempUrl = SignupConfigManager.getEsignetBaseUrl();
		if (testCaseDTO.getEndPoint().contains("/signup/")) {
			tempUrl = SignupConfigManager.getSignupBaseUrl();
		}

		tempUrl = tempUrl.replace("https", "wss") + testCaseDTO.getEndPoint() + "?slotId=" + slotId;

		WebSocketClientUtil webSocketClient = new WebSocketClientUtil(cookie, subscribeDestination, sendDestination);

		// Connect to WebSocket server
		webSocketClient.connect(tempUrl);

		// Send a message
		webSocketClient.sendMessage(message1);

		int order = 1;
		String typeValue = "START";
		JSONObject messageObject = new JSONObject(message2);

		try {

			while (sendWebsocketMessage && order < 15) {

				Session session = webSocketClient.getSession();
				GlobalMethods.reportRequest(webSocketReqJson.toString(), messageObject.toString(), tempUrl);

				if (!(session == null) && !typeValue.equals("END")) {
					messageObject.getJSONArray("frames").getJSONObject(0).put("order", String.valueOf(order));
					webSocketClient.sendMessage(messageObject.toString());

					try {
						Thread.sleep(3000); // Wait 5 seconds for demonstration purposes
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					Map<String, String> receivedMessage = WebSocketClientUtil.getMessageStore();

					String completeMessage = receivedMessage.values().stream().reduce((a, b) -> a + "\n" + b)
							.orElse("");

					String jsonPayload = completeMessage.substring(completeMessage.indexOf("{"));
					typeValue = SignupUtil.getTypeValueFromWebSocketMessage(jsonPayload);

					GlobalMethods.reportResponse(session.toString(), tempUrl, jsonPayload, true);

					order++;
				} else {
					sendWebsocketMessage = false;
					if (session == null) {
						String webSocketConnectionError = "WebSocket connection is not active, either not created or closed abnormally";
						logger.info(webSocketConnectionError);
						GlobalMethods.reportResponse(null, tempUrl, webSocketConnectionError, true);
						throw new AdminTestException("Failed due to " + webSocketConnectionError);
					}
				}
			}

		} catch (Exception e) {
			throw new AdminTestException("Failed at sending message to websocket");
		}

		// Close the connection
		if (!(session == null)) {
			webSocketClient.closeConnection();
			System.out.println("Connection closed.");
		}

	}

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
