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

			Map<String, Object> fieldProps = uiSpecFields.get(fieldId);
			String controlType = (String) fieldProps.get("controlType");

			if (fieldId.equalsIgnoreCase("phone")) {
				continue;
			}

			List<WebElement> matchingElements = driver
					.findElements(By.xpath("//*[@id='" + fieldId + "' or @data-field-id='" + fieldId + "']"));

			if (matchingElements.isEmpty()) {
				logger.info("No element found for fieldId: " + fieldId);
				continue;
			}

			WebElement element = matchingElements.get(0);
			String tag = element.getTagName();
			String type = element.getAttribute("type");

			if ("photo".equalsIgnoreCase(controlType)) {
				registrationPage.clickOnUploadPhoto();
				registrationPage.clickOnCaptureButton();
				continue;
			}

			if ("checkbox".equalsIgnoreCase(controlType)) {
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

			if ("textbox".equalsIgnoreCase(controlType) && fieldId.toLowerCase().contains("name")) {
				EsignetUtil.FullName names = EsignetUtil.generateNamesFromUiSpec();

				for (WebElement nameField : matchingElements) {
					String lang = nameField.getAttribute("data-lang");

					if ("eng".equalsIgnoreCase(lang)) {
						nameField.clear();
						nameField.sendKeys(names.english);
					} else if ("khm".equalsIgnoreCase(lang)) {
						nameField.clear();
						nameField.sendKeys(names.khmer);
						RegisteredDetails.setFullName(names.khmer);
					} else {
						logger.info("Full name entry not supported for language: " + lang);
					}
				}
				continue;
			}

			if ("password".equalsIgnoreCase(controlType)) {
				String password = EsignetUtil.generateValidPasswordFromActuator();
				RegisteredDetails.setPassword(password);
				element.clear();
				element.sendKeys(password);

				WebElement confirmPwd = driver.findElement(By.id("password_confirm"));
				confirmPwd.clear();
				confirmPwd.sendKeys(password);
				continue;
			}

			if ("textbox".equalsIgnoreCase(controlType) && fieldId.equalsIgnoreCase("email")) {
				String email = EsignetUtil.generateEmailFromRegex(fieldId);
				element.clear();
				element.sendKeys(email);
				continue;
			}

			if ("dropdown".equalsIgnoreCase(controlType)) {
				Select dropdown = new Select(element);
				List<WebElement> options = dropdown.getOptions();
				if (options.size() > 1) {
					int index = new Random().nextInt(options.size() - 1) + 1;
					dropdown.selectByIndex(index);
				}
				continue;
			}

			if ("textbox".equalsIgnoreCase(controlType)
					&& (tag.equalsIgnoreCase("input") || tag.equalsIgnoreCase("textarea"))) {
				String regex = EsignetUtil.getRegexForField(fieldId);
				String value = EsignetUtil.generateValueFromRegex(regex);
				element.clear();
				element.sendKeys(value);
			}
		}
	}
}
