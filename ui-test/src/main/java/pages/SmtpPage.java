package pages;

import base.BasePage;
import utils.EsignetConfigManager;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SmtpPage extends BasePage {

	public SmtpPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//p[@class='text-gray-700 mb-2 mr-3 ml-4']")
	private List<WebElement> notifications;

	public void navigateToSmtpUrl() {
		driver.get(EsignetConfigManager.getSmtpUrl());
	}

	public void navigateToHealthPortalUrl() {
		driver.get(EsignetConfigManager.getHealthPortalUrl());
	}

	public boolean isOtpNotificationReceived() {
		return isNotificationVisibleAndHasText();
	}

	public boolean isRegistrationSuccessNotificationDisplayed() {
		return isNotificationVisibleAndHasText();
	}

	public boolean isPasswordResetSuccessNotificationDisplayed() {
		return isNotificationVisibleAndHasText();
	}

	private boolean isNotificationVisibleAndHasText() {
		for (WebElement notification : notifications) {
			if (isElementVisible(notification) && !notification.getText().trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

}
