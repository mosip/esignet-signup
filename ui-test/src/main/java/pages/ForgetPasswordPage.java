package pages;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BasePage;
import utils.EsignetUtil;

public class ForgetPasswordPage extends BasePage {

	public ForgetPasswordPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "sign-in-with-esignet")
	WebElement signInWithEsignet;

	@FindBy(id = "phone_input")
	WebElement enterMobileNumberField;

	@FindBy(xpath = "//img[@class='brand-logo']")
	WebElement brandLogo;

	@FindBy(xpath = "//span[@class='flex self-center border-r-[1px] border-input px-3 text-muted-foreground/60']")
	WebElement phonePrefix;

	@FindBy(id = "phone_input")
	WebElement phoneInput;

	@FindBy(xpath = "//span[@class='flex self-center border-r-[1px] border-input px-3 text-muted-foreground/60']")
	WebElement countryCodeSpan;

	@FindBy(id = ":r4:-form-item-message")
	List<WebElement> phoneErrorForInvalidValue;

	@FindBy(xpath = "//div[@class='text-center text-[26px] font-semibold tracking-normal']")
	WebElement forgetPasswordHeading;

	@FindBy(xpath = "//div[@class='text-center text-gray-500']")
	WebElement forgetPasswordSubHeadning;

	@FindBy(xpath = "//label[@class='text-sm font-semibold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70']")
	WebElement userNameLabel;

	@FindBy(xpath = "//label[@class='text-sm font-semibold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70']")
	WebElement fullNameLabel;

	@FindBy(id = "continue-button")
	WebElement continueButton;

	@FindBy(id = "language-select-button")
	WebElement langSelectionButton;

	@FindBy(xpath = "//footer/span")
	WebElement poweredByText;

	@FindBy(xpath = "//footer//img[@class='footer-brand-logo' ]")
	WebElement footerLogo;

	@FindBy(id = "fullname")
	WebElement fullNameInput;

	@FindBy(xpath = "//p[@id=':r5:-form-item-message']")
	WebElement fullNameError;

	@FindBy(xpath = "//div[@class='flex gap-x-1 text-center']")
	WebElement resendOtpCountdownText;

	@FindBy(id = "resend-otp-button")
	WebElement resendOtpButton;

	@FindBy(xpath = "//div[@class='pincode-input-container']/input")
	List<WebElement> otpInputFields;

	@FindBy(xpath = "//p[@class='truncate text-xs text-destructive']")
	WebElement incorrectOtpError;

	@FindBy(xpath = "//p[@class='truncate text-xs text-destructive']")
	WebElement otpExpiredError;

	@FindBy(xpath = "//div[@class='pincode-input-container']")
	WebElement otpInputField;

	@FindBy(xpath = "//div[@class='w-max rounded-md bg-[#FFF7E5] p-2 px-8 text-center text-sm font-semibold text-[#8B6105]']")
	private WebElement resendAttemptsText;

	@FindBy(xpath = "//div[@class='flex gap-x-1 text-center']")
	private WebElement otpCountdownTimer;

	@FindBy(xpath = "//div[@class='text-center font-semibold tracking-normal']")
	private WebElement resetPasswordHeader;

	@FindBy(xpath = "//div[@class='text-center text-gray-500']")
	private WebElement passwordInstructionMessage;

	@FindBy(xpath = "//label[@class='text-sm font-semibold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70']")
	private WebElement newPasswordLabel;

	@FindBy(xpath = "//label[@class='text-sm font-semibold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70']")
	private WebElement confirmnNewPasswordLabel;

	@FindBy(id = "newPassword-info-icon")
	private WebElement newPasswordInfoIcon;

	@FindBy(xpath = "//div[contains(@id, 'radix-:') and @role='dialog']")
	WebElement passwordPolicyTooltip;

	@FindBy(id = "newPassword")
	private WebElement newPasswordInput;

	@FindBy(id = "confirmNewPassword")
	private WebElement confirmNewPasswordInput;

	@FindBy(id = "alert-action-button")
	private WebElement retryButton;

	@FindBy(xpath = "//h2[@class='text-lg font-semibold flex flex-col items-center justify-center gap-y-4']")
	private WebElement invalidErrorHeader;

	@FindBy(xpath = "//p[@class='text-sm text-center text-muted-dark-gray']")
	private WebElement errorMessage;

	@FindBy(xpath = "//p[contains(@id, '-form-item-message')]")
	private WebElement passwordErrorMessage;

	@FindBy(id = "newPassword-toggle-password")
	private WebElement newPasswordToggleIcon;

	@FindBy(xpath = "//p[contains(@id, '-form-item-message')]")
	private WebElement confirmPasswordErrorMessage;

	@FindBy(id = "confirmNewPassword-toggle-password")
	private WebElement confirmPasswordToggleIcon;

	@FindBy(id = "reset-password-button")
	private WebElement resetButton;

	@FindBy(xpath = "//h1[@class='text-center text-2xl font-semibold']")
	WebElement passwordResetInProgress;

	@FindBy(xpath = "//p[@class='text-center text-muted-neutral-gray']")
	WebElement pleaseWaitMessageInResetPassword;

	@FindBy(xpath = "//div[@class='text-center text-lg font-semibold']")
	private WebElement passwordResetConfirmationHeader;

	@FindBy(xpath = "//p[@class='text-center text-muted-neutral-gray']")
	private WebElement passwordResetConfirmationMessage;

	@FindBy(id = "success-continue-button")
	private WebElement loginButtonInSuccessScreen;

	@FindBy(id = "reset-password-button")
	WebElement resetPasswordButton;

	public void enterMobileNumber(String number) {
		enterMobileNumberField.clear();
		enterText(enterMobileNumberField, number, "Enterd Mobile Number");
	}

	public void clickOnSignInWIthEsignet() {
		clickOnElement(signInWithEsignet, "clicked on SignInWithEsignet");
	}

	public boolean isLogoDisplayed() {
		return isElementVisible(brandLogo, "check logo is displayed");
	}

	public void clickOnLanguageSelectionDropdown() {
		clickOnElement(langSelectionButton, "click on language dropdown");
	}

	public boolean isRedirectedToResetPasswordPage() {
		return isElementVisible(userNameLabel, "check if redirected to Reset password page");
	}

	public boolean isphonePrefixDisplayed() {
		return isElementVisible(phonePrefix, "check prefix of phone number is displayed");
	}

	public boolean isWaterMarkDisplayed() {
		String placeholder = getElementAttribute(phoneInput, "placeholder");
		return placeholder != null && !placeholder.isEmpty();
	}

	public boolean isCountryCodeNonEditable() {
		return isElementVisible(countryCodeSpan, "check country code is displayed")
				&& !getElementTagName(countryCodeSpan).equalsIgnoreCase("input");
	}

	public boolean isPhoneErrorVisible() {
		return !phoneErrorForInvalidValue.isEmpty() && phoneErrorForInvalidValue.get(0).isDisplayed();
	}

	public void enterPhoneNumber(String number) {
		phoneInput.clear();
		enterText(phoneInput, number, "Entered phone number");
	}

	public void triggerPhoneValidation() {
		clickOnElement(countryCodeSpan, "click outside the phone number field");
	}

	public boolean isForgetPasswordHeadingVisible() {
		return isElementVisible(forgetPasswordHeading, "check forgot password heading displayed ");
	}

	public boolean isForgetPasswordSubHeadingVisible() {
		return isElementVisible(forgetPasswordSubHeadning, "check forgot password sub heading is displayed");
	}

	public boolean isUserNameLabelVisible() {
		return isElementVisible(userNameLabel, "check username label");
	}

	public boolean isFullNameLabelVisible() {
		return isElementVisible(fullNameLabel, "check fullname label");
	}

	public boolean isContinueButtonVisible() {
		return isElementVisible(continueButton, "check continue button displayed");
	}

	public boolean isLangSelectionButtonVisible() {
		return isElementVisible(langSelectionButton, "check language button displayed");
	}

	public boolean isFooterPoweredByVisible() {
		return isElementVisible(poweredByText, "check footer text displayed")
				&& isElementVisible(footerLogo, "check footer logo displayed");
	}

	public boolean isMobileFieldHasNumericOnly() {
		WebElement field = phoneInput;
		{
			String value = getElementValue(field, "get the value of the field");
			if (value != null && !value.matches("\\d*")) {
				return false;
			}
		}
		return true;
	}

	public boolean isPhoneNumberFieldEmpty() {
		String value = getElementValue(phoneInput, "Get Mobile Field Empty Or Unchanged value");
		return value == null || value.isEmpty();
	}

	public boolean isContinueButtonDisabled() {
		return !isButtonEnabled(continueButton, "check continue button disabled");
	}

	public void enterFullName(String name) {
		enterText(fullNameInput, name, "Entered fullname");
	}

	public boolean isFullNameErrorVisible() {
		try {
			return isElementVisible(fullNameError, "check error displayed");
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public boolean isFullNameErrorPresent() {
		return driver.findElements(By.xpath("//p[@id=':r5:-form-item-message']")).size() > 0;
	}

	public boolean isFullnameRestrictedToMaxChars() {
		String value = getElementValue(fullNameInput, "get the entered value");
		return value != null && value.length() <= 30;
	}

	public boolean isContinueButtonEnabled() {
		return isButtonEnabled(continueButton, "check continue button enabled");
	}

	public void clickOnContinueButton() {
		clickOnElement(continueButton, "click on continue button");
	}

	public boolean isResendOtpCountdownVisible() {
		return isElementVisible(resendOtpCountdownText, "check resend otp countdown displayed");
	}

	public boolean isResendOtpButtonVisible() {
		return isElementVisible(resendOtpButton, "check resend otp button displayed");
	}

	public void waitUntilOtpExpire() {
		int otpExpiry = EsignetUtil.getOtpResendDelayFromSignupActuator();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(otpExpiry));
		wait.until(ExpectedConditions.textToBePresentInElement(otpCountdownTimer, "00:00"));
	}

	public boolean isInputRestrictedToNineDigits() {
		String value = getElementValue(phoneInput, "get the entered value");
		return value != null && value.length() == 9;
	}

	public void enterOtp(String otp) {
		for (int i = 0; i < otp.length(); i++) {
			WebElement field = otpInputFields.get(i);
			field.click();
			field.sendKeys(String.valueOf(otp.charAt(i)));
		}
	}

	public boolean isResendOtpButtonEnabled() {
		return isButtonEnabled(resendOtpButton, "check resend button enabled");
	}

	public void clickOnResendOtp() {
		clickOnElement(resendOtpButton, "click on resend otp button");
	}

	public String getOtpResendAttemptsText(int expectedRemainingAttempts) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		wait.until(ExpectedConditions.textToBePresentInElement(resendAttemptsText,
				String.valueOf(expectedRemainingAttempts)));
		return getText(resendAttemptsText, "get the remaining atteptes text");
	}

	public void waitForOtpExpire() {
		int otpExpiry = EsignetUtil.getOtpResendDelayFromSignupActuator();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(otpExpiry + 3));
		wait.until(ExpectedConditions.textToBePresentInElement(otpCountdownTimer, "00:00"));
	}

	public boolean isForgetPassowrdScreenVisible() {
		return isElementVisible(forgetPasswordHeading, "check is on forgot password page");
	}

	public boolean isResetPasswordScreenVisible() {
		return isElementVisible(resetPasswordHeader, "check reset password screen displyed");
	}

	public boolean isResetPasswordHeaderVisible() {
		return isElementVisible(resetPasswordHeader, "check reset password screen header displyed");
	}

	public boolean isPasswordInstructionMessageVisible() {
		return isElementVisible(passwordInstructionMessage, "check password instruction displyed");
	}

	public boolean isNewPasswordLabelVisible() {
		return isElementVisible(newPasswordLabel, "check new password label");
	}

	public boolean isConfirmNewPasswordLabelVisible() {
		return isElementVisible(confirmnNewPasswordLabel, "check confirm password label");
	}

	public boolean isNewPasswordInputTextboxVisible() {
		return isElementVisible(newPasswordInput, "check text box for password field");
	}

	public boolean isConfirmNewPasswordInputTextboxVisible() {
		return isElementVisible(confirmNewPasswordInput, "check text box for confirm password field");
	}

	public boolean isNewPasswordInfoIconVisible() {
		return isElementVisible(newPasswordInfoIcon, "check info icon for password field is displayed");
	}

	public void clickOnNewPasswordInfoIcon() {
		clickOnElement(newPasswordInfoIcon, "click on new password info incon");
	}

	public boolean isPasswordPolicyTooltipVisible() {
		return isElementVisible(passwordPolicyTooltip, "check password policy tooltip message displayed");
	}

	public String getNewPasswordFieldPlaceholder() {
		return getElementAttribute(newPasswordInput, "placeholder");
	}

	public String getConfirmNewPasswordFieldPlaceholder() {
		return getElementAttribute(confirmNewPasswordInput, "placeholder");
	}

	public boolean isErrorHeaderInvalidVisible() {
		return isElementVisible(invalidErrorHeader, "check error is displayed");
	}

	public boolean isErrorMessageVisible() {
		return isElementVisible(errorMessage, "check error message is displayed");
	}

	public boolean isRetryButtonVisible() {
		return isElementVisible(retryButton, "check retry button is displayed");
	}

	public void clickOnRetryButton() {
		clickOnElement(retryButton, "click on retry button");
	}

	public void enterNewPassword(String newPassword) {
		clearField(newPasswordInput);
		enterText(newPasswordInput, newPassword, "Entered new pasword");
	}

	public void enterShortNewPassword() {
		String pwd = EsignetUtil.generateValidPasswordFromActuator().substring(0,
				EsignetUtil.getPasswordMinLength() - 1);
		enterNewPassword(pwd);
	}

	public void enterConfirmPassword(String confirmPassword) {
		clearField(confirmNewPasswordInput);
		enterText(confirmNewPasswordInput, confirmPassword, "Entered confirm password");
	}

	public void enterShortConfirmPassword() {
		String pwd = EsignetUtil.generateValidPasswordFromActuator().substring(0,
				EsignetUtil.getPasswordMinLength() - 1);
		enterConfirmPassword(pwd);
	}

	public void clickOutsidePasswordField() {
		clickOnElement(newPasswordToggleIcon, "click outside password field");
	}

	public boolean isPasswordErrorDisplayed() {
		return isElementVisible(passwordErrorMessage, "check password error is displayed");
	}

	public boolean isPasswordRestrictedToMaxChar() {
		String value = getElementValue(newPasswordInput, "get the entered password value");
		return value != null && value.length() <= 20;
	}

	public boolean isconfirmPasswordErrorDisplayed() {
		return isElementVisible(confirmPasswordErrorMessage, "check confirm password error is displayed");
	}

	public boolean isConfirmPasswordRestrictedToMaxChar() {
		String value = getElementValue(confirmNewPasswordInput, "check confirm password is restricted");
		return value != null && value.length() <= 20;
	}

	public boolean isNewPasswordFieldMasked() {
		return "password".equalsIgnoreCase(getElementAttribute(newPasswordInput, "type"));
	}

	public boolean isConfirmPasswordFieldMasked() {
		return "password".equalsIgnoreCase(getElementAttribute(confirmNewPasswordInput, "type"));
	}

	public void clickOnNewPasswordUnmaskIcon() {
		clickOnElement(newPasswordToggleIcon, "click on password unmask icon");
	}

	public boolean isNewPasswordFieldUnmasked() {
		return "text".equalsIgnoreCase(getElementAttribute(newPasswordInput, "type"));
	}

	public void clickOnConfirmPasswordUnmaskIcon() {
		clickOnElement(confirmPasswordToggleIcon, "click on confirm password unmask icon");
	}

	public boolean isConfirmPasswordFieldUnmasked() {
		return "text".equalsIgnoreCase(getElementAttribute(confirmNewPasswordInput, "type"));
	}

	public boolean isResetButtonEnabled() {
		return isButtonEnabled(resetButton, "check resend button enabled");
	}

	public void clickOnResetButton() {
		clickOnElement(resetButton, "click on reset button");
	}

	public boolean isPasswordResetInProgressDisplayed() {
		return passwordResetInProgress.isDisplayed() && pleaseWaitMessageInResetPassword.isDisplayed();
	}

	public boolean isPasswordResetConfirmationHeaderDisplayed() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
		wait.until(ExpectedConditions.visibilityOf(passwordResetConfirmationHeader));
		return isButtonEnabled(passwordResetConfirmationHeader, "check password reset confirmation header displayed");
	}

	public boolean isPasswordResetConfirmationMessageDisplayed() {
		return isElementVisible(passwordResetConfirmationMessage,
				"check password reset confirmation message displayed");
	}

	public boolean isLoginButtonDisplayed() {
		return isButtonEnabled(loginButtonInSuccessScreen, "check login button is displayed");
	}

	public void clickOnLoginButton() {
		clickOnElement(loginButtonInSuccessScreen, "click on login button");
	}

	public void clickOnResetPasswordButton() {
		clickOnElement(resetPasswordButton, "click on reset password button");
	}

}