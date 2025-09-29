#@smokeAndRegression
Feature: Esignet Forgot Password Page
  This feature file is for verifying the forget password option page  

  @smoke @registrationProcess
  Scenario Outline: Verify user is completing registration process
    Given user directly navigates to sign-up portal URL
    And user clicks on Register button
    Then user enters mobile_number in the mobile number text box
    And user clicks on the Continue button
    When user enters the OTP
    And user clicks on the Verify OTP button 
    Then user click on Continue button in Success Screen
    And user enter text valid name in the Full Name field
    And user enter the valid password in the Password field
    And user enter the valid confirm password in the Confirm Password field
    And user accepts the Terms and Condition checkbox
    And user clicks on Continue button in Setup Account Page
  
   
  @smoke @forgetPasswordOptionsVerification
  Scenario Outline: Verify the forget password options for phoneNumber
    Given user directly navigates to sign-up portal URL
    When user click on reset password button
    And user verify country code prefix
    And user verify the water mark text inside phonenumber
    And user verify country code is not editable
    And user verify forget password heading
    And user verify subheading on forget password
    And user verify username label on forget password
    And user verify fullname label on forget password
    And user verify continue button on forget password
    And user verify footer on forget password

    When user enters number with starting 0 into the mobile number field
    And user clicks outside the input to trigger validation
    Then phone number should be invalid

    When user enters number with all zeros into the mobile number field
    And user clicks outside the input to trigger validation
    Then phone number should be invalid

    When user enters alphanumeric input into the mobile number field
    Then mobile number input should contain numbers only

    When user enters special char input into the mobile number field
    Then mobile number input should remain empty

    When user enters Registered moblie number into the mobile number field
    And user clicks outside the input to trigger validation
    Then phone number should be valid

    Then user verify continue button is not enabled

    When user enters more than max digits into the mobile number field
    Then verify the mobile number field should restrict to max digits


  @smoke @forgetPasswordFullNameVerification
  Scenario Outline: Verify the forget password options for fullName
    Given user directly navigates to sign-up portal URL
    And user click on reset password button

    When user enters Numeric input into the fullname field
    And user clicks outside the input to trigger validation
    Then user verify full name error message

    When user enters Alphanumeric input into the fullname field
    And user clicks outside the input to trigger validation
    Then user verify full name error message
    
    When user enters special char input into the fullname field
    And user clicks outside the input to trigger validation
    Then user verify full name error message

    When user enters other language input into the fullname field
    And user clicks outside the input to trigger validation
    Then user verify full name error message

    When user enters name more than maximum characters into the fullname field
    And only 30 characters are retained in the fullname field

    When user enters registered fullname into the full name field
    And user clicks outside the input to trigger validation
    Then user verify full name error message not displayed

    Then user verify continue button is not enabled

    When user enters Registered moblie number into the mobile number field
    Then user verify continue button is enabled
    Then user click on continue button

        
  @smoke @forgetPasswordOTPVerification
  Scenario Outline: Verify the forget password options for otp
    Given user directly navigates to sign-up portal URL
    And user click on reset password button

    When user enters registered fullname into the full name field
    And user enters Registered moblie number into the mobile number field
    Then user click on continue button

    And user waits until OTP timer to expire
    When user enters "<expired_otp>" as a Otp
    And user clicks on the Verify OTP button
    Then verify an error message OTP expired. Please request a new one and try again. is displayed at the top
    When user clicks on the close icon of the error message
  	Then verify the error message is not visible

    Then user clicks on the Resend OTP button
    When user enters "<InvalidOrRandomOtp>" as a Otp
    And user clicks on the Verify OTP button
    Then verify an error message OTP authentication failed. Please try again. is displayed at the top
    And verify error message disappears after 10 seconds

    When user enters "<alphabets>" as a Otp
    Then verify OTP field is rejecting alphabets

    When user enters "<Alphanumeric>" as a Otp
    Then verify OTP field is rejecting alphanumeric characters

    When user enters "<incomplete_otp>" as a Otp
    Then validate the verify button is disabled

    When user enters the OTP in forgot password flow
    Then verify OTP is masked as soon as it is entered
    And validate the verify button is enabled

    When user clicks the back button on the OTP screen
    Then verify user is redirected back to the Forget Password screen
    
    And user enters unregistered mobile number in the mobile number text box
    And user enters registered fullname into the full name field
    Then user click on continue button
    When user enters the OTP in forgot password flow
    And user clicks on the Verify OTP button
    Then verify error popup with header Invalid is displayed
    And verify error message Transaction has failed due to invalid request. Please try again. is displayed
    And verify retry button is displayed
    When user click on retry button 
    Then verify user is redirected back to the Forget Password screen
    
    When user enters Registered moblie number into the mobile number field
    And user enters Unregistered FullName into the fullname field
    Then user click on continue button
    When user enters the OTP in forgot password flow
    And user clicks on the Verify OTP button
    Then verify error popup with header Invalid is displayed
    And verify error message The mobile number or name entered is invalid. Please enter valid credentials associated with your account and try again. is displayed
    And verify retry button is displayed
    When user click on retry button 
    Then verify user is redirected back to the Forget Password screen

    Examples:
     | InvalidOrRandomOtp | expired_otp | alphabets | Alphanumeric | incomplete_otp |
     | 784590             | 111111      | ABCDEF    | Abc123       | 12             |

  @smoke @resetPasswordVerification
  Scenario Outline: Verify the reset password options
    Given user directly navigates to sign-up portal URL
    And user click on reset password button

    When user enters registered fullname into the full name field
    And user enters Registered moblie number into the mobile number field
    Then user click on continue button

    When user enters the OTP in forgot password flow
    Then user clicks on the Verify OTP button
    And verify user is redirected to the reset password screen

    Then user verify reset password header
    And user verify reset password description
    And user verify new password label
    And user verify confirm new password label
    And user verify new password input text box is present
    And user verify confirm new password input text box is present
    And user verify new password info icon is visible
    Then user click on new password info icon
    And verify new password policy displayed
    Then user verify new password field placeholder
    And user verify confirm password field placeholder
    And verify reset button is disabled

    When user enters invalid password into the new password field
    And user clicks outside the password field
    Then verify an error message Password does not meet the password policy. is displayed
    
    When user enters shorter password into the new password field
    And user clicks outside the password field
    Then verify an error message Password does not meet the password policy. is displayed
    
    When user enters longer password into the new password field
    Then verify password input is resitricted to maximum characters
    
    When user enters new password in Forgot Password flow
    And verify reset button is disabled
    
    When user enters different password into the confirm password field
    And user clicks outside the password field
    Then verify an error message New Password and Confirm New Password do not match. is displayed
    And verify reset button is disabled
    
    When user enters more than max characters into the confirm password field
    Then verify confirm password input is restricted to max characters
    
    When user enters less than min characters into the confirm password field
    And user clicks outside the password field
    Then verify an error message New Password and Confirm New Password do not match. is displayed
    
    Then validate the New Password field is masked
    And verify the Confirm Password field is masked

  	When user clicks on the unmask icon in the New Password field
  	Then validate the New Password field is unmasked

  	When user clicks on the unmask icon in the ConfirmPassword field
  	Then verify the Confirm Password field is unmasked

  	When user clicks again on the unmask icon in the New Password field
  	Then validate the New Password field is masked

  	When user clicks again on the unmask icon in the ConfirmPassword field
  	And verify the Confirm Password field is masked
  	
    Then verify reset button is disabled
    
    When user enters new password in Forgot Password flow
    And user enters new confirm password in Forgot Password flow
    And user clicks on Reset button
    Then verify system display password reset in progress message
    Then verify success screen with header Password Reset Confirmation is displayed
    And verify the message Your password has been reset successfully. Please login to proceed. is displayed
    And verify Login button is displayed
    When user clicks on Login button
    Then verify user is redirected to login screen of relying party
      
    
  @smoke @ForgetPasswordOtpNotification
  Scenario Outline: Verify the notification when OTP requested for forgot password
  	Given user opens SMTP portal
  	And navigate back to signup portal
    And user click on reset password button
    And user enters registered fullname into the full name field
    And user enters Registered moblie number into the mobile number field
    Then user click on continue button

  	And user switches back to SMTP portal
  	Then verify notification is received for otp requested
  	And switch back to signup portal
  	When user enters the OTP in forgot password flow
  	And user clicks on the Verify OTP button
  	When user enters new password in Forgot Password flow
    And user enters new confirm password in Forgot Password flow
    And user clicks on Reset button
  	Then user switches back to SMTP portal
  	And verify password changed successful notification is displayed

  @smoke @otpTimerVerification
  Scenario Outline: Verify the attempts of resend otp 
    Given user directly navigates to sign-up portal URL
    And user click on reset password button

    When user enters registered fullname into the full name field
    And user enters Registered moblie number into the mobile number field
    Then user click on continue button
    
    Then user waits for resend OTP button and verifies it's enabled or skipped
    And user waits and clicks on resend OTP, then validates 2 out of 3 attempts message
    And user waits for resend OTP button and verifies it's enabled or skipped
    Then user waits and clicks on resend OTP, then validates 1 out of 3 attempts message
    And user waits for resend OTP button and verifies it's enabled or skipped
    Then user waits and clicks on resend OTP, then validates 0 out of 3 attempts message
    And user waits for OTP timer to expire for fourth time
    Then validate the verify button is disabled

  	
  @smoke @BookMarkSignupUrl
  Scenario Outline: Navigate directly using sign-up URL
    Given user directly navigates to sign-up portal URL
    And verify the reset password button is available
    When user click on reset password button
    Then verify it is accessible,user is redirected to the Forget Password screen