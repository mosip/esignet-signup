package pages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
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

// Page object for the dynamically rendered (JsonFormBuilder) Setup Account form fields
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

	// Matches a whole class token, so 'error' does not also match 'error-icon'
	private static String hasClass(String cssClass) {
		return "contains(concat(' ', normalize-space(@class), ' '), ' " + cssClass + " ')";
	}

	// Bounds lookups to one field; the group is preferred since a radio renders its message outside the container
	private String fieldScopeXpath(String fieldId) {
		String container = "//*[@data-field-id='" + fieldId + "']";
		return "(" + container + "/ancestor-or-self::*[" + hasClass("form-field-group") + "][1] | " + container + ")";
	}

	// Exact id match, since starts-with() would also pick up sub-controls like <id>_docType
	public boolean isFieldInvalid(String fieldId) {
		String identifies = "(@id='" + fieldId + "' or @name='" + fieldId + "' or @data-field-id='" + fieldId + "')";
		List<WebElement> flagged = driver.findElements(
				By.xpath("//*[" + identifies + " and (@aria-invalid='true' or " + hasClass("error") + ")]"));
		return !flagged.isEmpty();
	}

	// Sub-field and neighbouring-field messages are excluded so they are not reported as this field's
	public String getFieldErrorText(String fieldId) {
		List<WebElement> errors = driver.findElements(By.xpath(fieldScopeXpath(fieldId) + "//*["
				+ hasClass("error-message")
				+ " and not(ancestor::*[" + hasClass("file-subfield") + "])"
				+ " and not(ancestor::*[@data-field-id and not(@data-field-id='" + fieldId + "')])]"));
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

	// Presence, not visibility: radio and file inputs sit hidden behind custom controls
	private boolean waitUntilRendered(By locator) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.presenceOfElementLocated(locator));
			return true;
		} catch (org.openqa.selenium.TimeoutException e) {
			return false;
		}
	}

	// ---- Radio ----------------------------------------------------------------

	public List<WebElement> getRadioOptions(String fieldId) {
		return driver.findElements(radioGroup(fieldId));
	}

	public boolean isRadioFieldRendered(String fieldId) {
		return waitUntilRendered(radioGroup(fieldId));
	}

	public boolean isRadioLabelRendered(String fieldId) {
		return waitUntilRendered(labelBy(fieldId));
	}

	public void selectRadioOption(String fieldId, int index) {
		List<WebElement> radios = getRadioOptions(fieldId);
		if (index < 0 || index >= radios.size()) {
			throw new IllegalArgumentException("Cannot select radio option " + index + " for field '" + fieldId
					+ "': " + radios.size() + " option(s) available");
		}
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

	public int selectTwoOptionsAndCountSelected(String fieldId) {
		List<WebElement> radios = getRadioOptions(fieldId);
		if (radios.size() < 2) {
			return getSelectedRadioCount(fieldId);
		}
		selectRadioOption(fieldId, 0);
		selectRadioOption(fieldId, 1);
		return getSelectedRadioCount(fieldId);
	}

	// ---- Textarea -------------------------------------------------------------

	public boolean isTextareaRendered(String fieldId) {
		return waitUntilRendered(textareaBy(fieldId));
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

	// ---- File upload ----------------------------------------------------------

	public boolean isFileUploadRendered(String fieldId) {
		return waitUntilRendered(fileInputBy(fieldId));
	}

	public boolean isFileUploadLabelRendered(String fieldId) {
		return isFileUploadRendered(fieldId) && waitUntilRendered(labelBy(fieldId));
	}

	public void uploadClasspathFile(String fieldId, String classpathResource, String fileName) throws IOException {
		Path tempPath = createRestrictedTempFile("upload-", "-" + fileName);
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
			if (in == null) {
				throw new IOException("Upload resource not found: " + classpathResource);
			}
			Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
		}
		sendFileToInput(fieldId, tempPath.toFile());
		logger.info("Uploaded supported file for " + fieldId + ": " + fileName);
	}

	public void uploadUnsupportedFile(String fieldId) throws IOException {
		Path tempPath = createRestrictedTempFile("malware-", ".exe");
		Files.write(tempPath, new byte[] { 0x4D, 0x5A });
		sendFileToInput(fieldId, tempPath.toFile());
		logger.info("Uploaded unsupported .exe file for " + fieldId);
	}

	// Falls back to the File permission API where POSIX permissions are unsupported (e.g. Windows)
	private Path createRestrictedTempFile(String prefix, String suffix) throws IOException {
		Path tempPath;
		try {
			tempPath = Files.createTempFile(prefix, suffix,
					PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
		} catch (UnsupportedOperationException e) {
			tempPath = Files.createTempFile(prefix, suffix);
			File f = tempPath.toFile();
			boolean restricted = f.setReadable(false, false);
			restricted = f.setReadable(true, true) && restricted;
			restricted = f.setWritable(false, false) && restricted;
			restricted = f.setWritable(true, true) && restricted;
			if (!restricted) {
				logger.warn("Could not fully restrict permissions on temp file: " + tempPath);
			}
		}
		tempPath.toFile().deleteOnExit();
		return tempPath;
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

	// Lets asynchronous client-side validation settle after an upload before acceptance is asserted
	public boolean waitForFieldValid(String fieldId) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(d -> !isUnsupportedFileErrorShown(fieldId));
			return true;
		} catch (org.openqa.selenium.TimeoutException e) {
			return false;
		}
	}

}
