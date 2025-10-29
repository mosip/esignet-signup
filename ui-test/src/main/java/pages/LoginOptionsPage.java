package pages;

import base.BasePage;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginOptionsPage extends BasePage {

	public LoginOptionsPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "sign-in-with-esignet")
	WebElement signInWithEsignet;

	@FindBy(xpath = "//img[@class='brand-logo']")
	WebElement brandLogo;

	@FindBy(id = "language_selection")
	WebElement languageSelection;

	@FindBy(id = "signup-url-button")
	WebElement signUpWithUnifiedLogin;

	@FindBy(id = "phone_input")
	WebElement mobileNumberInput;

	@FindBy(id = "login_with_pwd")
	WebElement loginWithPasswordButton;

	@FindBy(id = "Password_IND")
	WebElement registeredMobileNumberField;

	@FindBy(id = "Password_mobile")
	WebElement passwordField;

	@FindBy(id = "verify_password")
	WebElement loginButton;

	@FindBy(id = "error-banner-message")
	WebElement invalidNumberError;

	@FindBy(id = "password-eye")
	WebElement passwordEyeIcon;

	@FindBy(xpath = "//span[contains(text(),'Please Enter Valid Password')]")
	WebElement invalidPasswordError;

	@FindBy(id = "error-banner-message")
	WebElement invalidUsernameOrPasswordError;

	@FindBy(id = "error-close-button")
	WebElement errorCloseIcon;

	@FindBy(id = "navbar-header")
	WebElement proceedToAttentionScreen;

	@FindBy(id = "proceed-button")
	WebElement proceedButtonInAttentionPage;

	@FindBy(xpath = "//button[contains(@class, 'bg-primary text-primary-foreground hover:bg-primary')]")
	WebElement proceedButton;

	@FindBy(id = "mock-identity-verifier")
	WebElement eKycServiceProvider;

	@FindBy(id = "proceed-preview-button")
	WebElement proceedBtnInServiceProviderPage;

	@FindBy(id = "consent-button")
	WebElement termsAndConditionCheckBox;

	@FindBy(id = "proceed-tnc-button")
	WebElement proceedBtnInTandCPage;

	@FindBy(id = "proceed-preview-button")
	WebElement proceedBtnInCameraPreviewPage;

	@FindBy(xpath = "//p[@class='text-[#4E4E4E] font-semibold']")
	WebElement consentScreen;

	@FindBy(xpath = "//div[@class='text-center']")
	WebElement headerOfConsentPage;

	@FindBy(xpath = "//img[@class='object-contain client-logo-size']")
	WebElement imageOfHealthCareDesign;

	@FindBy(xpath = "//img[@class='object-contain brand-only-logo client-logo-size']")
	WebElement imageOfEsignetLogo;

	@FindBy(id = "essential_claims_tooltip")
	WebElement essentialClaimiIcon;

	@FindBy(xpath = "//div[@class='divide-y']")
	WebElement essentialClaimsList;

	@FindBy(id = "voluntary_claims_tooltip")
	WebElement voluntaryClaimiIcon;

	@FindBy(xpath = "//div[@class='divide-y']")
	WebElement lookForVoluntarClaims;

	@FindBy(id = "continue")
	WebElement allowButtonInConsentScreen;

	@FindBy(id = "cancel")
	WebElement cancelButtonInConsentScreen;

	@FindBy(xpath = "//div[@class='react-tooltip styles-module_tooltip__mnnfp styles-module_dark__xNqje md:w-3/6 lg:max-w-sm m-0 md:m-auto styles-module_show__2NboJ']")
	WebElement essentialClaimToolTipMessege;

	@FindBy(xpath = "//div[@class='relative text-center text-dark font-semibold text-xl text-[#2B3840] mt-4']")
	WebElement cancelWarningHeader;

	@FindBy(xpath = "//p[@class='text-base text-[#707070]']")
	WebElement cancelWarningMessage;

	@FindBy(xpath = "//button[@class='flex justify-center w-full font-medium rounded-lg text-sm px-5 py-4 text-center border-2 secondary-button']")
	WebElement discontinueButton;

	@FindBy(xpath = "//button[@class='flex justify-center w-full font-medium rounded-lg text-sm px-5 py-4 text-center border-2 primary-button']")
	WebElement stayButton;

	@FindBy(xpath = "//label[@for='voluntary_claims']")
	WebElement voluntaryClaimMasterToggle;

	@FindBy(xpath = "//span[@class='self-center text-2xl font-semibold whitespace-nowrap']")
	WebElement welcomePageOfRelyingParty;

	@FindBy(id = "voluntary_claims")
	WebElement masterToggle;

	public void clickOnSignInWIthEsignet() {
		clickOnElement(signInWithEsignet, "Click on Sign with esignet option ");
	}

	public boolean isLogoDisplayed() {
		return isElementVisible(brandLogo, "Check if the logo is displayed");
	}

	public boolean isSignUpWithUnifiedLoginOptionDisplayed() {
		return isElementVisible(signUpWithUnifiedLogin, "Check if Unified Login Option is displayed");
	}

	public String getCurrentLanguage() {
		return languageSelection.getText().trim();
	}

	public void clickOnLanguageSelection() {
		clickOnElement(languageSelection, "click on language selection option");
	}

	public void clickOnSignUpWithUnifiedLogin() {
		clickOnElement(signUpWithUnifiedLogin, "Click on SignUp With Unified Login Option");
	}

	public void enterMobileNumber(String number) {
		enterText(mobileNumberInput, number, "Entered mobile number");
	}

	public void enterRegisteredMobileNumber(String number) {
		registeredMobileNumberField.clear();
		enterText(registeredMobileNumberField, number, "Entered registered mobile number");
	}

	public void enterRegisteredPassword(String Password) {
		enterText(passwordField, Password, "entered registered password");
	}

	public void clickOnLoginButton() {
		loginButton.click();
	}

	public void clickOnLoginWithPasswordOption() {
		loginWithPasswordButton.click();
	}

	public boolean isInvalidNumberErrorDisplayed() {
		return invalidNumberError.isDisplayed();
	}

	public void userTaboutOfPasswordField() {
		clickOnElement(passwordEyeIcon, "click outside the passwood field");
	}

	public boolean isInvalidPasswordErrorDisplayed() {
		return invalidPasswordError.isDisplayed();
	}

	public boolean isInvalidUsernameOrPasswordErrorDisplayed() {
		return invalidUsernameOrPasswordError.isDisplayed();
	}

	public boolean isLoginButtonEnabled() {
		return isButtonEnabled(loginButton, "check login button is enabled");
	}

	public void clickOnErrorCloseIcon() {
		clickOnElement(errorCloseIcon, "click on error close icon");
	}

	public void verifyErrorDisappearsAfterClose() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
		wait.until(ExpectedConditions.invisibilityOf(invalidUsernameOrPasswordError));
	}

	public void verifyErrorMessageDisappearsAfterTenSeconds() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
		wait.until(ExpectedConditions.invisibilityOf(invalidUsernameOrPasswordError));
	}

	public boolean isOnAttentionScreen() {
		return proceedToAttentionScreen.isDisplayed();
	}

	public void clickOnProceedButtonInAttentionPage() {
		clickOnElement(proceedButtonInAttentionPage, "click on proceed button");
	}

	public void clickOnProceedButton() {
		clickOnElement(proceedButton, "click on proceed button");
	}

	public void clickOnMockIdentifyVerifier() {
		clickOnElement(eKycServiceProvider, "select ekyc provider");
	}

	public void clickOnProceedButtonInServiceProviderPage() {
		clickOnElement(proceedBtnInServiceProviderPage, "click on proceed button");
	}

	public void checkTermsAndConditions() {
		if (!termsAndConditionCheckBox.isSelected()) {
			clickOnElement(termsAndConditionCheckBox, "accept the terms and conditions");
		}
	}

	public void clickOnProceedButtonInTermsAndConditionPage() {
		clickOnElement(proceedBtnInTandCPage, "click on proceed button");
	}

	public void clickOnProceedButtonInCameraPreviewPage() {
		clickWhenClickable(proceedBtnInCameraPreviewPage);
	}

	public void waitUntilLivenessCheckCompletes() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(70));
		wait.until(ExpectedConditions.visibilityOf(allowButtonInConsentScreen));
	}

	private void clickWhenClickable(WebElement element) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		WebElement stableElement = wait
				.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(element)));
		stableElement.click();
	}

	public boolean isConsentSceenDisplayed() {
		return headerOfConsentPage.isDisplayed();
	}

	public boolean areLogosDisplayed() {
		return imageOfHealthCareDesign.isDisplayed() && imageOfEsignetLogo.isDisplayed();
	}

	public boolean isEssentialIconDisplayed() {
		return essentialClaimiIcon.isDisplayed();
	}

	public boolean isEssentialClaimsDisplayed() {
		return essentialClaimsList.isDisplayed();
	}

	public boolean isVoluntaryClaimsIconDisplayed() {
		return voluntaryClaimiIcon.isDisplayed();
	}

	public boolean isVoluntaryClaimsDisplayed() {
		return lookForVoluntarClaims.isDisplayed();
	}

	public boolean isAllowButtonInConsentScreenVisible() {
		return allowButtonInConsentScreen.isDisplayed();
	}

	public boolean isCancelButtonInConsentScreenVisible() {
		return cancelButtonInConsentScreen.isDisplayed();
	}

	public void clickOnEssentialTooltipIcon() {
		clickOnElement(essentialClaimiIcon, "click on essential claim tooltip iocn");
	}

	public String getEssentialClaimsTooltipText() {
		Actions actions = new Actions(driver);
		actions.moveToElement(essentialClaimiIcon).perform();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
		WebElement tooltip = wait.until(ExpectedConditions.visibilityOf(essentialClaimToolTipMessege));
		return tooltip.getText();
	}

	public void clickOnCancelBtnInConsentScreen() {
		clickOnElement(cancelButtonInConsentScreen, "click on cancel button");
	}

	public boolean isCancelWarningHeaderDisplayed() {
		return cancelWarningHeader.isDisplayed();
	}

	public boolean isCancelWarningMessageDisplayed() {
		return cancelWarningMessage.isDisplayed();
	}

	public boolean isStayButtonDisplayed() {
		return stayButton.isDisplayed();
	}

	public boolean isDiscontinueButtonDisplayed() {
		return discontinueButton.isDisplayed();
	}

	public void clickOnStayBtnInConsentScreen() {
		clickOnElement(stayButton, "click on stay button");
	}

	public void clickOnDiscontinueButton() {
		clickOnElement(discontinueButton, "click on discontinue button");
	}

	public boolean isHealthPortalPageDisplayed() {
		return signInWithEsignet.isDisplayed();
	}

	public void enableMasterToggleVoluntaryClaims() {
		if (!voluntaryClaimMasterToggle.isSelected()) {
			voluntaryClaimMasterToggle.click();
		}
	}

	public void clickOnAllowBtnInConsentScreen() {
		clickOnElement(allowButtonInConsentScreen, "click on allow button");
	}

	public boolean isWelcomePageDisplayed() {
		return welcomePageOfRelyingParty.isDisplayed();
	}

	public boolean isInputRestrictedToNineDigits() {
		String value = getElementValue(registeredMobileNumberField, "get the entered value");
		return value != null && value.length() == 9;
	}

	public boolean isMobileNumberFieldContainingOnlyDigits() {
		String value = getElementValue(registeredMobileNumberField, "get the entered value");
		return value != null && value.matches("\\d+");
	}

	public void enableFirstVoluntaryClaim(String claimName) {
		WebElement toggleLabel = driver.findElement(By.xpath("//label[@for='" + claimName + "']"));
		toggleLabel.click();
	}

	public boolean isToggleSelected(String claimName) {
		WebElement input = driver.findElement(By.id(claimName));
		return input.isSelected();
	}

	public void disableFirstVoluntaryClaim(String claimName) {
		WebElement input = driver.findElement(By.id(claimName));
		if (input.isSelected()) {
			WebElement toggleLabel = driver.findElement(By.xpath("//label[@for='" + claimName + "']"));
			toggleLabel.click();
		}
	}

	public boolean isToggleDeselected(String claimName) {
		WebElement input = driver.findElement(By.id(claimName));
		return !input.isSelected();
	}

	public boolean isVoluntaryMasterTogglePresent() {
		return isElementVisible(masterToggle, "check if master toggle is present");
	}

	public String getWelcomeMessage() {
		return welcomePageOfRelyingParty.getText();
	}

}
