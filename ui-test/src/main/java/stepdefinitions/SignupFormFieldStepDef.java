package stepdefinitions;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import base.BaseTest;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.SignupFormFieldPage;

// Steps for the dynamically rendered Setup Account form fields - radio, textarea and file upload.
// These fields are optional in the esqa2 UI-spec, so mandatory-validation cases are not covered here.
public class SignupFormFieldStepDef {

	private final WebDriver driver;
	private final SignupFormFieldPage formFieldPage;

	private String lastEnteredText;

	public SignupFormFieldStepDef(BaseTest baseTest) {
		this.driver = BaseTest.getDriver();
		this.formFieldPage = new SignupFormFieldPage(driver);
	}

	// ---- Radio ----------------------------------------------------------------

	@Then("verify the {string} radio field is rendered with its label and options")
	public void verifyRadioRendered(String fieldId) {
		Assert.assertTrue(formFieldPage.isRadioFieldRendered(fieldId),
				"Radio field '" + fieldId + "' should render its options");
		Assert.assertTrue(formFieldPage.isRadioLabelRendered(fieldId),
				"Radio field '" + fieldId + "' should render its label");
	}

	@When("user selects an option in the {string} radio field")
	public void userSelectsRadioOption(String fieldId) {
		formFieldPage.selectRadioOption(fieldId, 0);
	}

	@Then("verify one option is selected in the {string} radio field")
	public void verifyOneRadioSelected(String fieldId) {
		Assert.assertEquals(formFieldPage.getSelectedRadioCount(fieldId), 1,
				"Exactly one option should be selected in '" + fieldId + "'");
	}

	@When("user selects two different options in the {string} radio field")
	public void userSelectsTwoRadioOptions(String fieldId) {
		int selected = formFieldPage.selectTwoOptionsAndCountSelected(fieldId);
		Assert.assertEquals(selected, 1,
				"Radio group '" + fieldId + "' must allow only a single selection at a time");
	}

	// ---- Textarea -------------------------------------------------------------

	@Then("verify the {string} textarea field is rendered")
	public void verifyTextareaRendered(String fieldId) {
		Assert.assertTrue(formFieldPage.isTextareaRendered(fieldId),
				"Textarea field '" + fieldId + "' should be rendered");
	}

	@Then("verify the {string} textarea has {int} default rows")
	public void verifyTextareaDefaultRows(String fieldId, int expectedRows) {
		Assert.assertEquals(formFieldPage.getTextareaRows(fieldId), expectedRows,
				"Textarea '" + fieldId + "' should render with " + expectedRows + " rows by default");
	}

	@Then("verify the {string} textarea placeholder is rendered")
	public void verifyTextareaPlaceholder(String fieldId) {
		String placeholder = formFieldPage.getTextareaPlaceholder(fieldId);
		Assert.assertNotNull(placeholder, "Textarea '" + fieldId + "' should have a placeholder");
		Assert.assertFalse(placeholder.trim().isEmpty(),
				"Textarea '" + fieldId + "' placeholder should not be empty");
	}

	@When("user enters text into the {string} textarea")
	public void userEntersTextIntoTextarea(String fieldId) {
		lastEnteredText = "Automation entered details 12345";
		formFieldPage.enterTextarea(fieldId, lastEnteredText);
	}

	@Then("verify the {string} textarea retains the entered text")
	public void verifyTextareaRetainsText(String fieldId) {
		Assert.assertEquals(formFieldPage.getTextareaValue(fieldId), lastEnteredText,
				"Textarea '" + fieldId + "' should retain the entered text");
	}

	// ---- File upload ----------------------------------------------------------

	@Then("verify the {string} file upload field is rendered with its label")
	public void verifyFileUploadRendered(String fieldId) {
		Assert.assertTrue(formFieldPage.isFileUploadLabelRendered(fieldId),
				"File upload field '" + fieldId + "' should render with its label");
	}

	@When("user uploads a supported document for the {string} field")
	public void userUploadsSupportedFile(String fieldId) throws Exception {
		if ("photo".equalsIgnoreCase(fieldId)) {
			formFieldPage.uploadClasspathFile(fieldId, "config/Photo.jpg", "Photo.jpg");
		} else {
			formFieldPage.uploadClasspathFile(fieldId, "config/Passport.pdf", "Passport.pdf");
		}
	}

	@Then("verify the supported file is accepted for the {string} field")
	public void verifySupportedFileAccepted(String fieldId) {
		formFieldPage.waitForFieldValid(fieldId);
		Assert.assertFalse(formFieldPage.isUnsupportedFileErrorShown(fieldId),
				"Supported file should be accepted without a validation error for '" + fieldId + "'");
	}

	@When("user uploads an unsupported file for the {string} field")
	public void userUploadsUnsupportedFile(String fieldId) throws Exception {
		formFieldPage.uploadUnsupportedFile(fieldId);
	}

	@Then("verify an unsupported file error is shown for the {string} field")
	public void verifyUnsupportedFileError(String fieldId) {
		Assert.assertTrue(formFieldPage.isUnsupportedFileErrorShown(fieldId),
				"An unsupported file type should be rejected with an error for '" + fieldId + "'");
	}
}
