package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BasePage;
import utils.EsignetConfigManager;
import utils.WaitUtil;

/**
 * Page object for the signup critical-error popup (the {@code SignUpPopover}
 * rendered on {@code invalid_transaction}). It is a Radix AlertDialog:
 * <ul>
 * <li>container -&gt; role="alertdialog"</li>
 * <li>title     -&gt; h2 (shows the localized "error" title, e.g. "Error!")</li>
 * <li>message   -&gt; p (localized error description)</li>
 * <li>action    -&gt; button#okay-button (localized "okay" text)</li>
 * </ul>
 */
public class TransactionTimeoutPopupPage extends BasePage {

	private static final By POPUP = By.cssSelector("[role='alertdialog']");
	private static final By TITLE = By.xpath("//*[@role='alertdialog']//h2");
	private static final By MESSAGE = By.xpath("//*[@role='alertdialog']//p");
	private static final By OKAY_BUTTON = By.id("okay-button");

	public TransactionTimeoutPopupPage(WebDriver driver) {
		super(driver);
	}

	public boolean isPopupDisplayed() {
		return isVisible(POPUP);
	}

	public boolean isOkayButtonDisplayed() {
		return isVisible(OKAY_BUTTON);
	}

	public String getPopupTitle() {
		return textOf(TITLE);
	}

	public String getPopupMessage() {
		return textOf(MESSAGE);
	}

	public String getOkayButtonText() {
		return textOf(OKAY_BUTTON);
	}

	public void clickOkay() {
		clickOnElement(WaitUtil.waitForClickability(driver, OKAY_BUTTON),
				"Click Okay on the transaction timeout popup");
	}

	/**
	 * @return the app's active UI language code (e.g. "en" / "km"), read from the
	 *         same localStorage key the signup portal persists it under; defaults
	 *         to "en" if unavailable, so locale lookups match what is rendered.
	 */
	public String getActiveLanguage() {
		try {
			Object lang = ((JavascriptExecutor) driver)
					.executeScript("return window.localStorage.getItem('esignet-signup-language');");
			return (lang == null || lang.toString().isBlank()) ? "en" : lang.toString();
		} catch (Exception e) {
			return "en";
		}
	}

	private boolean isVisible(By locator) {
		try {
			return newWait().until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
		} catch (TimeoutException e) {
			return false;
		}
	}

	private String textOf(By locator) {
		return newWait().until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
	}

	private WebDriverWait newWait() {
		return new WebDriverWait(driver, Duration.ofSeconds(EsignetConfigManager.getTimeout()));
	}
}
