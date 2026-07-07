Feature: Esignet Login - Password language and username field
  Verifying the login (relying-party) password field accepts multi-language
  passwords and the login button state.

  # The password field's accept/reject behaviour on the eSignet login screen is
  # governed by the oidc-ui client policy (not this repo's signup policy), so we
  # assert the reliably verifiable behaviour: the field accepts and retains the
  # multi-language input the user types.

  @regression @passwordLanguage
  Scenario: Enter password as a combination of English and Khmer
    Given click on Sign In with eSignet
    And user clicks on login with password button
    When user enters a valid format mobile number in the login mobile field
    And user enters English and Khmer combined password into password field
    And user tabout of password field
    Then verify the password field is retained the entered value

  @regression @passwordLanguage
  Scenario: Enter password as a combination of Khmer and numbers
    Given click on Sign In with eSignet
    And user clicks on login with password button
    When user enters a valid format mobile number in the login mobile field
    And user enters Khmer and numbers combined password into password field
    And user tabout of password field
    Then verify the password field is retained the entered value

  @regression @passwordLanguage
  Scenario: Enter password as a combination of an unsupported language (Hindi) and English
    Given click on Sign In with eSignet
    And user clicks on login with password button
    When user enters a valid format mobile number in the login mobile field
    And user enters Hindi and English combined password into password field
    And user tabout of password field
    Then verify the password field is retained the entered value

  @regression @loginButtonState
  Scenario: Login button stays disabled when only the password is filled
    Given click on Sign In with eSignet
    And user clicks on login with password button
    When user enters a valid password into the password field
    Then verify the login button is in disabled state
