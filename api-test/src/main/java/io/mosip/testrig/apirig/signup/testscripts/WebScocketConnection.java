package io.mosip.testrig.apirig.signup.testscripts;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
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
import io.mosip.testrig.apirig.utils.WebSocketClientUtil;
import io.restassured.response.Response;

public class WebScocketConnection extends AdminTestUtil implements ITest {
	//private static final Logger logger = Logger.getLogger(WebScocketConnection.class);
	private static final Logger logger = Logger.getLogger(WebScocketConnection.class);
	protected String testCaseName = "";
	public String idKeyName = null;
	public Response response = null;
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
		
		
		testCaseName = isTestCaseValidForExecution(testCaseDTO);

		String inputJson = testCaseDTO.getInput().toString();
		inputJson = inputJsonKeyWordHandeler(inputJson, testCaseName);

		auditLogCheck = testCaseDTO.isAuditLogCheck();
		
		String slotId = null;		
		String idvSlotAllotted = null;
		String sendDestination = null;
		String subscribeDestination = null;
		String message1 = null;
		String message2 = null;
		
		
		JSONObject webSocketReqJson = new JSONObject(inputJson);
		slotId = webSocketReqJson.getString("slotId");
		webSocketReqJson.remove("slotId");
		idvSlotAllotted = webSocketReqJson.getString("idvSlotAllotted");
		webSocketReqJson.remove("idvSlotAllotted");
		sendDestination = webSocketReqJson.getString("sendDestination");
		webSocketReqJson.remove("sendDestination");
		
		message1 = webSocketReqJson.get("message1").toString();
		message2 = webSocketReqJson.get("message2").toString();
		
		webSocketReqJson.remove("message1");
		subscribeDestination = "/topic/" + slotId;

		String tempUrl = SignupConfigManager.getEsignetBaseUrl();
		if (testCaseDTO.getEndPoint().contains("/signup/")) {
			tempUrl = SignupConfigManager.getSignupBaseUrl();
		}
		
		tempUrl = tempUrl.replace("https", "wss") + testCaseDTO.getEndPoint();
		
		WebSocketClientUtil webSocketClient = new WebSocketClientUtil(slotId, idvSlotAllotted, subscribeDestination, sendDestination);
        
        // Connect to WebSocket server
        webSocketClient.connect(tempUrl);
        
        // Send a message
        webSocketClient.sendMessage(message1);
        webSocketClient.sendMessage(message2);
        
        // Wait for messages or other tasks, then close connection when done
        // This is just an example; add logic for your specific timing
        try {
            Thread.sleep(5000); // Wait 5 seconds for demonstration purposes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println(WebSocketClientUtil.getMessageStore());
        
        // Close the connection
        webSocketClient.closeConnection();
        System.out.println("Connection closed.");

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

	@AfterClass(alwaysRun = true)
	public void waittime() {
		try {
			if (SignupUtil.getIdentityPluginNameFromEsignetActuator().toLowerCase()
					.contains("mockauthenticationservice") == false) {
				if (!testCaseName.contains(GlobalConstants.ESIGNET_)) {
					long delayTime = Long.parseLong(properties.getProperty("Delaytime"));
					logger.info("waiting for " + delayTime + " mili secs after VID Generation In RESIDENT SERVICES");
					Thread.sleep(delayTime);
				}
			}

		} catch (Exception e) {
			logger.error("Exception : " + e.getMessage());
			Thread.currentThread().interrupt();
		}

	}
}
