Feature: Esignet Signup - Dynamic Setup Account form fields
  Verifying the radio, textarea and file-upload fields the JsonFormBuilder renders
  on the Setup Account form from the deployed UI-spec (esqa2: gender, details,
  passport, photo).

  # These fields are optional (required=false) in the esqa2 spec, so only
  # rendering and input behaviour are asserted here. Mandatory-validation,
  # error-clearing and default-selection cases require a required=true / default
  # schema and are covered separately when such a schema is available.

  Background:
    Given user directly navigates to sign-up portal URL
    And user clicks on Register button
    And user enters valid_mobile_number in the mobile number text box
    Then mark otp request timestamp
    And user clicks on the Continue button
    When user enters the complete 6-digit OTP
    And user clicks on the Verify OTP button
    Then remove otp request timestamp
    And verify user is redirected to the success screen
    When user click on Continue button in Success Screen
    Then verify setup account screen is displayed with header Setup Account

  @regression @radioField
  Scenario: Radio field renders with options and enforces single selection
    Then verify the "gender" radio field is rendered with its label and options
    When user selects an option in the "gender" radio field
    Then verify one option is selected in the "gender" radio field
    When user selects two different options in the "gender" radio field

  @regression @textareaField
  Scenario: Textarea renders with default rows and placeholder and accepts input
    Then verify the "details" textarea field is rendered
    And verify the "details" textarea has 2 default rows
    And verify the "details" textarea placeholder is rendered
    When user enters text into the "details" textarea
    Then verify the "details" textarea retains the entered text

  @regression @fileUploadField
  Scenario: Passport upload enforces supported file types
    Then verify the "passport" file upload field is rendered with its label
    When user uploads an unsupported file for the "passport" field
    Then verify an unsupported file error is shown for the "passport" field
    When user uploads a supported document for the "passport" field
    Then verify the supported file is accepted for the "passport" field

  @regression @fileUploadField
  Scenario: Photo upload accepts a supported image
    Then verify the "photo" file upload field is rendered with its label
    When user uploads a supported document for the "photo" field
    Then verify the supported file is accepted for the "photo" field
