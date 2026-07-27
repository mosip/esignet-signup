package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtil {
	private static final int TIMEOUT = EsignetConfigManager.getTimeout();

	public static void waitForVisibility(WebDriver driver, WebElement element) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
		wait.until(ExpectedConditions.visibilityOf(element));
	}

	public static WebElement waitForVisibility(WebDriver driver, By locator) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	public static void waitForClickability(WebDriver driver, WebElement element) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
		wait.until(ExpectedConditions.elementToBeClickable(element));
	}

	/**
	 * Resolves a locator once the element behind it is clickable. Throws
	 * {@link org.openqa.selenium.TimeoutException} (not NoSuchElementException) when
	 * it never becomes clickable, so callers can retry on a single exception type.
	 */
	public static WebElement waitForClickability(WebDriver driver, By locator) {
		return waitForClickability(driver, locator, Duration.ofSeconds(TIMEOUT));
	}

	/**
	 * As {@link #waitForClickability(WebDriver, By)}, but with an explicit budget -
	 * for callers that retry and so want each attempt to wait a fraction of the
	 * configured timeout rather than all of it.
	 */
	public static WebElement waitForClickability(WebDriver driver, By locator, Duration timeout) {
		WebDriverWait wait = new WebDriverWait(driver, timeout);
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	public static boolean waitForInvisibility(WebDriver driver, WebElement element) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
		return wait.until(ExpectedConditions.invisibilityOf(element));
	}
}
