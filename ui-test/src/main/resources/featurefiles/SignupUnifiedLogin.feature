Feature: Esignet Signup via Unified Login
  UI happy-path for the "audit the signup events" test case: from the relying
  party, register a mobile number through "Sign-Up with Unified Login", verify
  the OTP, and proceed from the mobile-verified success screen to Account Setup.

  # Audit logs are emitted server-to-server, so they are out of scope here and covered at the API layer
  @signupUnifiedLogin @smoke
  Scenario: Verify mobile number and proceed to account setup via Unified Login
    Given click on Sign In with eSignet
    When user clicks on the Sign-Up with Unified Login hyperlink
    Then verify user is navigated to the Mobile Number Registration screen

    When user enter valid mobile number in the mobile number field
    Then mark otp request timestamp
    And user clicks on the Continue button
    Then verify user is navigated to the OTP screen

    When user enters the correct OTP as input
    And user clicks on the Verify OTP button
    Then remove otp request timestamp
    And verify user is redirected to the success screen

    Then user clicks on continue button on success page
    And user redirected to registration page
