#@smokeAndRegression
Feature: Esignet Login Options Page
  This feature file is for verifying the Login options page

  @smoke @loginOptionsPageVerification
  Scenario: Verify the Login options page
    Given click on Sign In with eSignet
    Then validate that the logo is displayed
    #And validate the logo alignment
    #And validate that header is displayed
    #And validate that sub-header is displayed
    #And I validate the outcomes
    #And check more outcomes
    
    
  @smoke @loginFeature
  Scenario Outline: Verify the Registration process and Login Flow
    Given click on Sign In with eSignet
    Then verify the default language in which UI is rendered
    When user clicks on the Sign-Up with Unified Login hyperlink
    Then verify user is navigated to the Mobile Number Registration screen

    When user enter valid mobile number in the mobile number field
    Then validate that the Continue button enabled
    And user clicks on the Continue button
    Then verify user is navigated to the OTP screen
    When user enters the correct OTP as input
    And user clicks on the Verify OTP button
    And verify user is redirected to the success screen
    Then user clicks on continue button on success page
    And user redirected to registration page

    Then user enter valid full name in the Full Name field
    And user enter valid password in the Password field
    And user enter valid confirm password in the Confirm Password field
    And user clicks on agrees terms condition check-box
    And user clicks on continue button on registration page
    Then it will redirect to congratulations on login page
    And user clicks on login button
    Then verify user is redirected to Login screen of eSignet
    And user clicks on login with password button
    And verify login button is disabled

    When user enter less than minimum number into mobile number field
    Then verify login button is disabled
    And user enters registered password into password field
    And user clicks on login button in login page
    Then verify error Please Enter Valid Individual ID. is displayed

    When user clicks on the navigate back button
    And user clicks on login with password button
    And user enters registered password into password field
    Then verify login button is disabled
    And user enter all digit zero into mobile number field
    And user clicks on login button in login page
    Then verify error Please Enter Valid Individual ID. is displayed

    When user enter registered mobile number in mobile number field
    And user enters special Character into password field
    And user tabout of password field
    Then verify error Please Enter Valid Password is displayed 

    And user enter registered mobile number in mobile number field
    And user enters Unregistered password into password field
    And user clicks on login button in login page
    Then verify error Username or password is not valid. Please enter valid credentials. is displayed
    When user click on the close icon in error message
    Then verify the error message disappears

    When user enter Unregistered mobile number into mobile number field
    And user enters registered password into password field
    And user clicks on login button in login page
    Then verify error Username or password is not valid. Please enter valid credentials. is displayed
    Then verify error message disappears automatically after 10 seconds

    When user enter registered mobile number in mobile number field
    And user enters random char into password field
    And user clicks on login button in login page
    Then verify error Please Enter Valid Password is displayed

    When user enter registered mobile number in mobile number field
    And user enters registered password into password field
    And user tabout of password field
    Then verify login button is enabled
    And user clicks on login button in login page
    Then verify consent should ask user to proceed in attention page
    And clicks on proceed button in attention page
    And clicks on proceed button in next page
    Then select the e-kyc verification provider
    And clicks on proceed button in e-kyc verification provider page
    And user select the check box in terms and condition page
    And user clicks on proceed button in terms and condition page 
    And user clicks on proceed button in camera preview page
    Then user is navigated to consent screen once liveness check completes
    And verify the header Please take appropriate action in xx:xx is displayed
    And verify logos of the relying party and e-Signet is displayed
    And verify the essential claims with "i" icon is displayed
    And verify required essential claims is displayed
    And verify the voluntary claims with "i" icon is displayed
    And verify list of voluntary claims displayed
    And verify allow button is visible consent screen
    And verify cancel button is visible consent screen
    And verify the tooltip message for Essential Claims info icon
    Then Enable the toggle for first voluntary claim and verify it is selected
    And Disable the toggle for first voluntary claim and verify it is deselected
    Then Verify the existence of master toggle button when only one field is present under the Voluntary claims
    When user click on cancel button in consent screen
    Then verify the header Please Confirm is displayed
    And verify the message Are you sure you want to discontinue the login process? is displayed
    And verify discontinue button is displayed
    And verify stay button is displayed
    When user click on Stay button
    Then verify user is retained on same consent screen
    When user click on cancel button in consent screen
    Then user click on Discontinue button
    And verify user is redirected to relying party portal page

    Given click on Sign In with eSignet
    And user clicks on login with password button
    And user enter registered mobile number in mobile number field
    And user enters registered password into password field
    And user clicks on login button in login page

    When user enables the master toggle of voluntary claims
    And click on allow button in consent page
    Then verify user is navigated to landing page of relying party
    And verify welcome message is displayed with the registered name


  @smoke @consentPageValidation
  Scenario Outline: Verify welcome message when full name is not consented
    Given click on Sign In with eSignet
    And user clicks on login with password button
    When user enter registered mobile number in mobile number field
    And user enters registered password into password field
    And user clicks on login button in login page
    Then Verify the welcome message when user clicks on Allow without consenting for Full name
 
  
  @loginFeature
  Scenario Outline: Verifying the testCases that validates mobile number field
    Given click on Sign In with eSignet
    And user clicks on login with password button

    When user enter more than maximum digit into mobile number field
    Then verify input should be restricted for 9 digit

    When user enter specialCharacter into mobile number field
    Then verify input should restrict it and allow only numbers

    When user enter alphaNumeric into mobile number field
    Then verify input should restrict it and allow only numbers


  