Feature: Esignet Signup Transaction Timeout
  Verifying that, when the signup transaction has expired, proceeding with the
  registration shows the transaction-timeout error popup
  (Header: "Error!", the localized timeout message, Button: "Okay").

  # Entry is the relying party ("Sign In with eSignet" -> "Sign-Up with Unified
  # Login"), reusing the existing login/registration steps. A real expiry takes
  # ~5 minutes (mosip.signup.*.txn.timeout = 300s), so via CDP we stub
  # generate-challenge to succeed (reaching the OTP screen deterministically,
  # without depending on real SMS/send-OTP throttling) and verify-challenge to
  # return the backend's invalid_transaction error - the same critical error a
  # genuine expiry raises when the user clicks Verify OTP. CDP interception is
  # local-Chrome only, hence @localOnly (skipped on BrowserStack).
  @transactionTimeout @localOnly
  Scenario: Setup account after the signup transaction has expired
    Given click on Sign In with eSignet
    When user clicks on the Sign-Up with Unified Login hyperlink
    Then verify user is navigated to the Mobile Number Registration screen
    When user enter valid mobile number in the mobile number field
    Given the signup transaction is forced to expire on OTP verification
    And user clicks on the Continue button
    Then verify user is navigated to the OTP screen

    When user enters "111111" as a Otp
    And user clicks on the Verify OTP button

    Then verify the transaction timeout error popup is displayed
    And verify the error popup header shows the error title
    And verify the error popup message shows the transaction timeout message
    And verify the error popup has an Okay button
    When user clicks on the Okay button in the error popup
