package io.mosip.testrig.apirig.signup.testscripts;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.signup.utils.SignupConfigManager;
import io.mosip.testrig.apirig.signup.utils.SignupCustomWebSocketClientUtil;
import io.mosip.testrig.apirig.signup.utils.SignupUtil;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.restassured.response.Response;

public class WebSocketConnection extends SignupUtil implements ITest {
	private static final Logger logger = Logger.getLogger(WebSocketConnection.class);
	protected String testCaseName = "";
	public String idKeyName = null;
	public Response response = null;
	public boolean auditLogCheck = false;
	private boolean sendWebsocketMessage = true;
	private Session session;
	private static final long MAX_FEEDBACK_WAIT_MS = 3000;
	private static final long FEEDBACK_POLL_INTERVAL_MS = 100;

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
			throws AuthenticationTestException, AdminTestException, NumberFormatException, InterruptedException, SecurityXSSException {

		testCaseName = testCaseDTO.getTestCaseName();
		testCaseDTO = SignupUtil.isTestCaseValidForTheExecution(testCaseDTO);
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

		SignupCustomWebSocketClientUtil webSocketClient = new SignupCustomWebSocketClientUtil(cookie, subscribeDestination, sendDestination);
		

		// Connect to WebSocket server
		webSocketClient.connect(tempUrl);

		// Send a message
		webSocketClient.sendMessage(message1);

		// Data-driven assertion path: a test declares its expected websocket outcome in its output
		// (expectConnectionFailure and/or expectedFeedbackCodes). One logic handles every negative
		// and edge case; positive full-flow tests declare neither and fall through to the frame loop.
		String outputStr = testCaseDTO.getOutput();
		JSONObject expectedOutcome = new JSONObject(outputStr == null || outputStr.trim().isEmpty() ? "{}" : outputStr);
		if (expectedOutcome.has("expectedFeedbackCodes") || expectedOutcome.has("expectConnectionFailure")) {
			assertWebSocketOutcome(webSocketClient, slotId, tempUrl, expectedOutcome);
			return;
		}

		int order = 1;
		String typeValue = "START";
		JSONObject messageObject = new JSONObject(message2);
		
		if (testCaseName.contains("_Incomplete")) {
			sendWebsocketMessage = false;
		}

		try {

			while (sendWebsocketMessage && order < 15) {

				Session session = webSocketClient.getSession();

				if (!(session == null) && !typeValue.equals("END")) {
					GlobalMethods.reportRequest(webSocketReqJson.toString(), messageObject.toString(), tempUrl);
					messageObject.getJSONArray("frames").getJSONObject(0).put("order", String.valueOf(order));
					webSocketClient.sendMessage(messageObject.toString());

					try {
						Thread.sleep(3000); // Wait 5 seconds for demonstration purposes
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					Map<String, String> receivedMessage = SignupCustomWebSocketClientUtil.getMessageStore();

					String completeMessage = receivedMessage.values().stream().reduce((a, b) -> a + "\n" + b)
							.orElse("");

					String jsonPayload = completeMessage.substring(completeMessage.indexOf("{"));
					typeValue = SignupUtil.extractCodeById(jsonPayload, slotId);

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
					session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
					session = null;
				}
			}

		} catch (Exception e) {
			throw new AdminTestException("Failed at sending message to websocket");
		}

	}

	/**
	 * Single data-driven assertion for every websocket negative/edge test. The expected outcome is
	 * declared in the test's output JSON, so new scenarios need YAML only, no code changes:
	 *   { "expectConnectionFailure": true }   -> the handshake must not establish
	 *   { "expectedFeedbackCodes": [ ... ] }  -> the set of ERROR feedback codes published to
	 *                                            /topic/{slotId} must exactly equal this list
	 *                                            (an empty list asserts that no error is published).
	 * The single process-frame message (message1) has already been sent by the caller. Only that one
	 * frame is sent: the client's message store keeps just the most recent frame per slot, so sending
	 * a follow-up valid frame would overwrite the error we need to observe.
	 */
	private void assertWebSocketOutcome(SignupCustomWebSocketClientUtil webSocketClient, String slotId,
			String tempUrl, JSONObject expectedOutcome)
			throws AdminTestException, InterruptedException {

		Session wsSession = webSocketClient.getSession();

		if (expectedOutcome.optBoolean("expectConnectionFailure", false)) {
			if (wsSession != null) {
				throw new AdminTestException(
						"Expected the WebSocket connection to fail, but it was established");
			}
			GlobalMethods.reportResponse(null, tempUrl, "WebSocket connection failed as expected", true);
			return;
		}

		if (wsSession == null) {
			throw new AdminTestException(
					"Expected the WebSocket connection to be established, but it was not");
		}

		Set<String> expectedCodes = new LinkedHashSet<>();
		JSONArray expectedArray = expectedOutcome.optJSONArray("expectedFeedbackCodes");
		if (expectedArray != null) {
			for (int i = 0; i < expectedArray.length(); i++) {
				expectedCodes.add(expectedArray.getString(i));
			}
		}

		Set<String> actualCodes = pollFeedbackCodes(slotId, expectedCodes);

		try {
			wsSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
		} catch (Exception e) {
			logger.info("Error closing websocket session: " + e.getMessage());
		}

		GlobalMethods.reportResponse(null, tempUrl,
				"Expected feedback codes: " + expectedCodes + ", received: " + actualCodes, true);

		if (!actualCodes.equals(expectedCodes)) {
			throw new AdminTestException(
					"Expected websocket feedback codes " + expectedCodes + ", but received " + actualCodes);
		}
	}

	/**
	 * Polls the received-message store for this slot's feedback codes, returning as soon as every
	 * expected code has arrived, or after MAX_FEEDBACK_WAIT_MS. When no codes are expected (asserting
	 * absence) it always waits the full window so any erroneous feedback has time to surface.
	 */
	private Set<String> pollFeedbackCodes(String slotId, Set<String> expectedCodes) throws InterruptedException {
		long deadline = System.currentTimeMillis() + MAX_FEEDBACK_WAIT_MS;
		Set<String> actualCodes = collectFeedbackCodesForSlot(slotId);
		while (System.currentTimeMillis() < deadline
				&& (expectedCodes.isEmpty() || !actualCodes.containsAll(expectedCodes))) {
			Thread.sleep(FEEDBACK_POLL_INTERVAL_MS);
			actualCodes = collectFeedbackCodesForSlot(slotId);
		}
		return actualCodes;
	}

	/**
	 * Collects the distinct ERROR feedback codes published to this slot's /topic/{slotId} in the
	 * received-message store (slotId is a unique hash, so no cross-test collision). Only
	 * feedback.type == ERROR is considered - the verification plugin also publishes non-error
	 * MESSAGE/COLOR hints (e.g. "turn_left") that are not validation failures and must be ignored.
	 */
	private Set<String> collectFeedbackCodesForSlot(String slotId) {
		Set<String> codes = new LinkedHashSet<>();
		for (String rawFrame : SignupCustomWebSocketClientUtil.getMessageStore().values()) {
			if (rawFrame == null || !rawFrame.contains("/topic/" + slotId)
					|| !rawFrame.contains("{") || !rawFrame.contains("}")) {
				continue;
			}
			try {
				String jsonBody = rawFrame.substring(rawFrame.indexOf("{"), rawFrame.lastIndexOf("}") + 1);
				JSONObject body = new JSONObject(jsonBody);
				if (body.has("feedback") && !body.isNull("feedback")) {
					JSONObject feedback = body.getJSONObject("feedback");
					if ("ERROR".equals(feedback.optString("type")) && feedback.has("code")) {
						codes.add(feedback.getString("code"));
					}
				}
			} catch (Exception e) {
				logger.info("Skipping unparseable websocket frame while looking for feedback code: " + e.getMessage());
			}
		}
		return codes;
	}

	/**
	 * The method ser current test name to result
	 *
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}
}
