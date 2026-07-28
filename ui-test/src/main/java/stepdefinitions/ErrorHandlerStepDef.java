package stepdefinitions;

import static org.junit.Assert.assertTrue;

import org.openqa.selenium.WebDriver;

import base.BaseTest;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.ErrorHandlerPage;
import utils.LocaleTextUtil;
import utils.NetworkErrorInterceptor;

// Steps for the "Something went wrong" error handler page
public class ErrorHandlerStepDef {

	private static final String TITLE_KEY = "something_went_wrong";
	private static final String DETAIL_KEY = "something_went_wrong_detail";
	// Only /settings goes through the app's ApiService interceptor on every page load
	private static final String SETTINGS_API = "/v1/signup/settings";

	private final WebDriver driver;
	private final ErrorHandlerPage errorPage;
	private NetworkErrorInterceptor interceptor;

	public ErrorHandlerStepDef(BaseTest baseTest) {
		this.driver = BaseTest.getDriver();
		this.errorPage = new ErrorHandlerPage(driver);
	}

	// ---- Generic error page (direct navigation) -------------------------------

	@Given("user navigates directly to the error handler page")
	public void userNavigatesDirectlyToErrorHandlerPage() {
		errorPage.navigateToErrorPage();
		assertTrue("Error handler page did not load", errorPage.isErrorTitleDisplayed());
	}

	// ---- Forced HTTP 5XX (CDP, local only) ------------------------------------

	@Given("the signup settings API is forced to return HTTP {int}")
	public void forceSettingsApiToReturn(int statusCode) {
		interceptor = new NetworkErrorInterceptor(driver, SETTINGS_API, statusCode);
	}

	@When("user navigates to the signup portal to trigger the error")
	public void navigateToSignupPortalToTriggerError() {
		errorPage.navigateToSignupPortalRoot();
		assertTrue("App did not redirect to the error handler page after the forced 5XX",
				errorPage.waitForErrorPage());
	}

	@Then("verify the error handler page title shows the reason phrase {string}")
	public void verifyErrorTitleShowsReasonPhrase(String reasonPhrase) {
		assertTrue("Expected error title '" + reasonPhrase + "' but was '" + errorPage.getErrorTitle() + "'",
				errorPage.waitForTitle(reasonPhrase));
	}

	// ---- Language switch ------------------------------------------------------

	@When("user switches the error handler page language to {string}")
	public void userSwitchesErrorHandlerLanguage(String lang) {
		errorPage.switchLanguage(lang);
	}

	@Then("verify the error handler page displays title in {string} language")
	public void verifyErrorTitleInLanguage(String lang) {
		String expected = LocaleTextUtil.get(lang, TITLE_KEY);
		assertTrue("Expected error title '" + expected + "' (" + lang + ") but was '" + errorPage.getErrorTitle() + "'",
				errorPage.waitForTitle(expected));
	}

	@Then("verify the error handler page displays description in {string} language")
	public void verifyErrorDescriptionInLanguage(String lang) {
		String expected = LocaleTextUtil.get(lang, DETAIL_KEY);
		assertTrue(
				"Expected error description '" + expected + "' (" + lang + ") but was '"
						+ errorPage.getErrorDescription() + "'",
				errorPage.waitForDescription(expected));
	}

	// Higher order than BaseTest's @After so interception is released while the driver is alive
	@After(value = "@errorHandlerHttp", order = 20000)
	public void tearDownInterceptor() {
		if (interceptor != null) {
			interceptor.close();
			interceptor = null;
		}
	}
}
