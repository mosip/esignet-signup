package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
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
 * - title       -> h1.text-center.text-2xl
 * - description -> p.text-center.text-gray-500
 * The language switcher (from the shared NavBar) is present here as well,
 * so the same ids used across the app apply:
 * - language-select-button (dropdown trigger)
 * - {lang}_language        (dropdown item, e.g. en_language / km_language)
 */
public class ErrorHandlerPage extends BasePage {

	private static final By ERROR_TITLE = By.xpath("//h1[@class='text-center text-2xl']");
	private static final By ERROR_DESCRIPTION = By.xpath("//p[@class='text-center text-gray-500']");
	private static final String ERROR_ROUTE = "something-went-wrong";

	public ErrorHandlerPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//h1[@class='text-center text-2xl']")
	WebElement errorTitle;

	@FindBy(xpath = "//p[@class='text-center text-gray-500']")
	WebElement errorDescription;

	@FindBy(id = "language-select-button")
	WebElement languageSelectButton;

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

	/** Waits until the app has redirected to the error handler route. */
	public boolean waitForErrorPage() {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(40))
					.until(ExpectedConditions.urlContains(ERROR_ROUTE));
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

	/**
	 * Switches the page language using the shared language dropdown.
	 *
	 * <p>The dropdown is a headless-UI menu whose trigger only becomes interactive
	 * once React has attached its handler; a click fired the instant the button is
	 * merely visible can be a no-op, leaving the menu closed so the option never
	 * appears. Selecting a language then re-renders the page (i18n change), which
	 * can stale a menu still mid-animation. To stay deterministic under load we
	 * wait for the trigger to be clickable and retry the whole open-and-select if
	 * the option does not show up (or an element goes stale / a click is
	 * intercepted) rather than failing the scenario on the first miss.
	 *
	 * @param twoLetterLangKey two letter language key, e.g. "en" or "km"
	 */
	public void switchLanguage(String twoLetterLangKey) {
		By optionLocator = By.id(twoLetterLangKey + "_language");
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EsignetConfigManager.getTimeout()));
		RuntimeException lastError = null;
		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				wait.until(ExpectedConditions.elementToBeClickable(languageSelectButton)).click();
				wait.until(ExpectedConditions.elementToBeClickable(optionLocator)).click();
				return;
			} catch (TimeoutException | StaleElementReferenceException | ElementClickInterceptedException e) {
				lastError = e;
			}
		}
		throw new TimeoutException(
				"Failed to switch error handler page language to '" + twoLetterLangKey + "' after 3 attempts", lastError);
	}
}
