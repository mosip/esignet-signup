package stepdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import base.BaseTest;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.TransactionTimeoutPopupPage;
import utils.ForcedApiResponseInterceptor;
import utils.LocaleTextUtil;

// Steps for "Setup account after the signup transaction has expired".
// A real expiry takes ~5 minutes, so verify-challenge is faked to return invalid_transaction instead.
public class TransactionTimeoutStepDef {

	private static final String GENERATE_CHALLENGE_API = "/registration/generate-challenge";
	private static final String VERIFY_CHALLENGE_API = "/registration/verify-challenge";
	// Stubbed to succeed so the OTP screen is reached without depending on real SMS delivery
	private static final String GENERATE_CHALLENGE_SUCCESS_BODY =
			"{\"response\":{\"status\":\"SUCCESS\"},\"errors\":[]}";
	private static final String INVALID_TRANSACTION_BODY =
			"{\"response\":null,\"errors\":[{\"errorCode\":\"invalid_transaction\",\"errorMessage\":\"invalid transaction\"}]}";

	private static final String TITLE_KEY = "error";
	private static final String MESSAGE_KEY = "error_response.invalid_transaction";
	private static final String OKAY_KEY = "okay";

	private final WebDriver driver;
	private final TransactionTimeoutPopupPage popupPage;
	private ForcedApiResponseInterceptor interceptor;

	public TransactionTimeoutStepDef(BaseTest baseTest) {
		this.driver = BaseTest.getDriver();
		this.popupPage = new TransactionTimeoutPopupPage(driver);
	}

	@Given("the signup transaction is forced to expire on OTP verification")
	public void forceTransactionToExpireOnVerify() {
		Map<String, String> stubs = new LinkedHashMap<>();
		stubs.put(GENERATE_CHALLENGE_API, GENERATE_CHALLENGE_SUCCESS_BODY);
		stubs.put(VERIFY_CHALLENGE_API, INVALID_TRANSACTION_BODY);
		interceptor = new ForcedApiResponseInterceptor(driver, 200, stubs);
	}

	@Then("verify the transaction timeout error popup is displayed")
	public void verifyTransactionTimeoutPopupDisplayed() {
		assertTrue("Transaction timeout error popup was not displayed", popupPage.isPopupDisplayed());
	}

	@Then("verify the error popup header shows the error title")
	public void verifyErrorPopupHeader() {
		String expected = LocaleTextUtil.get(popupPage.getActiveLanguage(), TITLE_KEY);
		assertEquals("Unexpected error popup title", expected, popupPage.getPopupTitle());
	}

	@Then("verify the error popup message shows the transaction timeout message")
	public void verifyErrorPopupMessage() {
		String expected = LocaleTextUtil.get(popupPage.getActiveLanguage(), MESSAGE_KEY);
		assertEquals("Unexpected error popup message", expected, popupPage.getPopupMessage());
	}

	@Then("verify the error popup has an Okay button")
	public void verifyOkayButton() {
		assertTrue("Okay button not displayed on the error popup", popupPage.isOkayButtonDisplayed());
		String expected = LocaleTextUtil.get(popupPage.getActiveLanguage(), OKAY_KEY);
		assertEquals("Unexpected Okay button label", expected, popupPage.getOkayButtonText());
	}

	@When("user clicks on the Okay button in the error popup")
	public void userClicksOkay() {
		popupPage.clickOkay();
	}

	// Higher order than BaseTest's @After so interception is released while the driver is alive
	@After(value = "@transactionTimeout", order = 20000)
	public void tearDownInterceptor() {
		if (interceptor != null) {
			interceptor.close();
			interceptor = null;
		}
	}
}
