package pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import utils.EsignetUtil;
import utils.EsignetUtil.RegisteredDetails;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SignupFormDynamicFiller {

	private WebDriver driver;
	private static final Logger logger = Logger.getLogger(SignupFormDynamicFiller.class);
	RegistrationPage registrationPage;

	public SignupFormDynamicFiller(WebDriver driver) {
		this.driver = driver;
		registrationPage = new RegistrationPage(driver);
	}

	public void fillFormFromUiSpec(Map<String, Map<String, Object>> uiSpecFields) {

		for (String fieldId : uiSpecFields.keySet()) {

			if (fieldId.equalsIgnoreCase("phone")) {
				continue;
			}

			WebElement element = null;

			// Try finding element by id
			List<WebElement> elementsById = driver.findElements(By.id(fieldId));
			if (!elementsById.isEmpty()) {
				element = elementsById.get(0);
			}

			// Try finding element by data-field-id if not found
			if (element == null) {
				List<WebElement> elementsByDataId = driver
						.findElements(By.xpath("//*[@data-field-id='" + fieldId + "']"));
				if (!elementsByDataId.isEmpty()) {
					element = elementsByDataId.get(0);
				}
			}

			if (element == null) {
				logger.info("No element found for fieldId: " + fieldId);
				continue;
			}

			String tag = element.getTagName();
			String type = element.getAttribute("type");

			if (fieldId.equalsIgnoreCase("individualBiometrics")) {
				registrationPage.clickOnUploadPhoto();
				registrationPage.clickOnCaptureButton();
				continue;
			}

			if ("checkbox".equalsIgnoreCase(type) || fieldId.equalsIgnoreCase("consent")) {
				if (!element.isSelected()) {
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
							element);
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
				}
				continue;
			}

			if ("hidden".equalsIgnoreCase(type) || !element.isEnabled()) {
				continue;
			}

			if (fieldId.equalsIgnoreCase("fullName")) {
			    EsignetUtil.FullName names = EsignetUtil.generateNamesFromUiSpec();
			    List<WebElement> fullNameFields = driver.findElements(By.xpath("//*[@data-field-id='fullName']"));

			    for (WebElement nameField : fullNameFields) {
			        String lang = nameField.getAttribute("data-lang");

			        if ("eng".equalsIgnoreCase(lang)) {
			            nameField.clear();
			            nameField.sendKeys(names.english);
			        } 
			        else if ("khm".equalsIgnoreCase(lang)) {
			            nameField.clear();
			            nameField.sendKeys(names.khmer);
			            RegisteredDetails.setFullName(names.khmer);
			        } 
			        else {
			            logger.info("Full name entry not supported for language: " + lang);
			        }
			    }
			    continue;
			}

			if (fieldId.equalsIgnoreCase("password")) {
				String password = EsignetUtil.generateValidPasswordFromActuator();
				RegisteredDetails.setPassword(password);
				element.clear();
				element.sendKeys(password);

				WebElement confirmPwd = driver.findElement(By.id("password_confirm"));
				confirmPwd.clear();
				confirmPwd.sendKeys(password);
				continue;
			}

			if (fieldId.equalsIgnoreCase("email")) {
				String email = EsignetUtil.generateEmailFromRegex(fieldId);
				element.clear();
				element.sendKeys(email);
				continue;
			}

			if (tag.equalsIgnoreCase("select")) {
				Select dropdown = new Select(element);
				List<WebElement> options = dropdown.getOptions();
				if (options.size() > 1) {
					int index = new Random().nextInt(options.size() - 1) + 1;
					dropdown.selectByIndex(index);
				}
				continue;
			}

			if (tag.equalsIgnoreCase("input") || tag.equalsIgnoreCase("textarea")) {
				String regex = EsignetUtil.getRegexForField(fieldId);
				String value = EsignetUtil.generateValueFromRegex(regex);
				element.clear();
				element.sendKeys(value);
			}
		}
	}

	private void enterTextById(String id, String value) {
		WebElement element = driver.findElement(By.id(id));
		element.clear();
		element.sendKeys(value);
	}
}
