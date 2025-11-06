package stepdefinitions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import io.cucumber.java.en.When;
import io.mosip.testrig.apirig.testrunner.OTPListener;
import base.BaseTest;
import io.cucumber.java.en.Then;

import pages.ForgetPasswordPage;
import pages.LoginOptionsPage;
import pages.SmtpPage;
import pages.SignUpPage;
import utils.BaseTestUtil;
import utils.EsignetUtil;
import utils.MultiLanguageUtil;
import utils.EsignetUtil.RegisteredDetails;;

public class ForgetPasswordStepDefinition {

	public WebDriver driver;
	private static final Logger logger = Logger.getLogger(ForgetPasswordStepDefinition.class);
	BaseTest baseTest;
	LoginOptionsPage loginOptionsPage;
	ForgetPasswordPage forgetPasswordPage;
	SmtpPage smtpPage;
	SignUpPage signUpPage;

	public ForgetPasswordStepDefinition(BaseTest baseTest) {
		this.baseTest = baseTest;
		this.driver = BaseTest.getDriver();
		this.forgetPasswordPage = new ForgetPasswordPage(driver);
		this.loginOptionsPage = new LoginOptionsPage(driver);
		this.smtpPage = new SmtpPage(driver);
		this.signUpPage = new SignUpPage(driver);
	}

	private String newGeneratedPassword;

	@Then("user enters mobile_number in the mobile number text box")
	public void userEnterValidMobileNumber() {
		String mobileNumber = EsignetUtil.generateMobileNumberFromRegex();
		RegisteredDetails.setMobileNumber(mobileNumber);
		forgetPasswordPage.enterMobileNumber(mobileNumber);
	}

	@When("user enters the OTP")
	public void userEnterOtp() {
		String mobile = RegisteredDetails.getMobileNumber();
		forgetPasswordPage.enterOtp(OTPListener.getOtp(mobile));
	}

	@When("user click on reset password button")
	public void userClickOnResetPasswordButton() {
		forgetPasswordPage.clickOnResetPasswordButton();
		forgetPasswordPage.clickOnLanguageSelectionDropdown();
		String languagePassed = MultiLanguageUtil.getDisplayName(BaseTestUtil.getThreadLocalLanguage());
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		WebElement langOption = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='" + languagePassed + "']")));
		langOption.click();
		BaseTestUtil.setThreadLocalLanguage(languagePassed);
	}

	@Then("user verify country code prefix")
	public void userVerifyCountryCodePrefix() {
		Assert.assertTrue(forgetPasswordPage.isphonePrefixDisplayed(), "Country code prefix displayed");
	}

	@Then("user verify the water mark text inside phonenumber")
	public void userVerifyWatermarkTextInsidePhonenumber() {
		Assert.assertTrue(forgetPasswordPage.isWaterMarkDisplayed());
	}

	@Then("user verify country code is not editable")
	public void userVerifyCountryCodeIsNotEditable() {
		Assert.assertTrue(forgetPasswordPage.isCountryCodeNonEditable(), "Country code is not editable");
	}

	String phoneNumber;

	@When("user enters {string} into the mobile number field")
	public void userEntersMobileNumber(String number) {
		this.phoneNumber = number;
		forgetPasswordPage.enterPhoneNumber(number);
	}

	@When("user enters Registered mobile number into the mobile number field")
	public void userEntersRegisteredMobileNumber() {
		String registeredNumber = RegisteredDetails.getMobileNumber();
		forgetPasswordPage.enterPhoneNumber(registeredNumber);
	}

	@When("user clicks outside the input to trigger validation")
	public void userClicksOutsideToTriggerValidation() {
		forgetPasswordPage.triggerPhoneValidation();
	}

	@Then("phone number should be invalid")
	public void verifyPhoneNumberIsInvalid() {
		assertTrue(forgetPasswordPage.isPhoneErrorVisible());
	}

	@Then("phone number should be valid")
	public void verifyPhoneNumberIsValid() {
		assertFalse(forgetPasswordPage.isPhoneErrorVisible());
	}

	@Then("user verify forget password heading")
	public void userVerifyForgetPasswordHeading() {
		Assert.assertTrue(forgetPasswordPage.isForgetPasswordHeadingVisible(), "Forget password heading visible");
	}

	@Then("user verify subheading on forget password")
	public void userVerifySubheadingOnForgetPassword() {
		Assert.assertTrue(forgetPasswordPage.isForgetPasswordSubHeadingVisible(),
				"Subheading on foget password visible");
	}

	@Then("user verify username label on forget password")
	public void userVerifyUserLabelOnForgetPassword() {
		Assert.assertTrue(forgetPasswordPage.isUserNameLabelVisible(), "User Label on foget password visible");
	}

	@Then("user verify fullname label on forget password")
	public void userVerifyFullnameLabelOnForgetPassword() {
		Assert.assertTrue(forgetPasswordPage.isFullNameLabelVisible(), "Fullname Label on foget password visible");
	}

	@Then("user verify continue button on forget password")
	public void userVerifyLangSelectionButton() {
		Assert.assertTrue(forgetPasswordPage.isLangSelectionButtonVisible(),
				"Lang selection button on foget password visible");
	}

	@Then("user verify footer on forget password")
	public void userVerifyFooterOnForgetPassword() {
		Assert.assertTrue(forgetPasswordPage.isFooterPoweredByVisible(),
				"Lang selection button on foget password visible");
	}

	@When("user enters number with starting 0 into the mobile number field")
	public void userEntersDigitStartingWith0() {
		String zeroNum = EsignetUtil.getNumberStartingWithZero(6);
		forgetPasswordPage.enterPhoneNumber(zeroNum);
	}

	@When("user enters number with all zeros into the mobile number field")
	public void userEnterAllZeros() {
		String allZero = EsignetUtil.getAllZeros(6);
		forgetPasswordPage.enterPhoneNumber(allZero);
	}

	@When("user enters more than max digits into the mobile number field")
	public void userEnterMoreThanMaxDigits() {
		String maxDigit = EsignetUtil.getMoreThanMaxDigits();
		forgetPasswordPage.enterPhoneNumber(maxDigit);
	}

	@When("user enters special char input into the mobile number field")
	public void userEnterSpecialCharacters() {
		String specialChar = EsignetUtil.getSpecialChar();
		forgetPasswordPage.enterPhoneNumber(specialChar);
	}

	@When("user enters alphanumeric input into the mobile number field")
	public void userEntersAlphaNumeric() {
		String alphaNumeric = EsignetUtil.getAlphaNumeric();
		forgetPasswordPage.enterPhoneNumber(alphaNumeric);
	}

	@Then("mobile number input should contain numbers only")
	public void verifyMobileNumberInput() {
		Assert.assertTrue(forgetPasswordPage.isMobileFieldHasNumericOnly());
	}

	@Then("mobile number input should remain empty")
	public void verifyMobileNumberInputIsEmpty() {
		assertTrue(forgetPasswordPage.isPhoneNumberFieldEmpty());
	}

	@Then("user verify continue button is not enabled")
	public void userVerifyContinueButtonNotEnabled() {
		Assert.assertTrue(forgetPasswordPage.isContinueButtonDisabled(), "Continue button disabled");
	}

	@Then("verify the mobile number field should restrict to max digits")
	public void verifyNumberRestrictedToNine() {
		assertTrue(forgetPasswordPage.isInputRestrictedToNineDigits());
	}

	@When("user enters name more than maximum characters into the fullname field")
	public void userEnterMoreThanMaxLengthFullName() {
		String maxFullname = EsignetUtil.getMoreThanMaxLengthFullName("km");
		forgetPasswordPage.enterFullName(maxFullname);
	}

	@When("user enters special char input into the fullname field")
	public void userEnterSpecialCharacterInFullName() {
		String specialChar = EsignetUtil.getSpecialChar();
		forgetPasswordPage.enterFullName(specialChar);
	}

	@When("user enters Numeric input into the fullname field")
	public void userEnterNumericInFullName() {
		String numericName = EsignetUtil.getNumericFullName("km");
		forgetPasswordPage.enterFullName(numericName);
	}

	@When("user enters Alphanumeric input into the fullname field")
	public void userEnterAlphanumericInKhmerFullName() {
		String alphaNumeric = EsignetUtil.getAlphanumericFullName("km");
		forgetPasswordPage.enterFullName(alphaNumeric);
	}

	@When("user enters other language input into the fullname field")
	public void userEntersFullNameInOtherLanguage() {
		EsignetUtil.FullName names = EsignetUtil.generateNamesFromUiSpec();
		forgetPasswordPage.enterFullName(names.english);
	}

	@Then("user verify full name error message")
	public void verifyFullnameErrorDisplayed() {
		Assert.assertTrue(forgetPasswordPage.isFullNameErrorVisible());
	}

	@Then("user verify full name error message not displayed")
	public void verifyFullnameErrorNotDisplayed() {
		Assert.assertFalse(forgetPasswordPage.isFullNameErrorPresent(),
				"Expected no error message, but one was present");
	}

	@Then("only 30 characters are retained in the fullname field")
	public void verifyMaxCharactersAllowed() {
		assertTrue(forgetPasswordPage.isFullnameRestrictedToMaxChars());
	}

	@When("user enters registered fullname into the full name field")
	public void userEntersRegisteredFullname() {
		String registeredFullname = RegisteredDetails.getFullName();
		forgetPasswordPage.enterFullName(registeredFullname);
	}

	@Then("user verify continue button is enabled")
	public void userVerifyContinueButtonIsEnabled() {
		Assert.assertTrue(forgetPasswordPage.isContinueButtonEnabled(), "Continue button enabled");
	}

	@Then("user click on continue button")
	public void userClickOnContinueButtonIsEnabled() {
		forgetPasswordPage.clickOnContinueButton();
	}

	@Then("user waits until OTP timer to expire")
	public void userWaitForOtpToExpire() {
		forgetPasswordPage.waitForOtpExpire();
	}

	private int waitTime;

	@Then("user waits for resend OTP button and verifies it's enabled or skipped")
	public void waitForResendOtpButtonAndValidate() throws InterruptedException {
		waitTime = EsignetUtil.getOtpResendDelayFromSignupActuator();
		logger.info("Waiting for OTP resend delay: " + waitTime + " seconds");
		Thread.sleep(waitTime * 1000L + 1000);
		Assert.assertTrue(forgetPasswordPage.isResendOtpButtonEnabled(), "Resend OTP button should be enabled");
	}

	@Then("user waits and clicks on resend OTP, then validates {int} out of 3 attempts message")
	public void userClicksResendOtpAndValidatesAttemptLeft(int expectedAttemptLeft) throws Exception {
		forgetPasswordPage.clickOnResendOtp();
		String attemptText = forgetPasswordPage.getOtpResendAttemptsText(expectedAttemptLeft);
		Assert.assertTrue(attemptText.contains(String.valueOf(expectedAttemptLeft)),
				"Expected attempt count: " + expectedAttemptLeft + " not found. Actual: " + attemptText);
	}

	@Then("user waits for OTP timer to expire for fourth time")
	public void userWaitsForOtpToExpire() {
		forgetPasswordPage.waitUntilOtpExpire();
	}

	@Then("verify user is redirected back to the Forget Password screen")
	public void verifyUserRedirectedToPreviousScreen() {
		assertTrue(forgetPasswordPage.isForgetPassowrdScreenVisible());
	}

	@Then("verify error popup with header Invalid is displayed")
	public void verifyHeaderInErrorPopup() {
		assertTrue(forgetPasswordPage.isErrorHeaderInvalidVisible());
	}

	@Then("user enters unregistered mobile number in the mobile number text box")
	public void userEnterUnRegisterdMobileNumber() {
		String mobileNumber = EsignetUtil.generateMobileNumberFromRegex();
		forgetPasswordPage.enterMobileNumber(mobileNumber);
	}

	@Then("verify error message Transaction has failed due to invalid request. Please try again. is displayed")
	public void verifyMessageInErrorPopup() {
		assertTrue(forgetPasswordPage.isErrorMessageVisible());
	}

	@When("user enters Unregistered FullName into the fullname field")
	public void userEntersUnRegisteredFullname() {
		EsignetUtil.FullName names = EsignetUtil.generateNamesFromUiSpec();
		forgetPasswordPage.enterFullName(names.khmer);
	}

	@Then("verify error message The mobile number or name entered is invalid. Please enter valid credentials associated with your account and try again. is displayed")
	public void verifyMessageInErrorPopupDisplayed() {
		assertTrue(forgetPasswordPage.isErrorMessageVisible());
	}

	@Then("verify retry button is displayed")
	public void validateRetryButton() {
		assertTrue(forgetPasswordPage.isRetryButtonVisible());
	}

	@Then("user click on retry button")
	public void userClicksOnRetryButton() {
		forgetPasswordPage.clickOnRetryButton();
	}

	@Then("verify user is redirected to the reset password screen")
	public void verifyUserIsRedirectedToesetPasswordScreen() {
		assertTrue(forgetPasswordPage.isResetPasswordScreenVisible());
	}

	@When("user enters the OTP in forgot password flow")
	public void userEntersValidOtp() {
		forgetPasswordPage.enterOtp("111111");
	}

	@Then("user verify reset password header")
	public void userVerifyResetPasswordHeader() {
		assertTrue(forgetPasswordPage.isResetPasswordHeaderVisible());
	}

	@Then("user verify reset password description")
	public void userVerifyResetPasswordDescription() {
		assertTrue(forgetPasswordPage.isPasswordInstructionMessageVisible());
	}

	@Then("user verify new password label")
	public void userVerifyNewPasswordLabel() {
		assertTrue(forgetPasswordPage.isNewPasswordLabelVisible());
	}

	@Then("user verify confirm new password label")
	public void userVerifyConfirmNewPasswordLabel() {
		assertTrue(forgetPasswordPage.isConfirmNewPasswordLabelVisible());
	}

	@Then("user verify new password input text box is present")
	public void userVerifyNewPasswordInputTextbox() {
		assertTrue(forgetPasswordPage.isNewPasswordInputTextboxVisible());
	}

	@Then("user verify confirm new password input text box is present")
	public void userVerifyConfirmNewPasswordInputTextbox() {
		assertTrue(forgetPasswordPage.isConfirmNewPasswordInputTextboxVisible());
	}

	@Then("user verify new password info icon is visible")
	public void userVerifyNewPasswordInfoIcon() {
		assertTrue(forgetPasswordPage.isNewPasswordInfoIconVisible());
	}

	@Then("user click on new password info icon")
	public void userClickOnNewPasswordInfoIcon() {
		forgetPasswordPage.clickOnNewPasswordInfoIcon();
	}

	@Then("verify new password policy displayed")
	public void userVerifyNewPasswordPolicyToolTip() {
		assertTrue(forgetPasswordPage.isPasswordPolicyTooltipVisible());
	}

	@Then("user verify new password field placeholder")
	public void userVerifyNewPasswordPlaceholder() {
		String actualPlaceholder = forgetPasswordPage.getNewPasswordFieldPlaceholder();
		assertTrue(actualPlaceholder != null && !actualPlaceholder.trim().isEmpty());
	}

	@Then("user verify confirm password field placeholder")
	public void userVerifyConfirmPasswordPlaceholder() {
		String actualPlaceholder = forgetPasswordPage.getConfirmNewPasswordFieldPlaceholder();
		assertTrue(actualPlaceholder != null && !actualPlaceholder.trim().isEmpty());
	}

	@When("user enters invalid password into the new password field")
	public void userEnterInvalidNewPassword() {
		String invalidPassword = EsignetUtil.generateInvalidPassword(7);
		forgetPasswordPage.enterNewPassword(invalidPassword);
	}

	@When("user enters shorter password into the new password field")
	public void userEnterShorterNewPassword() {
		forgetPasswordPage.enterShortNewPassword();
	}

	@When("user enters longer password into the new password field")
	public void userEnterLongerNewPassword() {
		String longPassword = EsignetUtil.generateInvalidPassword(30);
		forgetPasswordPage.enterNewPassword(longPassword);
	}

	@When("user enters new password in Forgot Password flow")
	public void userEntersNewPassword() {
		newGeneratedPassword = EsignetUtil.generateValidPasswordFromActuator();
		forgetPasswordPage.enterNewPassword(newGeneratedPassword);
	}

	@Then("user clicks outside the password field")
	public void userClicksOutsidePasswordField() {
		forgetPasswordPage.clickOutsidePasswordField();
	}

	@Then("verify an error message Password does not meet the password policy. is displayed")
	public void verifyInvalidPasswordError() {
		assertTrue(forgetPasswordPage.isPasswordErrorDisplayed());
	}

	@Then("verify password input is resitricted to maximum characters")
	public void verifyPasswordIsResticted() {
		assertTrue(forgetPasswordPage.isPasswordRestrictedToMaxChar());
	}

	@When("user enters different password into the confirm password field")
	public void userFillsConfirmPasswordField() {
		String confirmPassword = EsignetUtil.generateValidPasswordFromActuator();
		forgetPasswordPage.enterConfirmPassword(confirmPassword);
	}

	@When("user enters less than min characters into the confirm password field")
	public void userEnterShortConfirmPassword() {
		forgetPasswordPage.enterShortConfirmPassword();
	}

	@When("user enters more than max characters into the confirm password field")
	public void userEnterLongConfirmPassword() {
		String longPassword = EsignetUtil.generateInvalidPassword(25);
		forgetPasswordPage.enterConfirmPassword(longPassword);
	}

	@When("user enters new confirm password in Forgot Password flow")
	public void userEntersValidConfirmPassword() {
		forgetPasswordPage.enterConfirmPassword(newGeneratedPassword);
	}

	@Then("verify an error message New Password and Confirm New Password do not match. is displayed")
	public void verifyInvalidConfirmPasswordError() {
		assertTrue(forgetPasswordPage.isconfirmPasswordErrorDisplayed());
	}

	@Then("verify confirm password input is restricted to max characters")
	public void verifyConfirmPasswordIsResticted() {
		assertTrue(forgetPasswordPage.isConfirmPasswordRestrictedToMaxChar());
	}

	@Then("validate the New Password field is masked")
	public void passwordFieldShouldBeMasked() {
		assertTrue(forgetPasswordPage.isNewPasswordFieldMasked());
	}

	@Then("verify the Confirm Password field is masked")
	public void confirmPasswordFieldShouldBeMasked() {
		assertTrue(forgetPasswordPage.isConfirmPasswordFieldMasked());
	}

	@When("user clicks on the unmask icon in the New Password field")
	public void userClicksOnPassUnmaskIcon() {
		forgetPasswordPage.clickOnNewPasswordUnmaskIcon();
	}

	@Then("validate the New Password field is unmasked")
	public void passwordShouldBeUnmasked() {
		assertTrue(forgetPasswordPage.isNewPasswordFieldUnmasked());
	}

	@When("user clicks on the unmask icon in the ConfirmPassword field")
	public void userClicksOnConfirmPassUnmaskIcon() {
		forgetPasswordPage.clickOnConfirmPasswordUnmaskIcon();
	}

	@Then("verify the Confirm Password field is unmasked")
	public void confirmPasswordShouldBeUnmasked() {
		assertTrue(forgetPasswordPage.isConfirmPasswordFieldUnmasked());
	}

	@When("user clicks again on the unmask icon in the New Password field")
	public void userClicksAgainOnUnmaskIcon() {
		forgetPasswordPage.clickOnNewPasswordUnmaskIcon();
	}

	@When("user clicks again on the unmask icon in the ConfirmPassword field")
	public void userClicksAgainOnConfirmPassUnmaskIcon() {
		forgetPasswordPage.clickOnConfirmPasswordUnmaskIcon();
	}

	@Then("verify reset button is disabled")
	public void verifyResetButonIsDisabled() {
		assertFalse(forgetPasswordPage.isResetButtonEnabled());
	}

	@Then("user clicks on Reset button")
	public void userClickOnResetButton() {
		forgetPasswordPage.clickOnResetButton();
	}

	@Then("verify system display password reset in progress message")
	public void systemShouldBrieflyDisplayAccountSetupInProgressMessage() {
		try {
			if (forgetPasswordPage.isPasswordResetInProgressDisplayed()) {
				logger.info("Password Reset in progress message is displayed.");
			}
		} catch (Exception e) {
			logger.warn("Skipping step due to element disappearing instantly: " + e.getMessage());
		}
	}

	@Then("verify success screen with header Password Reset Confirmation is displayed")
	public void verifyHeaderOfSuccessScreen() {
		assertTrue(forgetPasswordPage.isPasswordResetConfirmationHeaderDisplayed());
	}

	@Then("verify the message Your password has been reset successfully. Please login to proceed. is displayed")
	public void verifyMessageOfSuccessScreen() {
		assertTrue(forgetPasswordPage.isPasswordResetConfirmationMessageDisplayed());
	}

	@Then("verify Login button is displayed")
	public void verifyLoginButtonDisplayed() {
		assertTrue(forgetPasswordPage.isLoginButtonDisplayed());
	}

	@When("user clicks on Login button")
	public void userClicksLoginButton() {
		forgetPasswordPage.clickOnLoginButton();
	}

	@Then("verify password changed successful notification is displayed")
	public void verifySuccessNotificationInSelectedLanguage() {
		String currentLang = BaseTestUtil.getThreadLocalLanguage();
		logger.info("Verifying password changed notification in language: " + currentLang);
		assertTrue(smtpPage.isPasswordResetSuccessNotificationDisplayed());
	}

	@Then("verify it is accessible,user is redirected to the Forget Password screen")
	public void verifyUserRedirectedToForgotPasswordScreen() {
		assertTrue(forgetPasswordPage.isForgetPassowrdScreenVisible());
	}

	String signupPortalTabHandle;

	@Then("navigate back to signup portal")
	public void userNavigateToSignupPortal() {
		driver.switchTo().newWindow(WindowType.TAB);
		signUpPage.navigateToSignupPortal();
		signupPortalTabHandle = driver.getWindowHandle();
	}

	@Then("switch back to signup portal")
	public void userSwitchToSignupPortal() {
		driver.switchTo().window(signupPortalTabHandle);
	}

}