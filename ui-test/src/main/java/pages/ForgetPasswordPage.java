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

	@FindBy(id = "password")
	WebElement passwordField;

	@FindBy(id = "password_confirm")
	WebElement confirmPasswordField;

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

	@FindBy(id = "fullName_eng")
	WebElement fullNameEnglishField;

	@FindBy(id = "fullName_khm")
	WebElement fullNameKhmerField;

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

	@FindBy(id = "back-button")
	WebElement backButton;

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

	@FindBy(id = "login-header")
	private WebElement loginScreen;

	@FindBy(id = "reset-password-button")
	WebElement resetPasswordButton;

	public void enterMobileNumber(String number) {
		enterMobileNumberField.clear();
		enterText(enterMobileNumberField, number);
	}

	public void enterPassword(String password) {
		clearField(passwordField);
		enterText(passwordField, password);
	}

	public void enterConfirmPwd(String confirmPassword) {
		clearField(confirmPasswordField);
		enterText(confirmPasswordField, confirmPassword);
	}

	public void clickOnSignInWIthEsignet() {
		clickOnElement(signInWithEsignet);
	}

	public boolean isLogoDisplayed() {
		return isElementVisible(brandLogo);
	}

	public void clickOnLanguageSelectionDropdown() {
		clickOnElement(langSelectionButton);
	}

	public boolean isRedirectedToResetPasswordPage() {
		return isElementVisible(userNameLabel);
	}

	public boolean isphonePrefixDisplayed() {
		return isElementVisible(phonePrefix);
	}

	public boolean isWaterMarkDisplayed() {
		String placeholder = getElementAttribute(phoneInput, "placeholder");
		return placeholder != null && !placeholder.isEmpty();
	}

	public boolean isCountryCodeNonEditable() {
		return isElementVisible(countryCodeSpan) && !getElementTagName(countryCodeSpan).equalsIgnoreCase("input");
	}

	public boolean isPhoneErrorVisible() {
		return !phoneErrorForInvalidValue.isEmpty() && phoneErrorForInvalidValue.get(0).isDisplayed();
	}

	public void enterPhoneNumber(String number) {
		phoneInput.clear();
		enterText(phoneInput, number);
	}

	public void triggerPhoneValidation() {
		clickOnElement(countryCodeSpan);
	}

	public boolean isForgetPasswordHeadningVisible() {
		return isElementVisible(forgetPasswordHeading);
	}

	public boolean isForgetPasswordSubHeadningVisible() {
		return isElementVisible(forgetPasswordSubHeadning);
	}

	public boolean isUserNameLabelVisible() {
		return isElementVisible(userNameLabel);
	}

	public boolean isFullNameLabelVisible() {
		return isElementVisible(fullNameLabel);
	}

	public boolean isContinueButtonVisible() {
		return isElementVisible(continueButton);
	}

	public boolean isLangSelectionButtonVisible() {
		return isElementVisible(langSelectionButton);
	}

	public boolean isFooterPoweredByVisible() {
		return isElementVisible(poweredByText) && isElementVisible(footerLogo);
	}

	public boolean isMobileFieldHasNumericOnly() {
		WebElement field = phoneInput;
		{
			String value = getElementValue(field);
			if (value != null && !value.matches("\\d*")) {
				return false;
			}
		}
		return true;
	}

	public String getEnteredPhoneNumber() {
		return getElementAttribute(phoneInput, "value");
	}

	public boolean isContinueButtonDisabled() {
		return !isButtonEnabled(continueButton);
	}

	public void enterFullName(String name) {
		enterText(fullNameInput, name);
	}

	public void enterFullNameInEnglish(String name) {
		clearField(fullNameEnglishField);
		enterTextJS(fullNameEnglishField, name);
	}

	public void enterFullNameInKhmer(String name) {
		clearField(fullNameKhmerField);
		enterTextJS(fullNameKhmerField, name);
	}

	public boolean isFullNameErrorVisible() {
		try {
			return isElementVisible(fullNameError);
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public boolean isFullNameErrorPresent() {
		return driver.findElements(By.xpath("//p[@id=':r5:-form-item-message']")).size() > 0;
	}

	public boolean isFullnameRestrictedToMaxChars() {
		String value = getElementValue(fullNameInput);
		return value != null && value.length() <= 30;
	}

	public boolean isContinueButtonEnabled() {
		return isButtonEnabled(continueButton);
	}

	public void clickOnContinueButton() {
		clickOnElement(continueButton);
	}

	public boolean isResendOtpCountdownVisible() {
		return isElementVisible(resendOtpCountdownText);
	}

	public boolean isResendOtpButtonVisible() {
		return isElementVisible(resendOtpButton);
	}

	public void waitUntilOtpExpire() {
		int otpExpiry = EsignetUtil.getOtpResendDelayFromSignupActuator();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(otpExpiry));
		wait.until(ExpectedConditions.textToBePresentInElement(otpCountdownTimer, "00:00"));
	}

	public boolean isInputRestrictedToNineDigits() {
		String value = getElementValue(phoneInput);
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
		return isButtonEnabled(resendOtpButton);
	}

	public void clickOnResendOtp() {
		clickOnElement(resendOtpButton);
	}

	public String getOtpResendAttemptsText(int expectedRemainingAttempts) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		wait.until(ExpectedConditions.textToBePresentInElement(resendAttemptsText,
				String.valueOf(expectedRemainingAttempts)));
		return getText(resendAttemptsText);
	}

	public void clickOnNavigateBackButton() {
		clickOnElement(backButton);
	}

	public boolean isForgetPassowrdScreenVisible() {
		return isElementVisible(forgetPasswordHeading);
	}

	public boolean isResetPasswordScreenVisible() {
		return isElementVisible(resetPasswordHeader);
	}

	public boolean isResetPasswordHeaderVisible() {
		return isElementVisible(resetPasswordHeader);
	}

	public boolean isPasswordInstructionMessageVisible() {
		return isElementVisible(passwordInstructionMessage);
	}

	public boolean isNewPasswordLabelVisible() {
		return isElementVisible(newPasswordLabel);
	}

	public boolean isConfirmNewPasswordLabelVisible() {
		return isElementVisible(confirmnNewPasswordLabel);
	}

	public boolean isNewPasswordInputTextboxVisible() {
		return isElementVisible(newPasswordInput);
	}

	public boolean isConfirmNewPasswordInputTextboxVisible() {
		return isElementVisible(confirmNewPasswordInput);
	}

	public boolean isNewPasswordInfoIconVisible() {
		return isElementVisible(newPasswordInfoIcon);
	}

	public void clickOnNewPasswordInfoIcon() {
		clickOnElement(newPasswordInfoIcon);
	}

	public boolean isPasswordPolicyTooltipVisible() {
		return isElementVisible(passwordPolicyTooltip);
	}

	public String getNewPasswordFieldPlaceholder() {
		return getElementAttribute(newPasswordInput, "placeholder");
	}

	public String getConfirmNewPasswordFieldPlaceholder() {
		return getElementAttribute(confirmNewPasswordInput, "placeholder");
	}

	public boolean isErrorHeaderInvalidVisible() {
		return isElementVisible(invalidErrorHeader);
	}

	public boolean isErrorMessageVisible() {
		return isElementVisible(errorMessage);
	}

	public boolean isRetryButtonVisible() {
		return isElementVisible(retryButton);
	}

	public void clickOnRetryButton() {
		clickOnElement(retryButton);
	}

	public void enterNewPassword(String newPassword) {
		clearField(newPasswordInput);
		enterText(newPasswordInput, newPassword);
	}

	public void enterShortNewPassword() {
		String pwd = EsignetUtil.generateValidPasswordFromActuator().substring(0,
				EsignetUtil.getPasswordMinLength() - 1);
		enterNewPassword(pwd);
	}

	public void enterConfirmPassword(String confirmPassword) {
		clearField(confirmNewPasswordInput);
		enterText(confirmNewPasswordInput, confirmPassword);
	}

	public void enterShortConfirmPassword() {
		String pwd = EsignetUtil.generateValidPasswordFromActuator().substring(0,
				EsignetUtil.getPasswordMinLength() - 1);
		enterConfirmPassword(pwd);
	}

	public void clickOutsidePasswordField() {
		clickOnElement(newPasswordToggleIcon);
	}

	public boolean isPasswordErrorDisplayed() {
		return isElementVisible(passwordErrorMessage);
	}

	public boolean isPasswordRestrictedToMaxChar() {
		String value = getElementValue(newPasswordInput);
		return value != null && value.length() <= 20;
	}

	public boolean isconfirmPasswordErrorDisplayed() {
		return isElementVisible(confirmPasswordErrorMessage);
	}

	public boolean isConfirmPasswordRestrictedToMaxChar() {
		String value = getElementValue(confirmNewPasswordInput);
		return value != null && value.length() <= 20;
	}

	public boolean isNewPasswordFieldMasked() {
		return "password".equalsIgnoreCase(getElementAttribute(newPasswordInput, "type"));
	}

	public boolean isConfirmPasswordFieldMasked() {
		return "password".equalsIgnoreCase(getElementAttribute(confirmNewPasswordInput, "type"));
	}

	public void clickOnNewPasswordUnmaskIcon() {
		clickOnElement(newPasswordToggleIcon);
	}

	public boolean isNewPasswordFieldUnmasked() {
		return "text".equalsIgnoreCase(getElementAttribute(newPasswordInput, "type"));
	}

	public void clickOnConfirmPasswordUnmaskIcon() {
		clickOnElement(confirmPasswordToggleIcon);
	}

	public boolean isConfirmPasswordFieldUnmasked() {
		return "text".equalsIgnoreCase(getElementAttribute(confirmNewPasswordInput, "type"));
	}

	public boolean isResetButtonEnabled() {
		return isButtonEnabled(resetButton);
	}

	public void clickOnResetButton() {
		clickOnElement(resetButton);
	}

	public boolean isPasswordResetInProgressDisplayed() {
		return passwordResetInProgress.isDisplayed() && pleaseWaitMessageInResetPassword.isDisplayed();
	}

	public boolean isPasswordResetConfirmationHeaderDisplayed() {
		return isButtonEnabled(passwordResetConfirmationHeader);
	}

	public boolean isPasswordResetConfirmationMessageDisplayed() {
		return isElementVisible(passwordResetConfirmationMessage);
	}

	public boolean isLoginButtonDisplayed() {
		return isButtonEnabled(loginButtonInSuccessScreen);
	}

	public void clickOnLoginButton() {
		clickOnElement(loginButtonInSuccessScreen);
	}

	public boolean isLoginScreenDisplayed() {
		return isButtonEnabled(loginScreen);
	}

	public void clickOnResetPasswordButton() {
		clickOnElement(resetPasswordButton);
	}

}