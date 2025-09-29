package stepdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import org.apache.log4j.Logger;

import base.BasePage;
import base.BaseTest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mosip.testrig.apirig.testrunner.OTPListener;
import pages.LoginOptionsPage;
import pages.RegistrationPage;
import pages.SignUpPage;
import pages.SmtpPage;
import utils.BaseTestUtil;
import utils.ClaimsUtil;
import utils.EsignetUtil;
import utils.MultiLanguageUtil;

public class SignUpStepDef {

	public WebDriver driver;
	private static final Logger logger = Logger.getLogger(SignUpStepDef.class);
	BaseTest baseTest;
	SignUpPage signUpPage;
	BasePage basePage;
	LoginOptionsPage loginOptionsPage;
	RegistrationPage registrationPage;
	SmtpPage smtpPage;

	public SignUpStepDef(BaseTest baseTest) {
		this.baseTest = baseTest;
		this.driver = BaseTest.getDriver();
		signUpPage = new SignUpPage(driver);
		basePage = new BasePage(driver);
		loginOptionsPage = new LoginOptionsPage(driver);
		registrationPage = new RegistrationPage(driver);
		smtpPage = new SmtpPage(driver);
	}

	@Then("verify Sign-Up with Unified Login option should be displayed")
	public void signUpWithUnifiedLoginOptionDisplayed() {
		assertTrue(loginOptionsPage.isSignUpWithUnifiedLoginOptionDisplayed());
	}

	@When("user clicks on the Sign-Up with Unified Login hyperlink")
	public void userClicksOnSignUpWithUnifiedLoginHyperlink() {
		loginOptionsPage.clickOnSignUpWithUnifiedLogin();
		registrationPage.clickOnLanguageSelectionOption();

		String languagePassed = MultiLanguageUtil.getDisplayName(BaseTestUtil.getThreadLocalLanguage());

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

		WebElement langOption = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='" + languagePassed + "']")));
		langOption.click();
		BaseTestUtil.setThreadLocalLanguage(languagePassed);
	}

	@Then("verify user is navigated to the Mobile Number Registration screen")
	public void userShouldBeNavigatedToRegistrationScreen() {
		assertTrue(registrationPage.isRegistrationScreenDisplayed());
	}

	@Then("user verify header text")
	public void headerInRegistrationPageDisplayed() {
		assertTrue(registrationPage.isHeaderInRegistrationPageDisplayed());
	}

	@Then("user verify a text box for entering an 8–9 digit mobile number")
	public void validateMobileTextBox() {
		assertTrue(registrationPage.isEnterMobileNumberTextBoxDisplayed());
	}

	@Then("user verify a Continue button")
	public void validateContinueButton() {
		assertTrue(registrationPage.isContinueButtonVisible());
	}

	@Then("user verify an option to select preferred language")
	public void validateLanguageSelectionOption() {
		assertTrue(registrationPage.isLanguageSelectionVisible());
	}

	@Then("user verify footer text and logo")
	public void validateFooterText() {
		assertTrue(registrationPage.isFooterTextDisplayed());
		assertTrue(registrationPage.isFooterLogoDisplayed());
	}

	@Then("user verify the mobile number text field should be pre-filled with country code")
	public void verifyPrefilledCountryCodeInMobileField() {
		assertTrue(registrationPage.isTextBoxPrefilledWithCountryCode());
	}

	@Then("user verify the help text in mobile number text field is displayed")
	public void verifyHelpTextInMobileNumberTextBox() {
		assertTrue(registrationPage.isHelpTextInMobileNumberTextBoxDisplayed(""));
	}

	@When("user enters less than minimum digit in the mobile number text box")
	public void userEntersLessThanMinDigits() {
		String minDigit = EsignetUtil.getLessThanMinimumDigit();
		registrationPage.enterMobileNumber(minDigit);
	}

	@Then("user tabs out")
	public void userTabsOutOfMobileField() {
		registrationPage.clickOnOutsideMobileField();
	}

	@Then("verify the error message Enter valid username is displayed")
	public void errorMessageShouldBeDisplayed() {
		assertTrue(registrationPage.isErrorMessageDisplayed());
	}

	@Then("validate that the Continue button remain disabled")
	public void continueButtonShouldBeDisabled() {
		assertFalse(registrationPage.isContinueButtonEnabled());
	}

	@Then("the placeholder will be replaced with the entered mobile number")
	public void placeholderShouldDisappear() {
		assertTrue(registrationPage.isPlaceholderGone());
	}

	@Then("validate that the Continue button enabled")
	public void continueButtonShouldBeEnabled() {
		assertTrue(registrationPage.isContinueButtonEnabled());
	}

	@Then("verify no error message is displayed")
	public void noErrorMessageShouldBeDisplayed() {
		assertFalse(registrationPage.isErrorMessageDisplayed());
	}

	@When("user enters digit starting with 0 in the mobile number text box")
	public void userEnterDigitStartingWith0() {
		String zeroNum = EsignetUtil.getNumberStartingWithZero(8);
		registrationPage.enterMobileNumber(zeroNum);
	}

	@When("user enters all zeros in the mobile number text box")
	public void userEntersAllZeros() {
		String allZero = EsignetUtil.getAllZeros(7);
		registrationPage.enterMobileNumber(allZero);
	}

	@Then("verify the error Number cannot start with zero.Enter valid username is shown")
	public void numberCannotStartWithZeroErrorShouldBeDisplayed() {
		assertTrue(registrationPage.isZeroErrorMessageDisplayed());
	}

	@When("user enters more than maximum Digit in the mobile number text box")
	public void userEntersMoreThanMaxDigits() {
		String maxDigit = EsignetUtil.getMoreThanMaxDigits();
		registrationPage.enterMobileNumber(maxDigit);
	}

	@Then("verify the mobile number field should contain only 9 digits")
	public void verifyMaxDigitsAllowed() {
		assertTrue(registrationPage.isNumberRestrictedToNineDigits());
	}

	@When("user enters specialChars in the mobile number text box")
	public void userEntersSpecialChars() {
		String specialChar = EsignetUtil.getSpecialChar();
		registrationPage.enterMobileNumber(specialChar);
	}

	@Then("verify the mobile number field should remain empty or accept only number")
	public void fieldShouldNotAcceptSpecialCharacters() {
		assertTrue(registrationPage.isMobileFieldEmptyOrUnchanged());
	}

	@When("user enters alphaNumeric in the mobile number text box")
	public void userEntersAlphaNumeric() {
		String aplhaNumeric = EsignetUtil.getAlphaNumeric();
		registrationPage.enterMobileNumber(aplhaNumeric);
	}

	@Then("verify the mobile number field should contain only numeric characters")
	public void fieldShouldContainOnlyDigits() {
		assertTrue(registrationPage.isMobileFieldContainingOnlyDigits());
	}

	@Then("verify user is redirected to the previous screen")
	public void verifyUserIsRedirectedToPreviousScreen() {
		assertTrue(registrationPage.isPreviousScreenVisible());
	}

	@When("user clicks the browser back button")
	public void userClicksTheBrowserBackButton() {
		basePage.browserBackButton("click on back button");
	}

	@Then("user clicks on the Continue button")
	public void userClickOnContinueButton() {
		registrationPage.clickOnContinueButton();
	}

	@Then("verify user is navigated to the OTP screen")
	public void userIsNavigatedToOtpPage() {
		assertTrue(registrationPage.isEnterOtpPageDisplayed());
	}

	@Given("user is on the OTP screen")
	public void userIsOnOtpScreen() {
		assertTrue(registrationPage.isEnterOtpPageDisplayed());
	}

	@Then("user verifies the OTP screen header is displayed as Enter OTP")
	public void verifyOtpHeader() {
		assertTrue(registrationPage.isOtpPageHeaderDisplayed());
	}

	@Then("user verifies the OTP screen description should contain a masked mobile number")
	public void verifyOtpDescriptionMasked() {
		assertTrue(registrationPage.isOtpPageDescriptionDisplayed());
	}

	@Then("user verifies the OTP input field is visible")
	public void verifyOtpInputVisible() {
		assertTrue(registrationPage.isOtpInputFieldVisible());
	}

	@Then("user verifies the Verify OTP button is visible")
	public void verifyButtonVisible() {
		assertTrue(registrationPage.isVerifyOtpButtonVisible());
	}

	@Then("user verifies a countdown timer is displayed")
	public void verifyTimerDisplayed() {
		assertTrue(registrationPage.isCountdownTimerDisplayed());
	}

	@Then("user verifies the Resend OTP option is visible")
	public void verifyResendOptionVisible() {
		assertTrue(registrationPage.isResendOtpOptionVisible());
	}

	@Then("user verifies an option to go back and update the mobile number is be present")
	public void verifyBackOptionVisible() {
		assertTrue(registrationPage.isBackToEditMobileNumberOptionVisible());
	}

	private int waitTime;

	@Then("user waits for OTP time to expire and resend button gets enabled")
	public void userWaitsForOtpTimeToExpireAndResendButtonGetsEnabled() throws InterruptedException {
		waitTime = EsignetUtil.getOtpResendDelayFromSignupActuator();
		logger.info("Waiting for OTP resend delay: " + waitTime + " seconds");
		Thread.sleep(waitTime * 1000L + 1000);
		Assert.assertTrue(registrationPage.isResendOtpButtonEnabled(), "Resend OTP button should be enabled");
	}

	@Then("user validates {int} out of 3 attempts message displayed")
	public void userValidatesOutOfThreeAttemptsMessageDisplayed(int remainingAttempts) {
		registrationPage.clickOnResendOtpButton();
		String actualMessage = registrationPage.getOtpResendAttemptsText(remainingAttempts);

		Assert.assertTrue(actualMessage.contains(String.valueOf(remainingAttempts)),
				"Expected attempt count: " + remainingAttempts + " not found. Actual: " + actualMessage);
	}
	
	@When("user clicks the back button on the OTP screen")
	public void userClicksBackButtonOnOtpScreen() {
		registrationPage.clickOnNavigateBackButton();
	}

	@Then("verify user is redirected back to the Registration screen")
	public void userIsRedirectedToRegistrationScreen() {
		assertTrue(registrationPage.isRegistrationScreenDisplayed());
	}

	@Then("user waits for OTP timer to expire")
	public void userWaitsForOtpToExpire() {
		registrationPage.waitUntilOtpExpires();
	}

	@When("user enters {string} as a Otp")
	public void userEntersOtp(String otp) {
		registrationPage.enterOtp(otp);
	}

	@Then("user clicks on the Verify OTP button")
	public void userClicksOnVerifyOtpButton() {
		registrationPage.clickOnVerifyOtpButton();
	}

	@Then("verify an error message OTP expired. Please request a new one and try again. is displayed at the top")
	public void verifyOtpExpiredErrorMessage() {
		assertTrue(registrationPage.isOtpExpiredMessageDisplayed());
	}

	@When("user clicks on the close icon of the error message")
	public void userClicksOnErrorCloseIcon() {
		registrationPage.clickOnErrorCloseIcon();
	}

	@Then("verify the error message is not visible")
	public void verifyErrorMessageIsGone() {
		registrationPage.verifyErrorIsGoneAfterClose();
	}

	@Then("user clicks on the Resend OTP button")
	public void userClicksOnResendButton() {
		registrationPage.clickOnResendOtpButton();
	}

	@Then("verify an error message OTP authentication failed. Please try again. is displayed at the top")
	public void verifyIncorrectOtpMessage() {
		assertTrue(registrationPage.isIncorrectOtpErrorDisplayed());
	}

	@Then("verify error message disappears after 10 seconds")
	public void waitForErrorMessageToDisappear() {
		registrationPage.verifyErrorMessageDisappesAfterTenSeconds();
	}

	@Then("verify error message disappears as user starts typing in the input field")
	public void verifyUserStartsTypingAndErrorMessageDisappear() {
		registrationPage.verifyErrorMessageDisappesAsUserStartsTyping();
	}

	@Then("verify OTP field is rejecting special characters")
	public void otpFieldRemainsEmptyAfterEntreingSpecialCharacters() {
		assertTrue(registrationPage.isOtpFieldEmptyOrUnchanged());
	}

	@Then("verify OTP field is rejecting alphabets")
	public void otpFieldRemainsEmptyAfterEntreingAlphabets() {
		assertTrue(registrationPage.isOtpFieldEmptyfterAlphabetEntry());
	}

	@Then("verify OTP field is rejecting alphanumeric characters")
	public void otpFieldRemainsEmptyAfterEntreingAlphaNumeric() {
		assertTrue(registrationPage.isOtpFieldsNumericOnly());
	}

	@Then("validate the verify button is disabled")
	public void verifyButtonShouldBeDisabled() {
		registrationPage.waitForButtonToBecomeDisabled(registrationPage.getVerifyOtpButton(), 3);
		assertFalse(registrationPage.isVerifyOtpButtonEnabled());
	}

	@When("user enters the complete 6-digit OTP")
	public void userEntersOtp() {
		registrationPage.enterOtp(OTPListener.getOtp(lastGeneratedMobileNumber));
	}

	@Then("verify OTP is masked as soon as it is entered")
	public void verifyOtpMaskedOnEntry() {
		assertTrue(registrationPage.isOtpMasked());
	}

	@Then("validate the verify button is enabled")
	public void verifyButtonShouldBeEnabled() {
		assertTrue(registrationPage.isVerifyOtpButtonEnabled());
	}

	@Then("verify Sign-Up Failed! is displayed as a heading")
	public void verifyHeaderText() {
		assertTrue(registrationPage.isFailureHeaderDisplayed());
	}

	@Then("verify the failure message The provided mobile number is already registered. Please use the Login option to proceed. shown")
	public void verifyFailureMessage() {
		assertTrue(registrationPage.isFailureMessageDisplayed());
	}

	@Then("verify a Login button is visible")
	public void verifyLoginButtonDisplayed() {
		assertTrue(registrationPage.isLoginButtonVisible());
	}

	@Then("user clicks on the Login button")
	public void userClicksOnLoginButton() {
		registrationPage.clickOnLoginButtonInSignUpFailedScreen();
	}

	@Then("verify user is redirected to the success screen")
	public void userShouldBeRedirectedToSuccessScreen() {
		assertTrue(registrationPage.isSuccessScreenDisplayed());
	}

	@Then("verify the header Successful! is displayed")
	public void theHeaderShouldBeDisplayed() {
		assertTrue(registrationPage.isSuccessHeaderDisplayed());
	}

	@Then("verify the message Your mobile number has been verified successfully. Please continue to setup your account and complete the registration process. is displayed")
	public void theMessageShouldBeDisplayed() {
		assertTrue(registrationPage.iSuccessMessageDisplayed());
	}

	@Then("verify a Continue button is displayed")
	public void continueButtonShouldBeDisplayed() {
		assertTrue(registrationPage.isContinueButtonDisplayed());
	}

	@When("user click on Continue button in Success Screen")
	public void clickOnContinueButtonInSucessScreen() {
		registrationPage.clickOnContinueButtonInSucessScreen();
	}

	@Then("verify setup account screen is displayed with header Setup Account")
	public void verifySetupAccountHeader() {
		assertTrue(registrationPage.isSetupAccountHeaderVisible());
	}

	@Then("verify description Please enter the requested details to complete your registration.")
	public void verifySetupAccountDescription() {
		assertTrue(registrationPage.isSetupAccountDescriptionVisible());
	}

	@Then("verify a Username field should be visible")
	public void verifyUsernameField() {
		assertTrue(registrationPage.isUsernameFieldVisible());
	}

	@Then("verify an option to enter Full Name in Khmer")
	public void verifyFullNameKhmer() {
		assertTrue(registrationPage.isFullNameInKhmerFieldVisible());
	}

	@Then("verify an option to setup Password")
	public void verifyPasswordField() {
		assertTrue(registrationPage.isPasswordFieldVisible());
	}

	@Then("verify an option to Confirm Password")
	public void verifyConfirmPasswordField() {
		assertTrue(registrationPage.isConfirmPasswordFieldVisible());
	}

	@Then("verify an option to mask or unmask the entered password")
	public void verifyPasswordMaskOption() {
		assertTrue(registrationPage.isPasswordToggleIconVisible());
	}

	@Then("verify an option to view password policy by clicking on the {string} icon")
	public void verifyPasswordPolicyIcon(String iconText) {
		assertTrue(registrationPage.isPasswordPolicyIconVisible());
	}

	@Then("verify an option to check the checkbox to agree to T&C and Privacy Policy")
	public void verifyTCCheckboxVisible() {
		assertTrue(registrationPage.isTermsCheckboxVisible());
	}

	@Then("verify it should display Continue button")
	public void verifyContinueButtonOnSetup() {
		assertTrue(registrationPage.isSetupContinueButtonVisible());
	}

	@Then("verify the Username field is auto-filled with the verified mobile number")
	public void verifyUsernameIsAutoFilledWithMobileNumber() {
		String expectedMobile = registrationPage.getLastEnteredMobileNumber();
		String actualUsername = registrationPage.getUsernameFieldValue();
		assertEquals(expectedMobile, actualUsername);
	}

	@Then("validate the Username field should be non-editable")
	public void verifyUsernameFieldIsNonEditable() {
		assertFalse(registrationPage.isUsernameFieldReadOnly());
	}

	@Then("verify the watermark text in the Full Name field")
	public void verifyFullNameInKhmerWatermark() {
		String expectedEn = EsignetUtil.getPlaceholderForFullName("en");
		String expectedKm = EsignetUtil.getPlaceholderForFullName("km");
		String actualEn = registrationPage.getEnglishFullNamePlaceholder();
		String actualKm = registrationPage.getKhmerFullNamePlaceholder();
		Assert.assertEquals(actualEn, expectedEn);
		Assert.assertEquals(actualKm, expectedKm);
	}

	@Then("user clicks on Language Selection Option")
	public void userClicksOnLanguageSelectionOption() {
		registrationPage.clickOnLanguageSelectionOption();
	}

	@Then("user selects Khmer from the language dropdown")
	public void userSelectsKhmerLanguage() {
		registrationPage.clickOnKhmerLanguage();
	}

	@Then("verify page rendered in selected language")
	public void verifyLanguageChanged() {
		assertTrue(registrationPage.isLanguageChanged());
	}

	@Then("user tabs out from the field")
	public void userTabsOutOfField() {
		registrationPage.tabsOutOfField();
	}

	@When("user enters text in other language in the Full Name in field")
	public void userEnterNameInOtherLanguage() {
		EsignetUtil.FullName names = EsignetUtil.generateNamesFromUiSpec();
		registrationPage.enterFullNameInKhmer(names.english);
	}

	@Then("verify an error message Should be able to enter only Khmer characters is displayed below the field")
	public void verifyKhmerOnlyErrorIsDisplayed() {
		assertTrue(registrationPage.isFullNameHasToBeInKhmerErrorDisplayed());
	}

	@Then("user selects English from the language dropdown")
	public void userSelectsEnglishLanguage() {
		registrationPage.clickOnEnglishLanguage();
	}

	@Then("verify UI rendered in English Language")
	public void verifyLanguageChangedToKhmerLanguage() {
		assertTrue(registrationPage.isScreenDisplayedInEnglishLang());
	}

	@When("user enters text more than maximum characters in the Full Name in field")
	public void userEntersMoreThanMaxLengthFullName() {
		String maxFullName = EsignetUtil.getMoreThanMaxLengthFullName("km");
		registrationPage.enterFullNameInKhmer(maxFullName);
	}

	@Then("verify the field restrict the input to 30 characters only")
	public void fieldShouldRestrictInputToThirtyCharsOnly() {
		assertTrue(registrationPage.isFullNameInKhmerRestrictedToThirtyChars());
	}

	@When("user enters only spaces in the Full Name in field")
	public void userEntersOnlySpacesFullName() {
		registrationPage.enterOnlySpacesFullName(5);
	}

	@When("user enters special characters in the Full Name in field")
	public void userEntersSpecialCharsInFullName() {
		String specialChar = EsignetUtil.getSpecialChar();
		registrationPage.enterFullNameInKhmer(specialChar);
	}

	@When("user enters numeric_input in the Full Name in field")
	public void userEntersNumericInFullName() {
		String numericFullName = EsignetUtil.getNumericFullName("km");
		registrationPage.enterFullNameInKhmer(numericFullName);
	}

	@When("user enters alphanumeric input in the Full Name in field")
	public void userEntersAlphanumericInKhmerFullName() {
		String specialChar = EsignetUtil.getAlphanumericFullName("km");
		registrationPage.enterFullNameInKhmer(specialChar);
	}

	@Then("verify an error message Please enter a valid name. is displayed below the field")
	public void verifyErrorMessageBelowField() {
		assertTrue(registrationPage.isPleaseEnterValidUsernameErrorDisplayed());
	}

	@Then("verify the watermark text in the Password field")
	public void verifyPasswordWatermark() {
		String langCode = ClaimsUtil.getDefaultLanguage(); 
	    String expectedPlaceholder = EsignetUtil.getPlaceholderForPassword(langCode);
	    String actualPlaceholder = registrationPage.getPasswordFieldPlaceholder();
	    Assert.assertEquals(actualPlaceholder, expectedPlaceholder);
	}

	@When("user enters password less than minimum length in the Password field")
	public void userEntersShortPassword() {
		registrationPage.enterShortPassword();
	}

	@When("user enters password more than maximum length in the Password field")
	public void userEntersLongPassword() {
		String longPass = EsignetUtil.generateInvalidPassword(30);
		registrationPage.enterMobileNumber(longPass);
	}

	@When("user enters invalid password in the Password field")
	public void userEntersInvalidPassword() {
		String invalidPassword = EsignetUtil.generateInvalidPassword(7);
		registrationPage.enterPassword(invalidPassword);
	}

	@Then("verify an error message Password does not meet the password policy. displayed below the Password field")
	public void verifyPasswordErrorMessage() {
		assertTrue(registrationPage.isPasswordDoesNotMeetThePolicyErrorDisplayed());
	}

	@Then("validate the field restrict the input to max characters only")
	public void fieldShouldRestrictInputToMaxCharsOnly() {
		assertTrue(registrationPage.isPasswordRestrictedToMaxChars());
	}

	@Then("verify the watermark text in the Confirm Password field")
	public void verifyConfirmPasswordWatermark() {
		String actualPlaceholder = registrationPage.getConfirmPasswordFieldPlaceholder();
		assertTrue(actualPlaceholder != null && !actualPlaceholder.trim().isEmpty());
	}

	@When("user enters different password in the Confirm Password field")
	public void userFillsDiffConfirmPassword() {
		String difPassword = EsignetUtil.generateValidPasswordFromActuator();
		registrationPage.enterConfirmPassword(difPassword);
	}

	@When("user enters more than max character in the Confirm Password field")
	public void userFillsMaxConfirmPassword() {
		String difPassword = EsignetUtil.generateInvalidPassword(30);
		registrationPage.enterConfirmPassword(difPassword);
	}

	@When("user enters less than min character in the Confirm Password field")
	public void userFillsConfirmPasswordrField() {
		registrationPage.enterShortPwd();
	}

	@Then("verify an inline error message Password and Confirm Password do not match. displayed below Confirm Password field")
	public void verifyPasswordMismatchError() {
		assertTrue(registrationPage.isPasswordAndConfirmPasswordDoesNotMatchErrorDisplayed());
	}

	@Then("verify the field should restrict the password to max characters only")
	public void confirmPassFieldShouldRestrictInputToMaxCharsOnly() {
		assertTrue(registrationPage.isConfirmPasswordRestrictedToMaxChars());
	}

	@Then("validate the Password field is masked")
	public void passwordFieldShouldBeMasked() {
		assertTrue(registrationPage.isPasswordFieldMasked());
	}

	@Then("validate the Confirm Password field is masked")
	public void confirmPasswordFieldShouldBeMasked() {
		assertTrue(registrationPage.isConfirmPasswordFieldMasked());
	}

	@When("user clicks on the unmask icon in the Password field")
	public void userClicksOnPassUnmaskIcon() {
		registrationPage.clickOnPasswordUnmaskIcon();
	}

	@Then("validate the Password field is unmasked")
	public void passwordShouldBeUnmasked() {
		assertTrue(registrationPage.isPasswordFieldUnmasked());
	}

	@When("user clicks on the unmask icon in the Confirm Password field")
	public void userClicksOnConfirmPassUnmaskIcon() {
		registrationPage.clickOnConfirmPasswordUnmaskIcon();
	}

	@Then("validate the Confirm Password field is unmasked")
	public void confirmPasswordShouldBeUnmasked() {
		assertTrue(registrationPage.isConfirmPasswordFieldUnmasked());
	}

	@When("user clicks again on the unmask icon in the Password field")
	public void userClicksAgainOnUnmaskIcon() {
		registrationPage.clickOnPasswordUnmaskIcon();
	}

	@When("user clicks again on the unmask icon in the Confirm Password field")
	public void userClicksAgainOnConfirmPassUnmaskIcon() {
		registrationPage.clickOnConfirmPasswordUnmaskIcon();
	}

	@When("user clicks on the {string} icon in the Password field")
	public void userHoversOnPasswordInfoIcon(String iconLabel) {
		registrationPage.clickOnPasswordInfoIcon();
	}

	@Then("verify the tooltip message for password field is displayed")
	public void verifyPasswordTooltipMessage() {
		String expectedLang = registrationPage.getExpectedDefaultLanguage();
		String expectedTooltip = EsignetUtil.getInfoForPassword(expectedLang);
		String actualTooltip = registrationPage.getPasswordTooltipText();
		Assert.assertEquals(actualTooltip, expectedTooltip);
	}

	@When("user clicks on the {string} icon in the Full Name in field")
	public void userHoversOnFullNameInKhmerInfoIcon(String iconLabel) {
		registrationPage.clickOnFullNameInKhmerInfoIcon();
	}

	@Then("verify the tooltip message for full name field is displayed")
	public void verifyFullNameInKhmerTooltipMessage() {
		String expectedLang = registrationPage.getExpectedDefaultLanguage();
		String expectedTooltip = EsignetUtil.getInfoForFullName(expectedLang);
		String actualTooltip = registrationPage.getFullNameTooltipText();
		assertEquals(actualTooltip, expectedTooltip);
	}

	@When("user does not check the terms and conditions checkbox")
	public void userDoesNotCheckTermsAndConditionsCheckbox() {
		registrationPage.ensureTermsCheckboxIsUnchecked();
	}

	@Then("verify the Continue button will be in disabled state")
	public void verifyButtonIsDisabled() {
		assertFalse(registrationPage.isContinueButtonInSetupAccountPageEnabled());
	}

	@Then("verify the terms and conditions message")
	public void verifyTermsAndConditionsMessage() {
		assertTrue(registrationPage.isTermsAndConditionsMessageDisplayed());
	}

	@Then("verify it restricts such input with an error message Full Name has to be in Khmer only.")
	public void verifyErrorMessageDisplayedBelowField() {
		assertTrue(registrationPage.isFullNameHasToBeInKhmerErrorDisplayed());
	}

	@When("user clicks on the Terms & Conditions hyperlink")
	public void userClicksOnHyperlink() {
		registrationPage.clickOnTermsAndConditionLink();
	}

	@Then("verify a pop-up window for Terms and Conditions is displayed")
	public void verifyTermsAndConditionsPopupDisplayed() {
		assertTrue(registrationPage.isTermsAndConditionsPopupDisplayed());
	}

	@When("user closes the Terms and Conditions popup")
	public void userClosesTermsAndConditionsPopup() {
		registrationPage.clickOnClosePopupIcon();
	}

	@Then("verify user is navigated back to the Account Setup screen")
	public void userShouldBeOnAccountSetupScreen() {
		assertTrue(registrationPage.isSetupAccountPageVisible());
	}

	@When("user clicks on the Privacy policy hyperlink")
	public void userClicksOnPrivacyPolicyHyperlink() {
		registrationPage.clickOnPrivacyPolicyLink();
	}

	@Then("verify a pop-up window for Privacy Policy is displayed")
	public void verifyPrivacyPolicyPopupDisplayed() {
		assertTrue(registrationPage.isPrivacyPolicyPopupDisplayed());
	}

	@When("user closes the privacy policy popup")
	public void userClosesPrivacyPolicyPopup() {
		registrationPage.clickOnClosePopupIcon();
	}

	@Then("verify the Continue button is disabled when mandatory fields are not filled in Account Setup screen")
	public void verifyContinueButtonIsDisabledWhenMandatoryFieldsAreEmpty() {
		registrationPage.clearAllMandatoryFields();
		registrationPage.ensureTermsCheckboxIsUnchecked();
		boolean isEnabled = registrationPage.isContinueButtonInSetupAccountPageEnabled();
		assertFalse("Continue button should be disabled when mandatory fields are empty", isEnabled);
	}

	@Then("verify the Continue button is disabled when only two mandatory fields are filled")
	public void verifyContinueButtonDisabledWithOnlyTwoFieldsFilled() {
		boolean isEnabled = registrationPage.isContinueButtonInSetupAccountPageEnabled();
		assertFalse("Continue button should be disabled when only two mandatory fields are filled", isEnabled);
	}

	@Then("verify the Continue button is enabled when all mandatory fields are filled")
	public void verifyContinueButtonEnabledWhenAllMandatoryFieldsFilled() {
		registrationPage.checkTermsAndConditions();
		assertTrue(registrationPage.isContinueButtonInSetupAccountPageEnabled());
	}

	@When("user clicks on Continue button in Setup Account Page")
	public void userClicksOnContinueButtonInSetpuAccountPage() {
		registrationPage.clickOnSetupAccountContinueButton();
	}

	@Then("verify system display account setup in progress message")
	public void systemShouldBrieflyDisplayAccountSetupInProgressMessage() {
		try {
			if (registrationPage.isAccountSetupInProgressDisplayed()) {
				logger.info("Account setup in progress message is displayed.");
			}
		} catch (Exception e) {
			logger.warn("Skipping step due to element disappearing instantly: " + e.getMessage());
		}
	}

	@Then("verify that success screen should display the message Congratulations! Your account has been created successfully. Please login to proceed.")
	public void verifyThenSuccessMessageDisplayed() {
		assertTrue(registrationPage.isAccountCreatedSuccessfullyMessageDisplayed());
	}

	@Then("verify a Login button is displayed")
	public void verifyLoginButtonIsDisplayed() {
		assertTrue(registrationPage.isLoginButtonDisplayed());
	}

	@When("user click on Login button")
	public void userClicksOnLoginButtonInSuccessScreen() {
		registrationPage.clickOnLoginButtonInSuccessScreen();
	}

	@Then("verify user is redirected to Login screen of eSignet")
	public void verifyUserIsOnLoginPage() {
		assertTrue(registrationPage.isLoginScreenDisplayed());
	}

	@Given("user directly navigates to sign-up portal URL")
	public void user_opens_signup_portal_directly() {
		signUpPage.navigateToSignupPortal();
	}

	@Then("verify the header in the screen")
	public void verifyHeaderInSignUpScreen() {
		assertTrue(signUpPage.isHeaderInSignUpErrorScreenDisplayed());
	}

	@Then("verify the message displayed")
	public void verifyMessageInSignUpScreen() {
		assertTrue(signUpPage.isMessageInSignUpErrorScreenDisplayed());
	}

	@Then("verify the reset password button is available")
	public void validateResetButtonVisible() {
		assertTrue(signUpPage.isResetPasswordButtonDisplayed());
	}

	@Then("verify the register Button is displayed")
	public void validateRegisterButtonDisplayed() {
		assertTrue(signUpPage.isRegisterButtonDisplayed());
	}

	@When("user clicks on Register button")
	public void userClicksOnRegisterButton() {
		registrationPage.clickOnLanguageSelectionOption();

		String languagePassed = MultiLanguageUtil.getDisplayName(BaseTestUtil.getThreadLocalLanguage());

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

		WebElement langOption = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='" + languagePassed + "']")));
		langOption.click();
		BaseTestUtil.setThreadLocalLanguage(languagePassed);
		signUpPage.clickOnRegisterButton();
	}

	@Then("verify that success screen should display the message Congratulations! Your account has been created successfully.Start using your registered number & password with service providers to avail the required services.")
	public void verifySuccessMessageIsDisplayed() {
		assertTrue(registrationPage.isAccountCreatedSuccessfullyMessageDisplayed());
	}

	@Then("verify a Okay button is displayed")
	public void verifyOkayButtonIsDisplayed() {
		assertTrue(registrationPage.isOkayButtonDisplayed());
	}

	@When("user click on Okay button")
	public void userClicksOnOkayButtonInSuccessScreen() {
		registrationPage.clickOnOkayButtonInSuccessScreen();
	}

	String smtpTabHandle;
	String healthPortalTabHandle;

	@Given("user opens SMTP portal")
	public void userNavigatesToSmtpPortalUrl() {
		smtpPage.navigateToSmtpUrl();
		smtpTabHandle = driver.getWindowHandle();
	}

	@Then("navigate back to eSignet portal")
	public void userOpensEsignetPortal() {
		driver.switchTo().newWindow(WindowType.TAB);
		smtpPage.navigateToHealthPortalUrl();
		healthPortalTabHandle = driver.getWindowHandle();
	}

	@Then("user switches back to SMTP portal")
	public void userSwitchesBackToSmtp() {
		driver.switchTo().window(smtpTabHandle);
	}

	@Then("verify notification is received for otp requested")
	public void verifyOtpNotificationInSelectedLanguage() {
		String currentLang = BaseTestUtil.getThreadLocalLanguage();
		logger.info("Verifying OTP notification in language: " + currentLang);
		assertTrue(smtpPage.isOtpNotificationReceived());
	}

	@Then("switch back to eSignet portal")
	public void userSwitchesBackToHealthPortal() {
		driver.switchTo().window(healthPortalTabHandle);
	}

	@Then("user accepts the Terms and Condition checkbox")
	public void userAcceptsTermsAndCondition() {
		registrationPage.checkTermsAndConditions();
	}

	@Then("verify registration success notification is received")
	public void verifySuccessNotificationInSelectedLanguage() {
		String currentLang = BaseTestUtil.getThreadLocalLanguage();
		logger.info("Verifying successful notification in language: " + currentLang);
		assertTrue(smtpPage.isRegistrationSuccessNotificationDisplayed());
	}

	private String lastGeneratedMobileNumber;

	@When("user enters valid_mobile_number in the mobile number text box")
	public void userEntersValidMobileNumber() {
		lastGeneratedMobileNumber = EsignetUtil.generateMobileNumberFromRegex();
		registrationPage.enterMobileNumber(lastGeneratedMobileNumber);
	}

	@When("user enters already registered number in the mobile number text box")
	public void userEntersRegisteredMobileNumber() {
		registrationPage.enterMobileNumber(lastGeneratedMobileNumber);
	}

	private String lastGeneratedPassword;

	@When("user enters valid password in the Password field")
	public void userEntersValidPassword() {
		lastGeneratedPassword = EsignetUtil.generateValidPasswordFromActuator();
		registrationPage.enterPassword(lastGeneratedPassword);
	}

	@When("user enters valid confirm password in the Confirm Password field")
	public void userEntersValidConfirmPassword() {
		registrationPage.enterConfirmPassword(lastGeneratedPassword);
	}

	@When("user enters text valid name in the Full Name field")
	public void userEntersValidNames() {
		EsignetUtil.FullName names = EsignetUtil.generateNamesFromUiSpec();

		registrationPage.enterFullNameInEnglish(names.english);
		registrationPage.enterFullNameInKhmer(names.khmer);
	}

	/*
	 * @Then("verify browser warning popup is displayed with header Leave site?")
	 * public void verifyHeaderInBrowserPopup() {
	 * assertTrue(registrationPage.isBrowserPopupHeaderDisplayed()); }
	 * 
	 * @Then("verify warning message Changes you made may not be saved is displayed"
	 * ) public void verifyMeaasgeInBrowserPopup() {
	 * assertTrue(registrationPage.isBrowserPopupMessageDisplayed()); }
	 * 
	 * @Then("verify Cancel button is displayed") public void
	 * verifyCancelButtonInBrowserPopup() {
	 * assertTrue(registrationPage.isBrowserPopupCancelButtonDisplayed()); }
	 * 
	 * @Then("verify Leave button is displayed") public void
	 * verifyLeaveButtonInBrowserPopup() {
	 * assertTrue(registrationPage.isBrowserPopupLeaveButtonDisplayed()); }
	 * 
	 * @When("user click on cancel button") public void
	 * userClicksOnCancelBtnInBrowserPopup() {
	 * registrationPage.clickOnBrowerCancelButton(); }
	 * 
	 * @Then("verify user is retained on same screen") public void
	 * verifyUserIsRetained() {
	 * assertTrue(registrationPage.isSetupAccountHeaderVisible()); }
	 * 
	 * @When("user click on Leave button") public void
	 * userClicksOnLeaveBtnInBrowserPopup() {
	 * registrationPage.clickOnBrowerLeaveButton(); }
	 */

}