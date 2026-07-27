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

/**
 * Page object for the "Something went wrong" error handler page
 * (route: /something-went-wrong).
 *
 * The page is rendered by ErrorPageTemplate:
 * - title       -> h1.text-center.text-2xl.font-semibold
 * - description -> p.text-center.text-gray-500
 * The language switcher (from the shared NavBar) is present here as well,
 * so the same ids used across the app apply:
 * - language-select-button (dropdown trigger)
 * - {lang}_language        (dropdown item, e.g. en_language / km_language)
 */
public class ErrorHandlerPage extends BasePage {

	/*
	 * Match the class attribute per token rather than with '=': the rendered
	 * markup carries additional utility classes (the title is
	 * "text-center text-2xl font-semibold"), and an exact match silently stops
	 * matching the moment a class is added. The XPaths are held as constants so
	 * the By locators and the @FindBy fields below cannot drift apart.
	 */
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

	/** Loads the error page directly (no HTTP status code in router state). */
	public void navigateToErrorPage() {
		driver.get(signupPortalBaseUrl() + ERROR_ROUTE);
	}

	/** Loads the signup portal root so a failing API call redirects to the error page. */
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

	/**
	 * Extra time the redirect needs on top of the configured wait.
	 *
	 * <p>The app's react-query client (signup-ui App.tsx) retries any non-4XX
	 * settings response three times, and react-query's default backoff is
	 * exponential - 1s + 2s + 4s. The 5XX examples in the outline therefore cannot
	 * reach the error route until that budget is spent, while the 4XX ones are not
	 * retried at all and redirect immediately. This is added to the configured
	 * timeout rather than replacing it, so the wait still scales with
	 * explicitWaitTimeout instead of being an unexplained constant.
	 */
	private static final Duration REDIRECT_RETRY_BUDGET = Duration.ofSeconds(7);

	/** Waits until the app has redirected to the error handler route. */
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

	// Language switching is inherited from BasePage.switchLanguage(..): the
	// dropdown here is the shared NavBar one, not an error-page control.
}
