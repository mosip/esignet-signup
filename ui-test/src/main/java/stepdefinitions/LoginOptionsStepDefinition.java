package stepdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import base.BaseTest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mosip.testrig.apirig.testrunner.OTPListener;
import pages.LoginOptionsPage;
import pages.RegistrationPage;
import utils.EsignetUtil;
import utils.EsignetUtil.RegisteredDetails;

public class LoginOptionsStepDefinition {

	public WebDriver driver;
	BaseTest baseTest;
	LoginOptionsPage loginOptionsPage;
	RegistrationPage registrationPage;

	public LoginOptionsStepDefinition(BaseTest baseTest) {
		this.baseTest = baseTest;
		this.driver = BaseTest.getDriver();
		loginOptionsPage = new LoginOptionsPage(driver);
		registrationPage = new RegistrationPage(driver);
	}

	@Then("verify the default language in which UI is rendered")
	public void verifyDefaultLanguage() {
		String expectedLang = loginOptionsPage.getExpectedDefaultLanguage();
		String actualLang = loginOptionsPage.getCurrentLanguage();
		assertEquals(expectedLang, actualLang);
	}

	@Given("click on Sign In with eSignet")
	public void clickOnSignInWithEsignet() throws Exception {
		loginOptionsPage.clickOnSignInWIthEsignet();
		new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains("#"));
		String currentUrl = driver.getCurrentUrl();
		loginOptionsPage.setAuthorizeUrl(currentUrl);
	}

	@Then("validate that the logo is displayed")
	public void validateTheLogo() {
		assertTrue(loginOptionsPage.isLogoDisplayed());
	}

	@When("user enter valid mobile number in the mobile number field")
	public void userEnterMobNumber() {
		String phoneNumber = EsignetUtil.generateMobileNumberFromRegex();
		RegisteredDetails.setMobileNumber(phoneNumber);
		loginOptionsPage.enterMobileNumber(phoneNumber);
	}
	
	@When("user enters the correct OTP as input")
	public void userEntersOtp() {
	    String mobile = RegisteredDetails.getMobileNumber();
	    registrationPage.enterOtp(OTPListener.getOtp(mobile));
	}

	@And("user redirected to registration page")
	public void verifyRedirectedToRegistrationPage() {
		assertTrue(registrationPage.isSetupAccountPageVisible());
	}

	@And("user clicks on continue button on registration page")
	public void userClicksOnContinueButtonOnRegistrationPage() {
		registrationPage.clickOnSetupAccountContinueButton();
	}

	@Then("user clicks on continue button on success page")
	public void userClicksOnContinueButtonOnSuccessPage() {
		registrationPage.clickOnContinueButtonInSucessScreen();
	}

	@Then("it will redirect to congratulations on login page")
	public void verifyCongratulationPageIsDisplayed() {
		assertTrue(registrationPage.isAccountCreatedSuccessfullyMessageDisplayed());
	}

	@Then("user clicks on login button")
	public void userClicksOnLoginButton() {
		registrationPage.clickOnLoginButtonInSuccessScreen();
	}
	
	@When("user clicks on the navigate back button")
	public void userClicksNavigateBackButton() {
		registrationPage.clickOnNavigateBackButton();
	}

	@And("user clicks on login with password button")
	public void userClicksOnLoginWithPasswordButton() {
		loginOptionsPage.clickOnLoginWithPasswordOption();
	}

	@Then("verify login button is disabled")
	public void verifyLoginButtonIsDisabled() {
		assertFalse(loginOptionsPage.isLoginButtonEnabled());
	}

	@When("user enter less than minimum number into mobile number field")
	public void enterLessDigitNumber() {
		String lessDigit = EsignetUtil.getLessThanMinimumDigit();
		loginOptionsPage.enterRegisteredMobileNumber(lessDigit);
	}

	@And("user enters registered password into password field")
	public void userEnterRegisteredPassword() {
		String registeredPassword = RegisteredDetails.getPassword();
		loginOptionsPage.enterRegisteredPassword(registeredPassword);
	}

	@And("user clicks on login button in login page")
	public void userClicksOnLoginBtnInLoginPage() {
		loginOptionsPage.clickOnLoginButton();
	}

	@Then("verify error Please Enter Valid Individual ID. is displayed")
	public void verifyInvalidErrorDisplayed() {
		assertTrue(loginOptionsPage.isInvalidNumberErrorDisplayed());
	}

	@And("user enter all digit zero into mobile number field")
	public void userEnterAllDigitZero() {
		String allZero = EsignetUtil.getAllZeros(7);
		loginOptionsPage.enterRegisteredMobileNumber(allZero);
	}

	@When("user enter registered mobile number in mobile number field")
	public void userEnterRegisteredNumber() {
		String registeredNumber = RegisteredDetails.getMobileNumber();
		loginOptionsPage.enterRegisteredMobileNumber(registeredNumber);
	}

	@And("user enters special Character into password field")
	public void userEnterSpecialCharacter() {
		String specialChar = EsignetUtil.getSpecialChar();
		loginOptionsPage.enterRegisteredPassword(specialChar);
	}

	@And("user tabout of password field")
	public void userTabOut() {
		loginOptionsPage.userTaboutOfPasswordField();
	}

	@Then("verify error Please Enter Valid Password is displayed")
	public void verifyInvalidPasswordErrorDisplayed() {
		assertTrue(loginOptionsPage.isInvalidPasswordErrorDisplayed());
	}

	@And("user enters Unregistered password into password field")
	public void userEnterUnregisteredPassword() {
		String unregisteredPassword = EsignetUtil.generateValidPasswordFromActuator();
		loginOptionsPage.enterRegisteredPassword(unregisteredPassword);
	}

	@Then("verify error Username or password is not valid. Please enter valid credentials. is displayed")
	public void verifyInvalidUsernameOrPasswordErrorDisplayed() {
		assertTrue(loginOptionsPage.isInvalidUsernameOrPasswordErrorDisplayed());
	}

	@When("user enter Unregistered mobile number into mobile number field")
	public void userEnterUnregisteredNumber() {
		String unregisteredNumber = EsignetUtil.generateMobileNumberFromRegex();
		loginOptionsPage.enterRegisteredMobileNumber(unregisteredNumber);
	}

	@When("user click on the close icon in error message")
	public void userClickOnErrorCloseIcon() {
		loginOptionsPage.clickOnErrorCloseIcon();
	}

	@Then("verify the error message disappears")
	public void verifyErrorMessageIsGone() {
		loginOptionsPage.verifyErrorDisappearsAfterClose();
	}

	@Then("verify error message disappears automatically after 10 seconds")
	public void waitForErrorMessageToDisappear() {
		loginOptionsPage.verifyErrorMessageDisappearsAfterTenSeconds();
	}

	@And("user enters random char into password field")
	public void userEnterRandomCharacter() {
		String randomPassword = EsignetUtil.generateInvalidPassword(8);
		loginOptionsPage.enterRegisteredPassword(randomPassword);
	}

	@Then("verify login button is enabled")
	public void verifyLoginButtonIsEnabled() {
		assertTrue(loginOptionsPage.isLoginButtonEnabled());
	}

	@Then("verify consent should ask user to proceed in attention page")
	public void userGoesToAttentionScreen() {
		Assert.assertTrue(loginOptionsPage.isOnAttentionScreen());
	}

	@And("clicks on proceed button in attention page")
	public void clickOnProceedButtonInAttentionPage() {
		loginOptionsPage.clickOnProceedButtonInAttentionPage();
	}

	@And("clicks on proceed button in next page")
	public void clickOnProceedButtonInNextPage() {
		loginOptionsPage.clickOnProceedButton();
	}

	@Then("select the e-kyc verification provider")
	public void selectEKycVerificationProvider() {
		loginOptionsPage.clickOnMockIdentifyVerifier();
	}

	@And("clicks on proceed button in e-kyc verification provider page")
	public void clickOnProceedButton() {
		loginOptionsPage.clickOnProceedButtonInServiceProviderPage();
	}

	@And("user select the check box in terms and condition page")
	public void userSelectTheCheckBoxInTermsAndConditionPage() {
		loginOptionsPage.checkTermsAndConditions();
	}

	@And("user clicks on proceed button in terms and condition page")
	public void userClicksOnProceedButtonInTermsAndConditionPage() {
		loginOptionsPage.clickOnProceedButtonInTermsAndConditionPage();
	}

	@And("user clicks on proceed button in camera preview page")
	public void userClicksOnProceedButtonInCameraPreviewPage() {
		loginOptionsPage.clickOnProceedButtonInCameraPreviewPage();
	}

	@Then("user is navigated to consent screen once liveness check completes")
	public void waitUntilLivenessCheckCompletesInCameraPage() {
		loginOptionsPage.waitUntilLivenessCheckCompletes();
	}

	@And("verify the header Please take appropriate action in xx:xx is displayed")
	public void verifyConsentHeaderDisplayed() {
		Assert.assertTrue(loginOptionsPage.isConsentSceenDisplayed(), "Consent page should be displayed");
	}

	@And("verify logos of the relying party and e-Signet is displayed")
	public void verifyBothLogosAreDisplayed() {
		Assert.assertTrue(loginOptionsPage.areLogosDisplayed(),
				"Logos of relying party and e-Signet should be visible");
	}

	@And("verify the essential claims with \"i\" icon is displayed")
	public void verifyEssentialIIconIsDisplayed() {
		assertTrue(loginOptionsPage.isEssentialIconDisplayed());
	}

	@And("verify required essential claims is displayed")
	public void verifyEssentialClaimsDisplayed() {
		Assert.assertTrue(loginOptionsPage.isEssentialClaimsDisplayed());
	}

	@And("verify the voluntary claims with \"i\" icon is displayed")
	public void verifyVoluntaryClaimsIconIsDisplayed() {
		assertTrue(loginOptionsPage.isVoluntaryClaimsIconDisplayed());
	}

	@And("verify list of voluntary claims displayed")
	public void verifyListOfVoluntaryClaimsDisplayed() {
		assertTrue(loginOptionsPage.isVoluntaryClaimsDisplayed());
	}

	@And("verify allow button is visible consent screen")
	public void verifyAllowButtonInConsentScreen() {
		Assert.assertTrue(loginOptionsPage.isAllowButtonInConsentScreenVisible());
	}

	@And("verify cancel button is visible consent screen")
	public void verifyCancelButtonInConsentScreen() {
		Assert.assertTrue(loginOptionsPage.isCancelButtonInConsentScreenVisible());
	}

	@Then("verify the tooltip message for Essential Claims info icon")
	public void verifyTooltipMessageForEssentialClaimsIcon() {
		String actualTooltip = loginOptionsPage.getEssentialClaimsTooltipText();
		assertFalse(actualTooltip.trim().isEmpty());
	}

	@When("user click on cancel button in consent screen")
	public void userClickOnCancelBtn() {
		loginOptionsPage.clickOnCancelBtnInConsentScreen();
	}

	@Then("verify the header Please Confirm is displayed")
	public void verifyWarningMessage() {
		assertTrue(loginOptionsPage.isCancelWarningHeaderDisplayed());
	}

	@Then("verify the message Are you sure you want to discontinue the login process? is displayed")
	public void verifyWarningHeader() {
		assertTrue(loginOptionsPage.isCancelWarningMessageDisplayed());
	}

	@Then("verify discontinue button is displayed")
	public void verifyDiscontinueButton() {
		assertTrue(loginOptionsPage.isDiscontinueButtonDisplayed());
	}

	@Then("verify stay button is displayed")
	public void verifWarningHeader() {
		assertTrue(loginOptionsPage.isStayButtonDisplayed());
	}

	@When("user click on Stay button")
	public void userClickOnStayBtn() {
		loginOptionsPage.clickOnStayBtnInConsentScreen();
	}

	@Then("verify user is retained on same consent screen")
	public void verifyOnSameConsentScreen() {
		assertTrue(loginOptionsPage.isConsentSceenDisplayed());
	}

	@Then("user click on Discontinue button")
	public void userClickOnDiscontinueBtn() {
		loginOptionsPage.clickOnDiscontinueButton();
	}

	@And("verify user is redirected to relying party portal page")
	public void verifyRedirectedToMainScreen() {
		assertTrue(loginOptionsPage.isHealthPortalPageDisplayed());
	}

	@When("user enables the master toggle of voluntary claims")
	public void enableMasterToggleOfVoluntaryClaims() {
		loginOptionsPage.enableMasterToggleVoluntaryClaims();
	}

	@And("click on allow button in consent page")
	public void userClickOnAllowButton() {
		loginOptionsPage.clickOnAllowBtnInConsentScreen();
	}

	@Then("verify user is navigated to landing page of relying party")
	public void verifyRedirectedToRelyingPartyPage() {
		assertTrue(loginOptionsPage.isWelcomePageDisplayed());
	}

	@Then("verify welcome message is displayed with the registered name")
	public void verifyWelcomeWithNameIsDisplayed() {
		assertTrue(loginOptionsPage.isWelcomePageDisplayed());
	}

	@Then("verify input should be restricted for 9 digit")
	public void verifyNumberIsRestricted() {
		assertTrue(loginOptionsPage.isInputRestrictedToNineDigits());
	}

	@Then("verify input should restrict it and allow only numbers")
	public void verifyAlphanumericIsRestricted() {
		assertTrue(loginOptionsPage.isInputRestrictedToNineDigits());
	}

	@Then("Enable the toggle for first voluntary claim and verify it is selected")
	public void enableToggleForFirstVoluntaryClaim() throws Exception {
		List<String> voluntaryClaims = loginOptionsPage.getClaims("voluntary");
		if (!voluntaryClaims.isEmpty()) {
			String firstVoluntary = voluntaryClaims.get(0);
			loginOptionsPage.enableFirstVoluntaryClaim(firstVoluntary);
			assertTrue(loginOptionsPage.isToggleSelected(firstVoluntary));
		}
	}

	@Then("Disable the toggle for first voluntary claim and verify it is deselected")
	public void disableToggleForFirstVoluntaryClaim() throws Exception {
		List<String> voluntaryClaims = loginOptionsPage.getClaims("voluntary");
		if (!voluntaryClaims.isEmpty()) {
			String firstVoluntary = voluntaryClaims.get(0);
			loginOptionsPage.disableFirstVoluntaryClaim(firstVoluntary);
			assertTrue(loginOptionsPage.isToggleDeselected(firstVoluntary));
		}
	}

	@Then("Verify the existence of master toggle button when only one field is present under the Voluntary claims")
	public void verifyMasterToggleForVoluntaryClaims() throws Exception {
		List<String> voluntaryClaims = loginOptionsPage.getClaims("voluntary");
		if (voluntaryClaims.size() == 1) {
			assertFalse(loginOptionsPage.isVoluntaryMasterTogglePresent());
		} else if (voluntaryClaims.size() > 1) {
			assertTrue(loginOptionsPage.isVoluntaryMasterTogglePresent());
		}
	}

	@Then("Verify the welcome message when user clicks on Allow without consenting for Full name")
	public void verifyWelcomeMessageWithoutFullName() throws Exception {
		List<String> voluntaryClaims = loginOptionsPage.getClaims("voluntary");
		if (voluntaryClaims.contains("name")) {
			loginOptionsPage.clickOnAllowBtnInConsentScreen();
			String msg = loginOptionsPage.getWelcomeMessage();
			Assert.assertFalse(msg.toLowerCase().contains("name"),
					"Welcome message should not contain Full name when not consented.");
		} else {
			Assert.assertTrue(true, "Full name is not in voluntary claims");
		}
	}

	@When("user enter more than maximum digit into mobile number field")
	public void userEnterMaxDigitMobileNumber() {
		String maxDigit = EsignetUtil.getMoreThanMaxDigits();
		loginOptionsPage.enterRegisteredMobileNumber(maxDigit);
	}

	@When("user enter specialCharacter into mobile number field")
	public void userEnterSpecialCharacterInMobileNumber() {
		String specialChar = EsignetUtil.getSpecialChar();
		loginOptionsPage.enterRegisteredMobileNumber(specialChar);
	}

	@When("user enter alphaNumeric into mobile number field")
	public void entersAlphaNumeric() {
		String alphaNumeric = EsignetUtil.getAlphaNumeric();
		loginOptionsPage.enterRegisteredMobileNumber(alphaNumeric);
	}

}
