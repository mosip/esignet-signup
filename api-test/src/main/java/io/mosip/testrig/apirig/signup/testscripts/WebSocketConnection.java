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

	@Override
	public String getTestName() {
		return testCaseName;
	}

	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		idKeyName = context.getCurrentXmlTest().getLocalParameters().get("idKeyName");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}

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
		// "rawMessage1": raw string sent verbatim instead of message1, for payloads that aren't valid JSON.
		String rawMessage1 = webSocketReqJson.optString("rawMessage1", null);
		// "steps": ordered list of independent frame-validation checks run over one connection.
		JSONArray steps = webSocketReqJson.optJSONArray("steps");
		// "reconnectSameSlot": after sending message1 and closing, reconnect to the same slotId and assert rejection.
		boolean reconnectSameSlot = webSocketReqJson.optBoolean("reconnectSameSlot", false);
		// "contentType": sent as message1's STOMP content-type header instead of application/json.
		String contentType = webSocketReqJson.optString("contentType", null);
		// "concurrentSubscription": open a second connection to the same slotId while the first stays open.
		boolean concurrentSubscription = webSocketReqJson.optBoolean("concurrentSubscription", false);
		// "abnormalClose": close with a non-NORMAL code so the server marks the transaction FAILED.
		boolean abnormalClose = webSocketReqJson.optBoolean("abnormalClose", false);
		// "subscribeSlotId": overrides the STOMP SUBSCRIBE destination, independent of the connect slotId.
		String subscribeSlotId = webSocketReqJson.optString("subscribeSlotId", null);
		// "expectSubscriptionRejected": assert the SUBSCRIBE was rejected with an invalid_slot_id ERROR frame.
		boolean expectSubscriptionRejected = webSocketReqJson.optBoolean("expectSubscriptionRejected", false);
		webSocketReqJson.remove("message1");
		webSocketReqJson.remove("message2");
		webSocketReqJson.remove("rawMessage1");
		webSocketReqJson.remove("steps");
		webSocketReqJson.remove("reconnectSameSlot");
		webSocketReqJson.remove("contentType");
		webSocketReqJson.remove("concurrentSubscription");
		webSocketReqJson.remove("abnormalClose");
		webSocketReqJson.remove("subscribeSlotId");
		webSocketReqJson.remove("expectSubscriptionRejected");

		String slotId = webSocketReqJson.getString("slotId");
		String idvSlotAllotted = webSocketReqJson.getString("idvSlotAllotted");
		String cookie = GlobalConstants.IDV_SLOT_ALLOTED_KEY + idvSlotAllotted;
		String sendDestination = webSocketReqJson.getString("sendDestination");
		String subscribeDestination = "/topic/" + (subscribeSlotId != null ? subscribeSlotId : slotId);

		String tempUrl = SignupConfigManager.getEsignetBaseUrl();
		if (testCaseDTO.getEndPoint().contains("/signup/")) {
			tempUrl = SignupConfigManager.getSignupBaseUrl();
		}

		tempUrl = tempUrl.replace("https", "wss") + testCaseDTO.getEndPoint() + "?slotId=" + slotId;

		SignupCustomWebSocketClientUtil webSocketClient = new SignupCustomWebSocketClientUtil(cookie, subscribeDestination, sendDestination);

		if (expectSubscriptionRejected) {
			clearErrorFrames();
		}

		webSocketClient.connect(tempUrl);

		if (steps != null) {
			assertWebSocketStepsOutcome(webSocketClient, slotId, tempUrl, steps);
			return;
		}

		if (reconnectSameSlot) {
			assertSlotReconnectionRejected(webSocketClient, message1, tempUrl, cookie, subscribeDestination,
					sendDestination);
			return;
		}

		if (concurrentSubscription) {
			assertConcurrentSubscriptionRejected(webSocketClient, tempUrl, cookie, subscribeDestination,
					sendDestination);
			return;
		}

		if (expectSubscriptionRejected) {
			assertSubscriptionRejected(webSocketClient, tempUrl);
			return;
		}

		if (abnormalClose) {
			assertAbnormalCloseHandled(webSocketClient, message1, tempUrl);
			return;
		}

		String messageToSend = rawMessage1 != null ? rawMessage1 : message1;
		if (contentType != null) {
			webSocketClient.sendMessage(messageToSend, contentType);
		} else {
			webSocketClient.sendMessage(messageToSend);
		}

		// Negative/edge tests declare expectConnectionFailure/expectedFeedbackCodes in output; positive tests declare neither and fall through below.
		String outputStr = testCaseDTO.getOutput();
		JSONObject expectedOutcome = new JSONObject(outputStr == null || outputStr.trim().isEmpty() ? "{}" : outputStr);
		if (expectedOutcome.has("expectedFeedbackCodes") || expectedOutcome.has("expectConnectionFailure")) {
			assertWebSocketOutcome(webSocketClient, slotId, tempUrl, expectedOutcome);
			return;
		}

		if (expectedOutcome.has("expectedStepDetails")) {
			assertStepDetails(webSocketClient, slotId, tempUrl, expectedOutcome.getJSONObject("expectedStepDetails"));
			return;
		}

		int order = 1;
		String typeValue = "START";
		JSONObject messageObject = new JSONObject(message2);
		
		// "_Incomplete" test names simulate an abrupt disconnect: skip the loop below, leaving the session abandoned without a close handshake.
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
						Thread.sleep(3000);
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

	// The message store keeps only the most recent frame per slot, so only message1 is sent here - a follow-up frame would overwrite the error being observed.
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

		closeSessionQuietly(wsSession, "websocket");

		GlobalMethods.reportResponse(null, tempUrl,
				"Expected feedback codes: " + expectedCodes + ", received: " + actualCodes, true);

		if (!actualCodes.equals(expectedCodes)) {
			throw new AdminTestException(
					"Expected websocket feedback codes " + expectedCodes + ", but received " + actualCodes);
		}
	}

	private void assertStepDetails(SignupCustomWebSocketClientUtil webSocketClient, String slotId, String tempUrl,
			JSONObject expectedStepDetails) throws AdminTestException, InterruptedException {

		Session wsSession = webSocketClient.getSession();
		if (wsSession == null) {
			throw new AdminTestException("Expected the WebSocket connection to be established, but it was not");
		}

		JSONObject step = pollStepDetails(slotId);

		closeSessionQuietly(wsSession, "websocket");

		if (step == null) {
			throw new AdminTestException(
					"Expected step details to be published to /topic/" + slotId + ", but none were received");
		}

		GlobalMethods.reportResponse(null, tempUrl, "Received step details: " + step, true);

		if (expectedStepDetails.has("durationInSecondsGreaterThan")) {
			int minExclusive = expectedStepDetails.getInt("durationInSecondsGreaterThan");
			if (!step.has("durationInSeconds")) {
				throw new AdminTestException("Expected step.durationInSeconds to be present, but it was missing");
			}
			int durationInSeconds = step.getInt("durationInSeconds");
			if (durationInSeconds <= minExclusive) {
				throw new AdminTestException("Expected step.durationInSeconds to be greater than " + minExclusive
						+ ", but received " + durationInSeconds);
			}
		}

		if (expectedStepDetails.optBoolean("retryableErrorCodesValid", false)) {
			if (!step.has("retryableErrorCodes") || step.isNull("retryableErrorCodes")) {
				throw new AdminTestException(
						"Expected step.retryableErrorCodes to be present, but it was missing/null");
			}
			JSONArray retryableErrorCodes = step.getJSONArray("retryableErrorCodes");
			for (int i = 0; i < retryableErrorCodes.length(); i++) {
				String code = retryableErrorCodes.getString(i);
				if (code == null || code.isBlank()) {
					throw new AdminTestException(
							"Expected every step.retryableErrorCodes entry to be a non-blank string, but found: "
									+ code);
				}
			}
		}

		if (expectedStepDetails.has("startupDelayInSecondsAtLeast")) {
			int minInclusive = expectedStepDetails.getInt("startupDelayInSecondsAtLeast");
			if (!step.has("startupDelayInSeconds")) {
				throw new AdminTestException("Expected step.startupDelayInSeconds to be present, but it was missing");
			}
			int startupDelayInSeconds = step.getInt("startupDelayInSeconds");
			if (startupDelayInSeconds < minInclusive) {
				throw new AdminTestException("Expected step.startupDelayInSeconds to be at least " + minInclusive
						+ ", but received " + startupDelayInSeconds);
			}
		}
	}

	// Each step's frames are cleared from the message store after asserting it, so the next step's feedback can't be lost to or confused with a prior step's.
	private void assertWebSocketStepsOutcome(SignupCustomWebSocketClientUtil webSocketClient, String slotId,
			String tempUrl, JSONArray steps) throws AdminTestException, InterruptedException {

		for (int i = 0; i < steps.length(); i++) {
			JSONObject step = steps.getJSONObject(i);
			String rawMessage = step.getString("rawMessage");

			Set<String> expectedCodes = new LinkedHashSet<>();
			JSONArray expectedArray = step.optJSONArray("expectedFeedbackCodes");
			if (expectedArray != null) {
				for (int j = 0; j < expectedArray.length(); j++) {
					expectedCodes.add(expectedArray.getString(j));
				}
			}

			webSocketClient.sendMessage(rawMessage);
			Set<String> actualCodes = pollFeedbackCodes(slotId, expectedCodes);
			clearFeedbackForSlot(slotId);

			GlobalMethods.reportResponse(null, tempUrl, "Step " + (i + 1) + " - expected feedback codes: "
					+ expectedCodes + ", received: " + actualCodes, true);

			if (!actualCodes.equals(expectedCodes)) {
				throw new AdminTestException("Step " + (i + 1) + ": expected websocket feedback codes "
						+ expectedCodes + ", but received " + actualCodes);
			}
		}

		Session wsSession = webSocketClient.getSession();
		if (wsSession != null) {
			closeSessionQuietly(wsSession, "websocket");
		}
	}

	// Slots are single-use: verifies reconnecting to the same slotId after the first connection closes is rejected.
	private void assertSlotReconnectionRejected(SignupCustomWebSocketClientUtil firstClient, String message1,
			String tempUrl, String cookie, String subscribeDestination, String sendDestination)
			throws AdminTestException {

		firstClient.sendMessage(message1);

		Session firstSession = firstClient.getSession();
		if (firstSession == null) {
			throw new AdminTestException(
					"Expected the first WebSocket connection to be established, but it was not");
		}
		closeSessionQuietly(firstSession, "first websocket");

		SignupCustomWebSocketClientUtil secondClient = new SignupCustomWebSocketClientUtil(cookie,
				subscribeDestination, sendDestination);
		secondClient.connect(tempUrl);
		Session secondSession = secondClient.getSession();

		if (secondSession != null) {
			closeSessionQuietly(secondSession, "second websocket");
			throw new AdminTestException(
					"Expected reconnection with the same slotId to be rejected, but a second session was established");
		}

		GlobalMethods.reportResponse(null, tempUrl, "Reconnection with the same slotId was rejected as expected",
				true);
	}

	private void assertConcurrentSubscriptionRejected(SignupCustomWebSocketClientUtil firstClient, String tempUrl,
			String cookie, String subscribeDestination, String sendDestination) throws AdminTestException {

		Session firstSession = firstClient.getSession();
		if (firstSession == null) {
			throw new AdminTestException("Expected the first WebSocket connection to be established, but it was not");
		}

		SignupCustomWebSocketClientUtil secondClient = new SignupCustomWebSocketClientUtil(cookie,
				subscribeDestination, sendDestination);
		secondClient.connect(tempUrl);
		Session secondSession = secondClient.getSession();

		closeSessionQuietly(firstSession, "first websocket");
		if (secondSession != null) {
			closeSessionQuietly(secondSession, "second websocket");
			throw new AdminTestException(
					"Expected a second concurrent connection to the same slotId to be rejected, but a second session was established");
		}

		GlobalMethods.reportResponse(null, tempUrl,
				"Second concurrent connection to the same slotId was rejected as expected", true);
	}

	// WebSocketController#onDisconnected marks the transaction FAILED for any non-NORMAL close code; the sleep lets that handling finish before a dependent test checks status.
	private void assertAbnormalCloseHandled(SignupCustomWebSocketClientUtil webSocketClient, String message1,
			String tempUrl) throws AdminTestException {

		webSocketClient.sendMessage(message1);

		Session wsSession = webSocketClient.getSession();
		if (wsSession == null) {
			throw new AdminTestException("Expected the WebSocket connection to be established, but it was not");
		}

		try {
			wsSession.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Simulated abnormal disconnect"));
		} catch (Exception e) {
			logger.error("Failed to close the websocket session with an abnormal close code", e);
			throw new AdminTestException(
					"Failed to close the websocket session with an abnormal close code: " + e);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		GlobalMethods.reportResponse(null, tempUrl,
				"WebSocket session closed with an abnormal close code (GOING_AWAY) to simulate a failed verification",
				true);
	}

	// WebSocketChannelInterceptor validates the SUBSCRIBE destination independently of the handshake, so a valid slotId can still connect while an unrecognized subscribeSlotId gets rejected.
	private void assertSubscriptionRejected(SignupCustomWebSocketClientUtil webSocketClient, String tempUrl)
			throws AdminTestException, InterruptedException {

		Session wsSession = webSocketClient.getSession();
		if (wsSession == null) {
			throw new AdminTestException("Expected the WebSocket connection to be established, but it was not");
		}

		String errorFrame = pollErrorFrame();

		closeSessionQuietly(wsSession, "websocket");

		if (errorFrame == null) {
			throw new AdminTestException("Expected the subscription to an unrecognized slotId to be rejected "
					+ "with a STOMP ERROR frame, but none was received");
		}

		GlobalMethods.reportResponse(null, tempUrl, "Received STOMP ERROR frame: " + errorFrame, true);

		if (!errorFrame.contains("invalid_slot_id")) {
			throw new AdminTestException(
					"Expected the STOMP ERROR frame to reference invalid_slot_id, but received: " + errorFrame);
		}
	}

	// ERROR frames are keyed distinctly from MESSAGE frames - see WebSocketClientUtil#extractMessageId.
	private String pollErrorFrame() throws InterruptedException {
		long deadline = System.currentTimeMillis() + MAX_FEEDBACK_WAIT_MS;
		String errorFrame = collectErrorFrame();
		while (errorFrame == null && System.currentTimeMillis() < deadline) {
			Thread.sleep(FEEDBACK_POLL_INTERVAL_MS);
			errorFrame = collectErrorFrame();
		}
		return errorFrame;
	}

	private String collectErrorFrame() {
		for (Map.Entry<String, String> entry : SignupCustomWebSocketClientUtil.getMessageStore().entrySet()) {
			if (entry.getKey() != null && entry.getKey().startsWith("ERROR-")) {
				return entry.getValue();
			}
		}
		return null;
	}

	private void clearErrorFrames() {
		SignupCustomWebSocketClientUtil.getMessageStore().keySet().removeIf(key -> key != null && key.startsWith("ERROR-"));
	}

	// Logs rather than throws on failure - a close error at teardown time should never fail the test itself.
	private void closeSessionQuietly(Session session, String label) {
		try {
			session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
		} catch (Exception e) {
			logger.info("Error closing " + label + " session: " + e.getMessage());
		}
	}

	// Scoped to this slot's own topic (not a global clear) to avoid interfering with other tests sharing the message store.
	private void clearFeedbackForSlot(String slotId) {
		SignupCustomWebSocketClientUtil.getMessageStore().entrySet()
				.removeIf(e -> e.getValue() != null && e.getValue().contains("/topic/" + slotId));
	}

	// When no codes are expected (asserting absence) this always waits the full window so any erroneous feedback has time to surface.
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

	// Only feedback.type == ERROR is considered - the plugin also publishes non-error MESSAGE/COLOR hints (e.g. "turn_left") that must be ignored.
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

	private JSONObject pollStepDetails(String slotId) throws InterruptedException {
		long deadline = System.currentTimeMillis() + MAX_FEEDBACK_WAIT_MS;
		JSONObject step = collectStepDetailsForSlot(slotId);
		while (step == null && System.currentTimeMillis() < deadline) {
			Thread.sleep(FEEDBACK_POLL_INTERVAL_MS);
			step = collectStepDetailsForSlot(slotId);
		}
		return step;
	}

	private JSONObject collectStepDetailsForSlot(String slotId) {
		for (String rawFrame : SignupCustomWebSocketClientUtil.getMessageStore().values()) {
			if (rawFrame == null || !rawFrame.contains("/topic/" + slotId)
					|| !rawFrame.contains("{") || !rawFrame.contains("}")) {
				continue;
			}
			try {
				String jsonBody = rawFrame.substring(rawFrame.indexOf("{"), rawFrame.lastIndexOf("}") + 1);
				JSONObject body = new JSONObject(jsonBody);
				if (body.has("step") && !body.isNull("step")) {
					return body.getJSONObject("step");
				}
			} catch (Exception e) {
				logger.info("Skipping unparseable websocket frame while looking for step details: " + e.getMessage());
			}
		}
		return null;
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
