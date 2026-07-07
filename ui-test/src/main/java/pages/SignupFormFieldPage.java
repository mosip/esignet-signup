package pages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BasePage;

/**
 * Page object for the dynamically rendered (JsonFormBuilder) Setup Account form
 * fields - radio (e.g. gender), textarea (e.g. details) and file upload (e.g.
 * passport / photo). Locators follow the conventions already used by
 * {@link SignupFormDynamicFiller} and {@link RegistrationPage}; invalid state is
 * detected via the {@code aria-invalid} attribute the app sets on invalid fields.
 */
public class SignupFormFieldPage extends BasePage {

	private static final Logger logger = Logger.getLogger(SignupFormFieldPage.class);

	public SignupFormFieldPage(WebDriver driver) {
		super(driver);
	}

	// ---- Generic helpers ------------------------------------------------------

	private By radioGroup(String fieldId) {
		return By.xpath("//input[@type='radio' and (@name='" + fieldId + "' or @data-field-id='" + fieldId + "')]");
	}

	private By textareaBy(String fieldId) {
		return By.xpath("//textarea[@id='" + fieldId + "' or @data-field-id='" + fieldId + "' or starts-with(@id,'"
				+ fieldId + "')]");
	}

	private By fileInputBy(String fieldId) {
		return By.xpath("//input[@type='file' and (contains(@id,'" + fieldId + "') or contains(@data-field-id,'"
				+ fieldId + "'))]");
	}

	private By labelBy(String fieldId) {
		return By.xpath("//label[@for='" + fieldId + "' or contains(@for,'" + fieldId + "')]");
	}

	/** True when the field (or its group) is flagged invalid via aria-invalid. */
	public boolean isFieldInvalid(String fieldId) {
		List<WebElement> flagged = driver.findElements(By.xpath(
				"//*[(@id='" + fieldId + "' or @name='" + fieldId + "' or @data-field-id='" + fieldId
						+ "' or starts-with(@id,'" + fieldId + "')) and @aria-invalid='true']"));
		return !flagged.isEmpty();
	}

	/** First non-empty inline error text rendered near the field, if any. */
	public String getFieldErrorText(String fieldId) {
		List<WebElement> errors = driver.findElements(By.xpath(
				"//*[@data-field-id='" + fieldId + "' or starts-with(@id,'" + fieldId
						+ "')]/following::*[contains(@class,'message') or contains(@class,'error')][1]"));
		for (WebElement e : errors) {
			String text = e.getText();
			if (text != null && !text.trim().isEmpty()) {
				return text.trim();
			}
		}
		return "";
	}

	private WebElement waitForVisible(By locator) {
		return new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	// ---- Submit / mandatory validation ---------------------------------------

	private final RegistrationPage registrationPage = new RegistrationPage(driver);

	public void submitForm() {
		registrationPage.clickOnSetupAccountContinueButton();
	}

	public boolean isContinueButtonDisabled() {
		return !registrationPage.isSetupAccountContinueEnabled();
	}

	/** Focus then blur a field via JS to mark it touched and surface validation. */
	public void touchAndBlur(By locator) {
		List<WebElement> els = driver.findElements(locator);
		if (els.isEmpty()) {
			return;
		}
		WebElement el = els.get(0);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block:'center'}); arguments[0].focus(); arguments[0].blur();", el);
	}

	// ---- Radio ----------------------------------------------------------------

	public List<WebElement> getRadioOptions(String fieldId) {
		return driver.findElements(radioGroup(fieldId));
	}

	public boolean isRadioFieldRendered(String fieldId) {
		return !getRadioOptions(fieldId).isEmpty();
	}

	public boolean isRadioLabelRendered(String fieldId) {
		return !driver.findElements(labelBy(fieldId)).isEmpty();
	}

	public void selectRadioOption(String fieldId, int index) {
		List<WebElement> radios = getRadioOptions(fieldId);
		WebElement radio = radios.get(index);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", radio);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
	}

	public int getSelectedRadioCount(String fieldId) {
		int selected = 0;
		for (WebElement radio : getRadioOptions(fieldId)) {
			if (radio.isSelected()) {
				selected++;
			}
		}
		return selected;
	}

	/** Selects two different options in turn and returns the final selected count. */
	public int selectTwoOptionsAndCountSelected(String fieldId) {
		List<WebElement> radios = getRadioOptions(fieldId);
		if (radios.size() < 2) {
			return getSelectedRadioCount(fieldId);
		}
		selectRadioOption(fieldId, 0);
		selectRadioOption(fieldId, 1);
		return getSelectedRadioCount(fieldId);
	}

	public boolean hasDefaultSelection(String fieldId) {
		return getSelectedRadioCount(fieldId) > 0;
	}

	// ---- Textarea -------------------------------------------------------------

	public boolean isTextareaRendered(String fieldId) {
		return !driver.findElements(textareaBy(fieldId)).isEmpty();
	}

	public WebElement getTextarea(String fieldId) {
		return waitForVisible(textareaBy(fieldId));
	}

	public int getTextareaRows(String fieldId) {
		String rows = getTextarea(fieldId).getAttribute("rows");
		try {
			return rows == null ? -1 : Integer.parseInt(rows.trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public String getTextareaPlaceholder(String fieldId) {
		return getTextarea(fieldId).getAttribute("placeholder");
	}

	public void enterTextarea(String fieldId, String value) {
		WebElement ta = getTextarea(fieldId);
		ta.clear();
		ta.sendKeys(value);
	}

	public String getTextareaValue(String fieldId) {
		return getTextarea(fieldId).getAttribute("value");
	}

	public int getTextareaMaxLength(String fieldId) {
		String max = getTextarea(fieldId).getAttribute("maxlength");
		try {
			return max == null ? -1 : Integer.parseInt(max.trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	// ---- File upload ----------------------------------------------------------

	public boolean isFileUploadRendered(String fieldId) {
		return !driver.findElements(fileInputBy(fieldId)).isEmpty();
	}

	public boolean isFileUploadLabelRendered(String fieldId) {
		return isFileUploadRendered(fieldId) || !driver.findElements(labelBy(fieldId)).isEmpty();
	}

	public void uploadClasspathFile(String fieldId, String classpathResource, String fileName) throws IOException {
		File tempFile = File.createTempFile("upload-", "-" + fileName);
		tempFile.deleteOnExit();
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
			if (in == null) {
				throw new IOException("Upload resource not found: " + classpathResource);
			}
			Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		sendFileToInput(fieldId, tempFile);
		logger.info("Uploaded supported file for " + fieldId + ": " + fileName);
	}

	/** Creates a throwaway unsupported (.exe) file on the fly and uploads it. */
	public void uploadUnsupportedFile(String fieldId) throws IOException {
		File tempFile = File.createTempFile("malware-", ".exe");
		tempFile.deleteOnExit();
		Files.write(tempFile.toPath(), new byte[] { 0x4D, 0x5A });
		sendFileToInput(fieldId, tempFile);
		logger.info("Uploaded unsupported .exe file for " + fieldId);
	}

	private void sendFileToInput(String fieldId, File file) {
		if (driver instanceof RemoteWebDriver) {
			((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
		}
		WebElement input = waitForVisible(fileInputBy(fieldId));
		input.sendKeys(file.getAbsolutePath());
	}

	public boolean isUnsupportedFileErrorShown(String fieldId) {
		return isFieldInvalid(fieldId) || !getFieldErrorText(fieldId).isEmpty();
	}

	public String getUploadedFileName(String fieldId) {
		List<WebElement> inputs = driver.findElements(fileInputBy(fieldId));
		if (inputs.isEmpty()) {
			return "";
		}
		String value = inputs.get(0).getAttribute("value");
		return value == null ? "" : value;
	}
}
