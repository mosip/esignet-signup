package pages;

import base.BasePage;
import utils.EsignetUtil;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import utils.EsignetConfigManager;

public class RegistrationPage extends BasePage {

	public RegistrationPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//div[@class='grow px-3 text-center font-semibold tracking-normal xs:px-2']")
	WebElement registrationScreen;

	@FindBy(xpath = "//div[@class='grow px-3 text-center font-semibold tracking-normal xs:px-2']")
	WebElement headerInRegistrationPage;

	@FindBy(id = "phone_input")
	WebElement enterMobileNumberTextBox;

	@FindBy(id = "continue-button")
	WebElement continueButton;

	@FindBy(id = "back-button")
	WebElement backButton;
  
	@FindBy(id = "register-button")
	WebElement registerButton;

	@FindBy(id = "language-select-button")
	WebElement languageSelection;

	@FindBy(xpath = "//*[@id='root']/div/div/div/footer/span")
	WebElement footerText;

	@FindBy(xpath = "//img[@class='footer-brand-logo']")
	WebElement footerLogo;

	@FindBy(xpath = "//div[contains(@id,'-form-item')]/span")
	WebElement prefilledCountryCode;

	@FindBy(id = "phone_input")
	WebElement helpTextInTextBox;

	@FindBy(id = ":r4:-form-item-message")
	WebElement numberCannotStartWithZeroErrorMessage;

	@FindBy(id = ":r4:-form-item-message")
	List<WebElement> enterValidUserNameError;

	@FindBy(id = "login-header")
	WebElement loginPageHeader;

	@FindBy(xpath = "//div[@class='w-full text-center text-[22px] font-semibold']")
	WebElement otpPage;

	@FindBy(xpath = "//div[@class='text-muted-neutral-gray']")
	WebElement otpPageDescription;

	@FindBy(xpath = "//div[@class='pincode-input-container']/input")
	List<WebElement> otpInputFields;

	@FindBy(xpath = "//div[@class='pincode-input-container']")
	WebElement otpInputField;

	@FindBy(id = "verify-otp-button")
	WebElement verifyOtpButton;

	@FindBy(xpath = "//div[@class='flex gap-x-1 text-center']")
	WebElement otpCountDownTimer;

	@FindBy(id = "resend-otp-button")
	WebElement resendOtpButton;

	@FindBy(xpath = "//div[@class='w-max rounded-md bg-[#FFF7E5] p-2 px-8 text-center text-sm font-semibold text-[#8B6105]']")
	WebElement remainingAttemptsMeassage;

	@FindBy(xpath = "//p[@class='truncate text-xs text-destructive']")
	WebElement otpExpiredError;

	@FindBy(xpath = "//p[@class='truncate text-xs text-destructive']")
	WebElement incorrectOtpError;

	@FindBy(id = "success_message_icon")
	WebElement successMessagePage;

	@FindBy(xpath = "//h1[@class='text-center text-2xl font-semibold']")
	WebElement successHeader;

	@FindBy(xpath = "//p[@class='text-center text-gray-500']")
	WebElement successMessage;

	@FindBy(id = "mobile-number-verified-continue-button")
	WebElement continueButtonInSuccessPage;

	@FindBy(xpath = "//h1[@class='text-center text-2xl font-semibold']")
	WebElement failureHeader;

	@FindBy(xpath = "//p[@class='text-center text-gray-500']")
	WebElement failureMessage;

	@FindBy(id = "signup-failed-okay-button")
	WebElement loginButtonInSignUpFailedScreen;

	@FindBy(id = "success-continue-button")
	WebElement loginButtonInSuccessScreen;

	@FindBy(id = "login-header")
	WebElement loginScreen;

	@FindBy(id = "cross_icon")
	WebElement errorCloseIcon;

	@FindBy(xpath = "//h3[@class='text-3xl font-medium leading-none tracking-tight']")
	WebElement setupAccountHeader;

	@FindBy(xpath = "//div[@class='text-center text-gray-500']")
	WebElement setupAccountDescription;

	@FindBy(id = "phone")
	WebElement usernameField;

	@FindBy(id = "fullName_eng")
	WebElement fullNameEnglishField;

	@FindBy(id = "fullName_khm")
	WebElement fullNameKhmerField;

	@FindBy(id = "password")
	WebElement passwordField;

	@FindBy(id = "password_confirm")
	WebElement confirmPasswordField;

	@FindBy(id = "password_eye")
	WebElement passwordToggleIcon;

	@FindBy(id = "password_confirm_eye")
	WebElement confirmPasswordToggleIcon;

	@FindBy(id = "info_FILL0_wght400_GRAD0_opsz48")
	WebElement passwordInfoIcon;

	@FindBy(id = "consent")
	WebElement termsAndConditionsCheckbox;

	@FindBy(xpath = "//button[@class='form-button']")
	WebElement setupContinueButton;

	@FindBy(id = "km_language")
	WebElement khmerLanguageSelection;

	@FindBy(xpath = "//span[@class='error-text']")
	WebElement fullNameHasToBeInKhmerOnlyError;

	@FindBy(id = "en_language")
	WebElement englishLanguageSelection;

	@FindBy(xpath = "//span[@class='error-text']")
	WebElement pleaseEnterValidNameError;

	@FindBy(xpath = "//div[@class='error-message']")
	WebElement passwordFieldError;

	@FindBy(id = "//div[@class='error-message']")
	WebElement confirmPasswordFieldError;

	@FindBy(id = "info_FILL0_wght400_GRAD0_opsz48")
	WebElement fullNameInKhmerInfoIcon;

	@FindBy(xpath = "//div[@class='info-detail active']")
	WebElement passwordFieldTooltipText;

	@FindBy(id = "//div[@class='info-detail active']")
	WebElement fullNameInKhmerTooltipText;

	@FindBy(xpath = "//label[@class='text-sm leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 font-medium']")
	WebElement messageToAcceptTermsAndCondition;

	@FindBy(xpath = "//span[@class='text-primary underline hover:cursor-pointer']")
	private WebElement termsAndConditionsLink;

	@FindBy(xpath = "//h3[@class='text-xl font-semibold text-gray-900 dark:text-gray-900']")
	private WebElement termsAndConditionsPopUp;

	@FindBy(id = "cross_icon")
	WebElement popupWindowCloseIcon;

	@FindBy(xpath = "//span[@class='text-primary underline hover:cursor-pointer']")
	private WebElement privacyPolicyLink;

	@FindBy(xpath = "//h3[@class='text-xl font-semibold text-gray-900 dark:text-gray-900']")
	private WebElement privacyPolicyPopUp;

	@FindBy(xpath = "//h1[@class='text-center text-2xl font-semibold']")
	private WebElement accountSetupInProgressMessage;

	@FindBy(xpath = "//p[@class='text-center text-gray-500']")
	private WebElement inProgressPleaseWaitMessage;

	@FindBy(xpath = "//div[@class='text-center text-lg font-semibold']")
	private WebElement accountCreatedSuccessfullyMessage;

	@FindBy(id = "username")
	WebElement screenInEnglishLanguage;

	public boolean isRegistrationScreenDisplayed() {
		return isElementVisible(registrationScreen, "Chcek if Registration screen displayed");
	}

	public boolean isHeaderInRegistrationPageDisplayed() {
		return isElementVisible(headerInRegistrationPage, "Chcek if Header In Registration Page Displayed");
	}

	public boolean isEnterMobileNumberTextBoxDisplayed() {
		return isElementVisible(enterMobileNumberTextBox, "Chcek if Mobile Number TextBox Displayed");
	}

	public boolean isContinueButtonVisible() {
		return isElementVisible(continueButton,"check continue button is displayed");

	}

	public boolean isLanguageSelectionVisible() {
		return isElementVisible(languageSelection, "Chcek if Lang selection is Visible");
	}

	public boolean isFooterTextDisplayed() {
		return isElementVisible(footerText, "Chcek if footer text is Visible");
	}

	public boolean isFooterLogoDisplayed() {
		return isElementVisible(footerLogo, "Chcek if footer logo is Visible");
	}

	public boolean isTextBoxPrefilledWithCountryCode() {
		return isElementVisible(prefilledCountryCode, "Chcek if Text Box Prefilled With CountryCode");
	}

	public boolean isHelpTextInMobileNumberTextBoxDisplayed(String expectedText) {
		String placeholder = getElementAttribute(helpTextInTextBox, "placeholder");
		return placeholder != null && !placeholder.isEmpty();
	}

	private String lastEnteredMobileNumber;

	public String getLastEnteredMobileNumber() {
		return lastEnteredMobileNumber;
	}

	public boolean isPlaceholderGone() {
		String value = getElementValue(enterMobileNumberTextBox,"Get value of Plachold Gone");
		return value != null && !value.isEmpty();
	}

	public void enterMobileNumber(String number) {
		enterMobileNumberTextBox.clear();
		enterText(enterMobileNumberTextBox, number, "Enterd Mobile Number");
		lastEnteredMobileNumber = number;
	}

	public void enterOtp(String otp) {
		for (WebElement field : otpInputFields) {
			field.click();
			field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
			field.sendKeys(Keys.BACK_SPACE);
		}
		for (int i = 0; i < otp.length(); i++) {
			WebElement field = otpInputFields.get(i);
			field.click();
			field.sendKeys(String.valueOf(otp.charAt(i)));
		}
	}

	public void clickOnOutsideMobileField() {
		clickOnElement(headerInRegistrationPage, "Click on Header on Registration page");
	}

	public boolean isContinueButtonEnabled() {
		return isButtonEnabled(continueButton, "Check if Continue Button is Enabled");
	}

	public boolean isErrorMessageDisplayed() {
		return !enterValidUserNameError.isEmpty() && enterValidUserNameError.get(0).isDisplayed();
	}

	public void verifyErrorIsGoneAfterClose() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
		wait.until(ExpectedConditions.invisibilityOf(otpExpiredError));
	}

	public void verifyErrorMessageDisappesAfterTenSeconds() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
		wait.until(ExpectedConditions.invisibilityOf(incorrectOtpError));
	}

	public void verifyErrorMessageDisappesAsUserStartsTyping() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
		wait.until(ExpectedConditions.invisibilityOf(incorrectOtpError));
	}

	public void clickOnContinueButton() {
		clickOnElement(continueButton, "Click on continue Button");
	}

	public boolean isZeroErrorMessageDisplayed() {
		return isElementVisible(numberCannotStartWithZeroErrorMessage, "Check if Zero Error Message Dispalyed");
	}

	public boolean isNumberRestrictedToNineDigits() {
		String value = getElementValue(enterMobileNumberTextBox,"Get value of Number Restricted");
		return value != null && value.length() == 9;
	}

	public boolean isMobileFieldEmptyOrUnchanged() {
		String value = getElementValue(enterMobileNumberTextBox,"Get Mobile Field Empty Or Unchanged value");
		return value == null || value.isEmpty();
	}

	public boolean isMobileFieldContainingOnlyDigits() {
		String value = getElementValue(enterMobileNumberTextBox,"Get Mobile Field and Verify only Contains Digits");
		return value != null && value.matches("\\d+");
	}

	public void clickOnNavigateBackButton() {
		clickOnElement(backButton, "Click on Navigation Back button");
	}

	public boolean isPreviousScreenVisible() {
		return isElementVisible(registerButton, "Check if Previous Screen is visible");
	}

	public boolean isEnterOtpPageDisplayed() {
		return isElementVisible(otpPage, "Check if OTP Screen is visible");
	}

	public boolean isOtpPageHeaderDisplayed() {
		return isElementVisible(otpPage, "Check if OTP Page Header is displayed");
	}

	public boolean isOtpPageDescriptionDisplayed() {
		return isElementVisible(otpPageDescription, "Check if OTP Page Descrition is displayed");
	}

	public boolean isOtpInputFieldVisible() {
		return isElementVisible(otpInputField, "Check if OTP input field is displayed");
	}

	public boolean isVerifyOtpButtonVisible() {
		return isElementVisible(verifyOtpButton, "Check if OTP Button is displayed");
	}

	public boolean isCountdownTimerDisplayed() {
		return isElementVisible(otpCountDownTimer, "Check if Count down timer Button is displayed");
	}

	public boolean isResendOtpOptionVisible() {
		return isElementVisible(resendOtpButton, "Check if Resend OTP Button is displayed");
	}

	public boolean isBackToEditMobileNumberOptionVisible() {
		return isElementVisible(backButton,"Back button is visible");
	}

	public void waitUntilOtpExpires() {
		int otpExpiry = EsignetUtil.getOtpResendDelayFromSignupActuator();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(otpExpiry));
		wait.until(ExpectedConditions.textToBePresentInElement(otpCountDownTimer, "00:00"));
	}

	public boolean isResendOtpButtonEnabled() {
		return isButtonEnabled(resendOtpButton, "Check if Resend Otp Button is Enabled" );
	}

	public void clickOnResendOtpButton() {
		clickOnElement(resendOtpButton,"click Resend Otp Button");
	}

	public void clickOnVerifyOtpButton() {
		clickOnElement(verifyOtpButton, "Click verify OTP Button");
	}

	public boolean isOtpExpiredMessageDisplayed() {
		return isElementVisible(otpExpiredError, "Check if OTP Expired Message Displayed");
	}

	public boolean isIncorrectOtpErrorDisplayed() {
		return isElementVisible(incorrectOtpError,"Check if incorrect OTP Message Displayed");
	}

	public boolean isOtpFieldEmptyOrUnchanged() {
		String value = getElementValue(otpInputField,"Get OTP Empty value");
		return value == null || value.isEmpty();
	}

	public boolean isOtpFieldEmptyfterAlphabetEntry() {
		String value = getElementValue(otpInputField,"Get empty Value after entering Alphabet");
		return value == null || value.isEmpty();
	}

	public boolean isOtpFieldsNumericOnly() {
		for (WebElement field : otpInputFields) {
			String value = getElementValue(field,"Get Value of OTP Value");
			if (value != null && !value.matches("\\d*")) {
				return false;
			}
		}
		return true;
	}

	public boolean isOtpMasked() {
		for (WebElement field : otpInputFields) {
			if (!"password".equalsIgnoreCase(getElementAttribute(field, "type"))) {
				return false;
			}
		}
		return true;
	}

	public void waitForButtonToBecomeDisabled(WebElement button, int timeoutSeconds) {
		new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(button)));
	}

	public WebElement getVerifyOtpButton() {
		return verifyOtpButton;
	}

	public boolean isVerifyOtpButtonEnabled() {
		return isButtonEnabled(verifyOtpButton,"Check if Verify OTP button is Enabled");
	}

	public boolean isSuccessScreenDisplayed() {
		return isElementVisible(successMessagePage, "Check if Success Screen is Displayed");
	}

	public boolean isSuccessHeaderDisplayed() {
		return isElementVisible(successHeader, "Check if Success Header is Displayed");
	}

	public boolean iSuccessMessageDisplayed() {
		return isElementVisible(successMessage,"Check if Success Message is Displayed");
	}

	public boolean isContinueButtonDisplayed() {
		return isElementVisible(continueButtonInSuccessPage, "Check if Continue Button is Displayed");
	}

	public boolean isFailureHeaderDisplayed() {
		return isElementVisible(failureHeader,"Check if Failure Header is Displayed" );
	}

	public boolean isFailureMessageDisplayed() {
		return isElementVisible(failureMessage,"Check if Failure Message is Displayed");
	}

	public boolean isLoginButtonVisible() {
		return isElementVisible(loginButtonInSignUpFailedScreen,"Check if Login Button is Visible");
	}

	public void clickOnLoginButtonInSignUpFailedScreen() {
		clickOnElement(loginButtonInSignUpFailedScreen, "Clikc on Login Button In SignUp Failed Screen");
	}

	public void clickOnContinueButtonInSucessScreen() {
		clickOnElement(continueButtonInSuccessPage, "Click on Continue Button In Sucess Screen");
	}

	public void clickOnErrorCloseIcon() {
		clickOnElement(errorCloseIcon,"Click on Error Close Icon");
	}

	public boolean isSetupAccountHeaderVisible() {
		return isElementVisible(setupAccountHeader, "Check if Setup Account Header is Visible");
	}

	public boolean isSetupAccountDescriptionVisible() {
		return isElementVisible(setupAccountDescription, "Check if Setup Account Description is Visible");
	}

	public boolean isUsernameFieldVisible() {
		return isElementVisible(usernameField,"Check if Username Field is Visible" );
	}

	public boolean isFullNameInKhmerFieldVisible() {
		return isElementVisible(fullNameKhmerField, "Check if FullName In Khmer Field Visible");
	}

	public boolean isPasswordFieldVisible() {
		return isElementVisible(passwordField,"Check if Password Field is Visible");
	}

	public boolean isConfirmPasswordFieldVisible() {
		return isElementVisible(confirmPasswordField, "Check if Confirm Password Field is Visible");
	}

	public boolean isPasswordToggleIconVisible() {
		return isElementVisible(passwordToggleIcon,"Check if Password Toggle Icon is Visible");
	}

	public boolean isPasswordPolicyIconVisible() {
		return isElementVisible(passwordInfoIcon,"Check if Password Policy Icon is Visible");
	}

	public boolean isTermsCheckboxVisible() {
		return isElementVisible(termsAndConditionsCheckbox,"Check if Terms Checkbox is Visible");
	}

	public boolean isSetupContinueButtonVisible() {
		return isElementVisible(setupContinueButton,"Check if Setup Continue Button is Visible");
	}

	public String getUsernameFieldValue() {
		return getElementValue(usernameField,"Get UserName Field value");
	}

	public boolean isUsernameFieldReadOnly() {
		return getElementAttribute(usernameField, "readonly") != null;
	}

	public void clickOnLanguageSelectionOption() {
		clickOnElement(languageSelection,"Click on Lang Selection Option");
	}

	public void clickOnKhmerLanguage() {
		clickOnElement(khmerLanguageSelection,"Click on Khmer Language");
	}

	public void enterFullNameInEnglish(String name) {
		clearField(fullNameEnglishField);
		enterTextJS(fullNameEnglishField, name);
	}

	public void enterFullNameInKhmer(String name) {
		clearField(fullNameKhmerField);
		enterTextJS(fullNameKhmerField, name);
	}

	public String getEnglishFullNamePlaceholder() {
		return fullNameEnglishField.getAttribute("placeholder");
	}

	public String getKhmerFullNamePlaceholder() {
		return fullNameKhmerField.getAttribute("placeholder");
	}

	public void clickOnOutsideNameField() {
		clickOnElement(setupAccountHeader,"Click on Out side Name field");
	}

	public boolean isFullNameHasToBeInKhmerErrorDisplayed() {
		return isElementVisible(fullNameHasToBeInKhmerOnlyError,"Cjeck if Full name has to be Khmenr Error Displayed");
	}

	public boolean isFullNameInKhmerRestrictedToThirtyChars() {
		String value = getElementValue(fullNameKhmerField,"Get FullName Value in Khmert");
		return value != null && value.length() <= 30;
	}

	public void enterOnlySpacesFullName(int length) {
		String spaces = " ".repeat(length);
		enterFullNameInKhmer(spaces);
	}

	public void clickOnEnglishLanguage() {
		clickOnElement(englishLanguageSelection,"Click On English Language");
	}

	public boolean isPleaseEnterValidUsernameErrorDisplayed() {
		return isElementVisible(pleaseEnterValidNameError,"Check if Please Enter Valid Username Error is Displayed");
	}

	public boolean isLanguageChanged() {
		return isElementVisible(setupAccountHeader,"Check if Lang change updated");
	}

	public String getPasswordFieldPlaceholder() {
		return getElementAttribute(passwordField, "placeholder");
	}

	public void enterPassword(String password) {
		clearField(passwordField);
		enterText(passwordField, password,"Enter password");
	}

	public boolean isPasswordDoesNotMeetThePolicyErrorDisplayed() {
		return isElementVisible(passwordFieldError,"Check if Password Does Not Meet The Policy Error is Displayed");
	}

	public void tabsOutOfField() {
		passwordField.sendKeys(Keys.TAB);
	}

	public void enterConfirmPassword(String confirmPassword) {
		clearField(confirmPasswordField);
		enterText(confirmPasswordField, confirmPassword, "Enterd confirm password");
	}

	public void enterShortPassword() {
		String pwd = EsignetUtil.generateValidPasswordFromActuator().substring(0,
				EsignetUtil.getPasswordMinLength() - 1);
		enterPassword(pwd);
	}

	public void enterShortPwd() {
		String pwd = EsignetUtil.generateValidPasswordFromActuator().substring(0,
				EsignetUtil.getPasswordMinLength() - 1);
		enterConfirmPassword(pwd);
	}

	public boolean isPasswordRestrictedToMaxChars() {
		String value = getElementValue(passwordField,"Get value for Password resticted message");
		return value != null && value.length() <= 20;
	}

	public String getConfirmPasswordFieldPlaceholder() {
		return getElementAttribute(confirmPasswordField, "placeholder");
	}

	public boolean isPasswordAndConfirmPasswordDoesNotMatchErrorDisplayed() {
		return isElementVisible(confirmPasswordFieldError,"Check if Password And Confirm Password Does Not Match Error Displayed");
	}

	public boolean isConfirmPasswordRestrictedToMaxChars() {
		String value = getElementValue(confirmPasswordField,"Get element value for confirm pasword");
		return value != null && value.length() <= 20;
	}

	public boolean isPasswordFieldMasked() {
		return "password".equalsIgnoreCase(getElementAttribute(passwordField, "type"));
	}

	public boolean isConfirmPasswordFieldMasked() {
		return "password".equalsIgnoreCase(getElementAttribute(confirmPasswordField, "type"));
	}

	public void clickOnPasswordUnmaskIcon() {
		clickOnElement(passwordToggleIcon,"Click on Password Unmask Icon");
	}

	public boolean isPasswordFieldUnmasked() {
		return "text".equalsIgnoreCase(getElementAttribute(passwordField, "type"));
	}

	public void clickOnConfirmPasswordUnmaskIcon() {
		clickOnElement(confirmPasswordToggleIcon,"Click on Confirm Password Unmask Icon");
	}

	public boolean isConfirmPasswordFieldUnmasked() {
		return "text".equalsIgnoreCase(getElementAttribute(confirmPasswordField, "type"));
	}

	public void clickOnPasswordInfoIcon() {
		clickOnElement(passwordInfoIcon,"Click on Password Info Icon");
	}

	public String getPasswordTooltipText() {
		return passwordFieldTooltipText.getText();
	}

	public void clickOnFullNameInKhmerInfoIcon() {
		clickOnElement(fullNameInKhmerInfoIcon,"Click on Full Name In Khmer Info Icon ");
	}

	public boolean isFullNameInKhmerTooltipMessage() {
		return isElementVisible(fullNameInKhmerTooltipText, "Check if FullName In Khmer Tooltip Message Displayed");
	}

	public String getFullNameTooltipText() {
		return fullNameInKhmerTooltipText.getText();
	}

	public void ensureTermsCheckboxIsUnchecked() {
		if (termsAndConditionsCheckbox.isSelected()) {
			termsAndConditionsCheckbox.click();
		}
	}

	public boolean isContinueButtonInSetupAccountPageEnabled() {
		return isButtonEnabled(setupContinueButton, "Check if Continue Button In Setup Account Page Enabled");
	}

	public boolean isTermsAndConditionsMessageDisplayed() {
		return isElementVisible(messageToAcceptTermsAndCondition, "Check if Terms And Conditions Message Displayed");
	}

	public void clickOnTermsAndConditionLink() {
		clickOnElement(termsAndConditionsLink,"Click on Terms And Conditions Link");
	}

	public boolean isTermsAndConditionsPopupDisplayed() {
		return isElementVisible(termsAndConditionsPopUp,"Check if Terms And Conditions Popup Displayed" );
	}

	public void clickOnClosePopupIcon() {
		clickOnElement(popupWindowCloseIcon, "Click on Close Popup Icon");
	}

	public boolean isSetupAccountPageVisible() {
		return isElementVisible(setupAccountHeader,"Check if Setup Account Page Visible");
	}

	public void clickOnPrivacyPolicyLink() {
		clickOnElement(privacyPolicyLink,"Click on Privacy Policy Link");
	}

	public boolean isPrivacyPolicyPopupDisplayed() {
		return isElementVisible(privacyPolicyPopUp, "Check if Privacy Policy Popup Displayed");
	}

	public void clearAllMandatoryFields() {
		fullNameEnglishField.clear();
		fullNameKhmerField.clear();
		passwordField.clear();
		confirmPasswordField.clear();
	}

	public void checkTermsAndConditions() {
		if (!termsAndConditionsCheckbox.isSelected()) {
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
					termsAndConditionsCheckbox);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", termsAndConditionsCheckbox);
		}
	}

	public WebElement getTermsAndConditionsCheckbox() {
		return termsAndConditionsCheckbox;
	}

	public void clickOnSetupAccountContinueButton() {
		clickOnElement(setupContinueButton, "Click on Setup Account continue button");
	}

	public boolean isScreenDisplayedInEnglishLang() {
		return isElementVisible(screenInEnglishLanguage,"Check if Screen Displayed in English");
	}

	public String getOtpResendAttemptsText(int expectedRemainingAttempts) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		wait.until(ExpectedConditions.textToBePresentInElement(remainingAttemptsMeassage,
				String.valueOf(expectedRemainingAttempts)));
		return getText(remainingAttemptsMeassage,"Get text from Remaing attempts");
	}

	public boolean isAccountCreatedSuccessfullyMessageDisplayed() {
		return isElementVisible(accountCreatedSuccessfullyMessage,"Check if Account Created Successfully Message Displayed");
	}

	public boolean isLoginButtonDisplayed() {
		return isElementVisible(loginButtonInSuccessScreen,"Check if Login Button Displayed");
	}

	public void clickOnLoginButtonInSuccessScreen() {
		clickOnElement(loginButtonInSuccessScreen,"Click on Login Button in Success Screen");
	}

	public boolean isLoginScreenDisplayed() {
		return isElementVisible(loginScreen, "Check if Login Screen is Displayed");
	}

	public boolean isOkayButtonDisplayed() {
		return isElementVisible(loginButtonInSuccessScreen,"Check if Okay Button Displayed");
	}

	public void clickOnOkayButtonInSuccessScreen() {
		clickOnElement(loginButtonInSuccessScreen,"Click on Okay button on Success screen");
	}

	public boolean isAccountSetupInProgressDisplayed() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(500));
		try {
			return wait.until(ExpectedConditions.visibilityOf(accountSetupInProgressMessage)).isDisplayed();
		} catch (TimeoutException e) {
			return false;
		}
	}

}
