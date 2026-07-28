package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BasePage;
import utils.EsignetConfigManager;

// Page object for the "Something went wrong" error handler page (route: /something-went-wrong)
public class ErrorHandlerPage extends BasePage {

	// Matched per class token, not with '=': the markup carries extra utility classes
	private static final String ERROR_TITLE_XPATH = "//h1[contains(@class,'text-center') and contains(@class,'text-2xl')]";
	private static final String ERROR_DESCRIPTION_XPATH = "//p[contains(@class,'text-center') and contains(@class,'text-gray-500')]";

	private static final By ERROR_TITLE = By.xpath(ERROR_TITLE_XPATH);
	private static final By ERROR_DESCRIPTION = By.xpath(ERROR_DESCRIPTION_XPATH);
	private static final String ERROR_ROUTE = "something-went-wrong";

	public ErrorHandlerPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = ERROR_TITLE_XPATH)
	WebElement errorTitle;

	@FindBy(xpath = ERROR_DESCRIPTION_XPATH)
	WebElement errorDescription;

	private String signupPortalBaseUrl() {
		String base = EsignetConfigManager.getSignupPortalUrl();
		return base.endsWith("/") ? base : base + "/";
	}

	public void navigateToErrorPage() {
		driver.get(signupPortalBaseUrl() + ERROR_ROUTE);
	}

	// Root is loaded so that a failing API call redirects to the error page
	public void navigateToSignupPortalRoot() {
		driver.get(signupPortalBaseUrl());
	}

	public String getErrorTitle() {
		return getText(errorTitle, "Get error handler page title");
	}

	public String getErrorDescription() {
		return getText(errorDescription, "Get error handler page description");
	}

	public boolean isErrorTitleDisplayed() {
		return isElementVisible(errorTitle, "Check error handler title displayed");
	}

	public boolean isErrorDescriptionDisplayed() {
		return isElementVisible(errorDescription, "Check error handler description displayed");
	}

	// Covers react-query's three retries of a non-4XX settings response (1s + 2s + 4s backoff)
	private static final Duration REDIRECT_RETRY_BUDGET = Duration.ofSeconds(7);

	public boolean waitForErrorPage() {
		Duration timeout = Duration.ofSeconds(EsignetConfigManager.getTimeout()).plus(REDIRECT_RETRY_BUDGET);
		try {
			new WebDriverWait(driver, timeout).until(ExpectedConditions.urlContains(ERROR_ROUTE));
			return isElementVisible(errorTitle, "Wait for error handler page");
		} catch (TimeoutException e) {
			return false;
		}
	}

	public boolean waitForTitle(String expected) {
		return waitForText(ERROR_TITLE, expected);
	}

	public boolean waitForDescription(String expected) {
		return waitForText(ERROR_DESCRIPTION, expected);
	}

	private boolean waitForText(By locator, String expected) {
		try {
			return new WebDriverWait(driver, Duration.ofSeconds(EsignetConfigManager.getTimeout()))
					.until(ExpectedConditions.textToBe(locator, expected));
		} catch (TimeoutException e) {
			return false;
		}
	}

	// Language switching is inherited from BasePage.switchLanguage(..)
}
